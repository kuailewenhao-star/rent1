package com.rent1.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 房源类型枚举
 * 整租房源：ENTIRE
 * 合租房源：SHARED
 */
@Getter
public enum HouseSourceType {
    
    ENTIRE("ENTIRE", "整租房源"),
    SHARED("SHARED", "合租房源");
    
    @EnumValue
    private final String code;
    private final String name;
    
    HouseSourceType(String code, String name) {
        this.code = code;
        this.name = name;
    }
    
    public static HouseSourceType fromCode(String code) {
        for (HouseSourceType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown HouseSourceType code: " + code);
    }
}