package com.rent1.application.service;

import com.rent1.api.dto.ContactDTO;
import com.rent1.api.dto.MemberProfileDTO;
import com.rent1.domain.common.BusinessException;
import com.rent1.common.enums.ErrorCode;
import com.rent1.domain.common.MemberStatus;
import com.rent1.domain.common.MemberType;
import com.rent1.domain.member.Member;
import com.rent1.domain.member.MemberContact;
import com.rent1.domain.member.MemberDomainService;
import com.rent1.domain.member.MemberContactDomainService;
import com.rent1.infrastructure.cache.RedisService;
import com.rent1.infrastructure.security.DesensitizationService;
import com.rent1.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 会员应用服务
 * 负责业务流程编排、跨领域调度、事务控制
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberAppService {

    private final MemberDomainService memberDomainService;
    private final MemberContactDomainService memberContactDomainService;
    private final JwtService jwtService;
    private final RedisService redisService;

    // 防重复注册锁的key前缀
    private static final String LOGIN_LOCK_PREFIX = "wechat_login:";
    private static final long LOGIN_LOCK_TIMEOUT = 30; // 30秒

    /**
     * 微信授权登录
     * MEM-001: 新用户首次登录自动注册
     * MEM-002: 老用户回登录
     */
    @Transactional
    public LoginResult loginByWechat(String code, String memberTypeStr) {
        MemberType memberType = MemberType.fromCode(memberTypeStr);

        // 1. 调用微信API获取 openid 和手机号
        WechatAuthResult wechatResult = callWechatApi(code);
        String phone = wechatResult.getPhone();
        String userId = wechatResult.getOpenid();

        // 2. 分布式锁防重复注册
        // 注意：此处对手机号做简单哈希以避免明文出现在 Redis key 中
        String lockKey = LOGIN_LOCK_PREFIX + Math.abs(phone.hashCode()) + ":" + memberType;
        if (!redisService.acquireLock(lockKey, LOGIN_LOCK_TIMEOUT, TimeUnit.SECONDS)) {
            throw new BusinessException(ErrorCode.R001);
        }

        try {
            // 3. 按（明文）手机号+角色类型查询会员
            Member member = memberDomainService.findByPhoneAndType(phone, memberType);
            boolean hasAccount = true;

            if (member == null) {
                hasAccount = false;
                // 注册新用户 - 传入明文手机号，加密由 TypeHandler 在写入时完成
                member = memberDomainService.createMemberIfNotExists(phone, userId, memberType);

                // 自动创建默认联系人 - 同样传入明文
                memberDomainService.createDefaultContact(
                        member.getMemberId(),
                        member.getRealNameEncrypted(), // 实体中已是明文
                        phone
                );
                log.info("New member registered: memberId={}, type={}", member.getMemberId(), memberType);
            } else {
                memberDomainService.validateLoginStatus(member);
            }

            // 4. 生成JWT Token
            String token = jwtService.generateUserToken(
                    member.getMemberId(),
                    member.getMemberType().getCode(),
                    member.getSubjectId()
            );

            return LoginResult.builder()
                    .token(token)
                    .memberId(member.getMemberId())
                    .memberType(member.getMemberType().getCode())
                    .hasAccount(hasAccount)
                    .build();

        } finally {
            redisService.releaseLock(lockKey);
        }
    }

    /**
     * 身份切换
     * MEM-003: 同一手机号切换房东/租客身份
     */
    public LoginResult switchMemberType(String currentMemberId, String targetMemberTypeStr) {
        MemberType targetType = MemberType.fromCode(targetMemberTypeStr);

        Member targetMember = memberDomainService.switchMemberType(currentMemberId, targetType);

        // 生成新Token
        String token = jwtService.generateUserToken(
                targetMember.getMemberId(),
                targetMember.getMemberType().getCode(),
                targetMember.getSubjectId()
        );

        return LoginResult.builder()
                .token(token)
                .memberId(targetMember.getMemberId())
                .memberType(targetMember.getMemberType().getCode())
                .hasAccount(true)
                .build();
    }

    /**
     * 获取会员个人信息
     * MEM-030: 个人信息查询
     * <p>
     * 注：加密字段（手机号、姓名、身份证号）由 SensitiveStringTypeHandler 在
     * MyBatis 读取时自动解密，因此这里直接 getter 即可得到明文。
     * 小程序端做脱敏展示，管理后台展示明文。
     */
    public MemberProfileDTO getProfile(String memberId, boolean isMiniProgram) {
        Member member = memberDomainService.findById(memberId);

        MemberProfileDTO profile = new MemberProfileDTO();
        profile.setMemberId(member.getMemberId());
        profile.setMemberType(member.getMemberType().getCode());
        profile.setAvatar(member.getAvatar());

        String phone = member.getPhoneEncrypted();
        String realName = member.getRealNameEncrypted();
        String idCard = member.getIdCardEncrypted();

        if (isMiniProgram) {
            profile.setPhone(DesensitizationService.staticMaskPhone(phone));
            profile.setRealName(DesensitizationService.staticMaskName(realName));
            profile.setIdCard(DesensitizationService.staticMaskIdCard(idCard));
        } else {
            profile.setPhone(phone);
            profile.setRealName(realName);
            profile.setIdCard(idCard);
        }

        return profile;
    }

    /**
     * 更新会员个人信息
     * MEM-030: 个人信息编辑
     * <p>
     * 注：敏感字段的加密由 SensitiveStringTypeHandler 在写入数据库时自动完成，
     * 因此领域实体中直接设置明文即可。
     */
    @Transactional
    public void updateProfile(String memberId, String realName, String idCard, String avatar) {
        memberDomainService.updateMemberInfo(memberId, realName, idCard, avatar);
    }

    /**
     * 获取会员联系人列表
     * MEM-020: 联系人管理
     */
    public List<ContactDTO> getContacts(String memberId, boolean isMiniProgram) {
        List<MemberContact> contacts = memberDomainService.getContacts(memberId);

        return contacts.stream().map(contact -> {
            ContactDTO dto = new ContactDTO();
            dto.setContactId(contact.getContactId());
            dto.setIsDefault(contact.getIsDefault());
            dto.setIsEmergency(contact.getIsEmergency());

            String name = contact.getNameEncrypted();
            String phone = contact.getPhoneEncrypted();

            if (isMiniProgram) {
                dto.setName(DesensitizationService.staticMaskName(name));
                dto.setPhone(DesensitizationService.staticMaskPhone(phone));
            } else {
                dto.setName(name);
                dto.setPhone(phone);
            }

            if (contact.getRelationship() != null) {
                dto.setRelationship(contact.getRelationship().getCode());
                dto.setRelationshipName(contact.getRelationship().getName());
            }

            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 新增联系人
     * MEM-020
     */
    @Transactional
    public ContactDTO createContact(String memberId, String name, String phone,
                                    String relationship, Boolean isEmergency, boolean isMiniProgram) {
        // 参数校验
        if (!memberContactDomainService.validatePhoneFormat(phone)) {
            throw new BusinessException(ErrorCode.V001);
        }

        // 敏感字段加密由 TypeHandler 在写入数据库时完成，此处向领域层传入明文
        MemberContact contact = memberContactDomainService.addContact(
                memberId, name, phone, relationship, isEmergency);

        ContactDTO dto = new ContactDTO();
        dto.setContactId(contact.getContactId());
        dto.setIsDefault(contact.getIsDefault());
        dto.setIsEmergency(contact.getIsEmergency());

        if (isMiniProgram) {
            dto.setName(DesensitizationService.staticMaskName(name));
            dto.setPhone(DesensitizationService.staticMaskPhone(phone));
        } else {
            dto.setName(name);
            dto.setPhone(phone);
        }

        if (contact.getRelationship() != null) {
            dto.setRelationship(contact.getRelationship().getCode());
            dto.setRelationshipName(contact.getRelationship().getName());
        }

        return dto;
    }

    /**
     * 删除联系人
     * MEM-020
     */
    @Transactional
    public void deleteContact(String contactId, String memberId) {
        memberContactDomainService.deleteContact(contactId, memberId);
    }

    /**
     * 校验会员紧急联系人是否满足合约创建条件
     * 合约创建强校验：至少1条紧急联系人
     */
    public void validateEmergencyContactForContract(String memberId) {
        if (!memberDomainService.hasEmergencyContact(memberId)) {
            throw new BusinessException("C002", "紧急联系人不能为空，无法创建合约");
        }
    }

    // 内部类：微信授权结果
    private WechatAuthResult callWechatApi(String code) {
        // TODO: 实际需要调用微信接口
        // 这里简化处理，实际应该调用微信的 auth.code2Session 接口
        // 返回 openid 和加密手机号
        return new WechatAuthResult("mock_openid_" + code, "13800138000");
    }

    // 内部类：登录结果
    @lombok.Data
    @lombok.Builder
    public static class LoginResult {
        private String token;
        private String memberId;
        private String memberType;
        private boolean hasAccount;
    }

    // 内部类：微信授权结果
    @lombok.Data
    private static class WechatAuthResult {
        private String openid;
        private String phone;
        
        public WechatAuthResult(String openid, String phone) {
            this.openid = openid;
            this.phone = phone;
        }
    }
}
