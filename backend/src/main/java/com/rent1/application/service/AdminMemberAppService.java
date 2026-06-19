package com.rent1.application.service;

import com.rent1.domain.common.BusinessException;
import com.rent1.common.enums.ErrorCode;
import com.rent1.domain.common.MemberStatus;
import com.rent1.domain.common.MemberType;
import com.rent1.domain.member.Member;
import com.rent1.domain.member.MemberRepository;
import com.rent1.infrastructure.cache.CacheService;
import com.rent1.infrastructure.security.JwtService;
import com.rent1.infrastructure.security.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 管理员会员应用服务
 * MEM-010: PC后台管理员账号密码登录
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminMemberAppService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CacheService redisService;

    // 密码错误次数缓存key前缀
    private static final String PASSWORD_FAIL_PREFIX = "admin_password_fail:";
    // 最大密码错误次数
    private static final int MAX_PASSWORD_FAIL_COUNT = 5;
    // 锁定时间（分钟）
    private static final int LOCK_MINUTES = 30;

    /**
     * 管理员登录
     */
    public AdminLoginResult login(String username, String password) {
        // 1. 检查是否被锁定
        String lockKey = PASSWORD_FAIL_PREFIX + username;
        String failCountStr = redisService.get(lockKey);
        if (failCountStr != null) {
            int failCount = Integer.parseInt(failCountStr);
            if (failCount >= MAX_PASSWORD_FAIL_COUNT) {
                throw new BusinessException(ErrorCode.A012);
            }
        }

        // 2. 查询管理员账号
        Member admin = memberRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.A010));

        // 3. 校验账号状态
        if (admin.getStatus() != MemberStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.A003);
        }

        // 4. 校验密码
        if (!passwordEncoder.matches(password, admin.getPasswordHash())) {
            // 密码错误计数
            int failCount = failCountStr != null ? Integer.parseInt(failCountStr) + 1 : 1;
            redisService.set(lockKey, String.valueOf(failCount), LOCK_MINUTES, TimeUnit.MINUTES);
            log.warn("Admin password error: username={}, failCount={}", username, failCount);
            throw new BusinessException(ErrorCode.A011);
        }

        // 5. 密码正确，清除错误计数
        redisService.delete(lockKey);

        // 6. 生成Token
        String token = jwtService.generateAdminToken(admin.getMemberId(), "ALL");

        log.info("Admin logged in: username={}", username);

        return AdminLoginResult.builder()
                .token(token)
                .adminId(admin.getMemberId())
                .permissions(new String[]{"ALL"})
                .build();
    }

    /**
     * 创建管理员（系统初始化使用）
     */
    public void createAdmin(String username, String password) {
        Member admin = Member.builder()
                .memberId(java.util.UUID.randomUUID().toString())
                .projectId("default")
                .subjectId("SUB-ADMIN")
                .userId(username)
                .memberType(MemberType.ADMIN)
                .status(MemberStatus.ACTIVE)
                .passwordHash(passwordEncoder.encode(password))
                .build();

        memberRepository.save(admin);
        log.info("Admin created: username={}", username);
    }

    @lombok.Data
    @lombok.Builder
    public static class AdminLoginResult {
        private String token;
        private String adminId;
        private String[] permissions;
    }
}
