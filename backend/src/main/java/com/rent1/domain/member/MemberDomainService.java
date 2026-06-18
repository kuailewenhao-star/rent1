package com.rent1.domain.member;

import com.rent1.domain.common.BusinessException;
import com.rent1.common.enums.ErrorCode;
import com.rent1.domain.common.MemberStatus;
import com.rent1.domain.common.MemberType;
import com.rent1.infrastructure.security.CryptoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * 会员领域服务
 * 核心业务规则、状态流转、校验逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberDomainService {

    private final MemberRepository memberRepository;
    private final MemberContactRepository memberContactRepository;
    private final CryptoService cryptoService;

    /**
     * 根据手机号和角色类型查找会员
     */
    public Member findByPhoneAndType(String phoneEncrypted, MemberType memberType) {
        return memberRepository.findByPhoneEncryptedAndMemberType(phoneEncrypted, memberType);
    }

    /**
     * 根据主体ID和角色类型查找会员
     */
    public Member findBySubjectIdAndType(String subjectId, MemberType memberType) {
        return memberRepository.findBySubjectIdAndMemberType(subjectId, memberType);
    }

    /**
     * 根据会员ID查找会员
     */
    public Member findById(String memberId) {
        Member member = memberRepository.findById(memberId);
        if (member == null) {
            throw new BusinessException(ErrorCode.A002);
        }
        return member;
    }

    /**
     * 创建新会员（如果不存在）
     * 核心业务规则：
     * 1. 同一主体可拥有多角色账号
     * 2. 注册时自动创建默认联系人
     */
    public Member createMemberIfNotExists(String phoneEncrypted, String userId, MemberType memberType) {
        // 检查是否已存在
        Member existingMember = findByPhoneAndType(phoneEncrypted, memberType);
        if (existingMember != null) {
            return existingMember;
        }

        // 生成主体ID（同一手机号首次注册时生成，后续跨角色共用）
        String subjectId = generateSubjectId(phoneEncrypted);
        
        // 创建新会员
        Member newMember = Member.builder()
                .memberId(UUID.randomUUID().toString())
                .projectId("default")
                .subjectId(subjectId)
                .userId(userId)
                .memberType(memberType)
                .phoneEncrypted(phoneEncrypted)
                .status(MemberStatus.ACTIVE)
                .build();

        memberRepository.save(newMember);
        log.info("Created new member: memberId={}, type={}", newMember.getMemberId(), memberType);

        return newMember;
    }

    /**
     * 切换会员身份
     * 同一主体下的不同角色账号切换
     */
    public Member switchMemberType(String currentMemberId, MemberType targetType) {
        Member currentMember = findById(currentMemberId);
        String subjectId = currentMember.getSubjectId();

        // 查找同一主体下目标角色的会员
        Member targetMember = findBySubjectIdAndType(subjectId, targetType);
        
        if (targetMember == null) {
            // 不存在则创建
            String phoneEncrypted = currentMember.getPhoneEncrypted();
            targetMember = createMemberIfNotExists(phoneEncrypted, currentMember.getUserId(), targetType);
        }

        // 校验目标账号状态
        if (!targetMember.isActive()) {
            throw new BusinessException(ErrorCode.A003);
        }

        log.info("Member switched from {} to {}, subjectId={}", 
                currentMember.getMemberType(), targetType, subjectId);
        
        return targetMember;
    }

    /**
     * 校验会员登录状态
     */
    public void validateLoginStatus(Member member) {
        if (!member.isActive()) {
            throw new BusinessException(ErrorCode.A003);
        }
    }

    /**
     * 更新会员信息
     */
    public Member updateMemberInfo(String memberId, String realNameEncrypted, String idCardEncrypted, String avatar) {
        Member member = findById(memberId);
        
        if (realNameEncrypted != null) {
            member.setRealNameEncrypted(realNameEncrypted);
        }
        if (idCardEncrypted != null) {
            member.setIdCardEncrypted(idCardEncrypted);
        }
        if (avatar != null) {
            member.setAvatar(avatar);
        }

        memberRepository.update(member);
        log.info("Updated member info: memberId={}", memberId);
        
        return member;
    }

    /**
     * 禁用会员账号
     */
    public void disableMember(String memberId) {
        Member member = findById(memberId);
        member.setStatus(MemberStatus.DISABLED);
        memberRepository.update(member);
        log.info("Disabled member: memberId={}", memberId);
    }

    /**
     * 根据手机号加密后的值生成主体ID
     * 相同手机号首次注册时生成，后续复用
     */
    private String generateSubjectId(String phoneEncrypted) {
        return "SUB-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * 获取会员的紧急联系人数量
     */
    public long countEmergencyContacts(String memberId) {
        return memberContactRepository.countByMemberIdAndIsEmergency(memberId, true);
    }

    /**
     * 校验会员是否有紧急联系人
     */
    public boolean hasEmergencyContact(String memberId) {
        return countEmergencyContacts(memberId) > 0;
    }

    /**
     * 根据会员ID获取所有联系人
     */
    public List<MemberContact> getContacts(String memberId) {
        return memberContactRepository.findByMemberId(memberId);
    }

    /**
     * 创建默认联系人
     * 注册时自动创建本人默认联系人
     */
    public MemberContact createDefaultContact(String memberId, String nameEncrypted, String phoneEncrypted) {
        MemberContact contact = MemberContact.builder()
                .contactId(UUID.randomUUID().toString())
                .memberId(memberId)
                .nameEncrypted(nameEncrypted)
                .phoneEncrypted(phoneEncrypted)
                .isDefault(true)
                .isEmergency(false)
                .build();

        memberContactRepository.save(contact);
        log.info("Created default contact for member: memberId={}", memberId);
        
        return contact;
    }
}
