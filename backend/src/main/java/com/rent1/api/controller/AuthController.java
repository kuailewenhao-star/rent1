package com.rent1.api.controller;

import com.rent1.application.service.MemberAppService;
import com.rent1.application.service.MemberAppService.LoginResult;
import com.rent1.api.dto.*;
import com.rent1.common.response.ApiResponse;
import com.rent1.api.interceptor.AuthInterceptor;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 认证控制器
 * MEM-001: 微信授权登录
 * MEM-002: 老用户回登录
 * MEM-003: 身份切换
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final MemberAppService memberAppService;

    /**
     * 微信授权登录
     * POST /api/auth/wechat/login
     */
    @PostMapping("/wechat/login")
    public ApiResponse<LoginResponse> wechatLogin(@Valid @RequestBody WechatLoginRequest request) {
        log.info("Wechat login request: memberType={}", request.getMemberType());
        
        try {
            LoginResult result = memberAppService.loginByWechat(
                    request.getCode(), 
                    request.getMemberType()
            );

            LoginResponse response = new LoginResponse(
                    result.getToken(),
                    result.getMemberId(),
                    result.getMemberType(),
                    result.isHasAccount()
            );

            return ApiResponse.success(response);
        } catch (JwtException e) {
            log.error("JWT error during login", e);
            return ApiResponse.error("A001", "登录失败，请重试");
        }
    }

    /**
     * 身份切换
     * POST /api/auth/switch
     */
    @PostMapping("/switch")
    public ApiResponse<LoginResponse> switchMemberType(
            HttpServletRequest httpRequest,
            @Valid @RequestBody SwitchMemberTypeRequest request) {
        
        String currentMemberId = (String) httpRequest.getAttribute(AuthInterceptor.ATTR_MEMBER_ID);
        log.info("Switch member type: currentMemberId={}, targetType={}", 
                currentMemberId, request.getTargetMemberType());

        LoginResult result = memberAppService.switchMemberType(
                currentMemberId, 
                request.getTargetMemberType()
        );

        LoginResponse response = new LoginResponse(
                result.getToken(),
                result.getMemberId(),
                result.getMemberType(),
                result.isHasAccount()
        );

        return ApiResponse.success(response);
    }
}
