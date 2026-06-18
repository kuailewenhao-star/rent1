package com.rent1.api.controller;

import com.rent1.application.service.AdminMemberAppService;
import com.rent1.application.service.AdminMemberAppService.AdminLoginResult;
import com.rent1.api.dto.AdminLoginRequest;
import com.rent1.common.response.ApiResponse;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理员认证控制器
 * MEM-010: PC后台管理员账号密码登录
 */
@Slf4j
@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminMemberAppService adminMemberAppService;

    /**
     * 管理员登录
     * POST /api/admin/auth/login
     */
    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> adminLogin(@Valid @RequestBody AdminLoginRequest request) {
        log.info("Admin login request: username={}", request.getUsername());

        try {
            AdminLoginResult result = adminMemberAppService.login(
                    request.getUsername(),
                    request.getPassword()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("token", result.getToken());
            response.put("adminId", result.getAdminId());
            response.put("permissions", result.getPermissions());

            return ApiResponse.success(response);
        } catch (JwtException e) {
            log.error("JWT error during admin login", e);
            return ApiResponse.error("A001", "登录失败，请重试");
        }
    }

    /**
     * 临时接口：创建管理员账号（使用后需删除）
     * POST /api/admin/auth/register
     */
    @PostMapping("/register")
    public ApiResponse<String> registerAdmin(@Valid @RequestBody AdminLoginRequest request) {
        log.info("临时注册管理员: username={}", request.getUsername());
        adminMemberAppService.createAdmin(request.getUsername(), request.getPassword());
        return ApiResponse.success("admin created");
    }
}
