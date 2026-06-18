package com.rent1.api.dto;

/**
 * 登录响应
 */
public class LoginResponse {

    private String token;
    private String memberId;
    private String memberType;
    private boolean hasAccount;

    public LoginResponse() {
    }

    public LoginResponse(String token, String memberId, String memberType, boolean hasAccount) {
        this.token = token;
        this.memberId = memberId;
        this.memberType = memberType;
        this.hasAccount = hasAccount;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getMemberType() {
        return memberType;
    }

    public void setMemberType(String memberType) {
        this.memberType = memberType;
    }

    public boolean isHasAccount() {
        return hasAccount;
    }

    public void setHasAccount(boolean hasAccount) {
        this.hasAccount = hasAccount;
    }
}
