package com.rent1.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 会员类型枚举
 * LANDLORD：房东会员
 * TENANT：租客会员
 * ADMIN：平台管理员会员
 */
@Getter
public enum MemberType {
    
    LANDLORD("LANDLORD", "房东会员"),
    TENANT("TENANT", "租客会员"),
    ADMIN("ADMIN", "平台管理员会员");
    
    @EnumValue
    private final String code;
    private final String name;
    
    MemberType(String code, String name) {
        this.code = code;
        this.name = name;
    }
    
    public static MemberType fromCode(String code) {
        for (MemberType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown MemberType code: " + code);
    }
    
    /**
     * 判断是否为房东
     */
    public boolean isLandlord() {
        return this == LANDLORD;
    }
    
    /**
     * 判断是否为租客
     */
    public boolean isTenant() {
        return this == TENANT;
    }
    
    /**
     * 判断是否为管理员
     */
    public boolean isAdmin() {
        return this == ADMIN;
    }
}