package com.rent1.domain.common;

/**
 * 会员账号状态枚举
 */
public enum MemberStatus {
    ACTIVE("ACTIVE", "正常"),
    DISABLED("DISABLED", "已禁用");

    private final String code;
    private final String name;

    MemberStatus(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
