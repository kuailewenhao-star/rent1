package com.rent1.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 房源业务状态枚举
 * NORMAL：正常经营
 * LEASE_EXPIRED：租期到期停用
 * TERMINATED：主动终止经营
 * VOID：作废
 */
@Getter
public enum HouseSourceStatus {
    
    NORMAL("NORMAL", "正常经营"),
    LEASE_EXPIRED("LEASE_EXPIRED", "租期到期停用"),
    TERMINATED("TERMINATED", "主动终止经营"),
    VOID("VOID", "作废");
    
    @EnumValue
    private final String code;
    private final String name;
    
    HouseSourceStatus(String code, String name) {
        this.code = code;
        this.name = name;
    }
    
    public static HouseSourceStatus fromCode(String code) {
        for (HouseSourceStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown HouseSourceStatus code: " + code);
    }
    
    /**
     * 判断是否为停用状态
     */
    public boolean isDeactivated() {
        return this == LEASE_EXPIRED || this == TERMINATED;
    }
}