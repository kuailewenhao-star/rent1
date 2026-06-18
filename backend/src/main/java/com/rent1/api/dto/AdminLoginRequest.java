package com.rent1.api.dto;

import javax.validation.constraints.NotBlank;

/**
 * 管理员登录请求
 * MEM-010
 */
public class AdminLoginRequest {

    @NotBlank(message = "username不能为空")
    private String username;

    @NotBlank(message = "password不能为空")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
