package com.rent1.domain.common;

/**
 * 会员角色枚举
 * LANDLORD: 房东会员
 * TENANT: 租客会员
 * ADMIN: 平台管理员会员
 */
public enum MemberType {
    LANDLORD("LANDLORD", "房东"),
    TENANT("TENANT", "租客"),
    ADMIN("ADMIN", "管理员");

    private final String code;
    private final String name;

    MemberType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static MemberType fromCode(String code) {
        for (MemberType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown member type: " + code);
    }
}
