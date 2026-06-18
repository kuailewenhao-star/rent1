package com.rent1.domain.invoice.enums;

import lombok.Getter;

/**
 * 计费方式枚举
 * 系统所有收入费用项，均支持以下两种计费模式
 */
@Getter
public enum ChargeType {
    
    /** 固定值计费 - 按固定金额结算 */
    FIXED("fixed", "固定值"),
    
    /** 比例分摊计费 - 按预设比例分摊总费用 */
    RATIO("ratio", "比例分摊");
    
    private final String code;
    private final String name;
    
    ChargeType(String code, String name) {
        this.code = code;
        this.name = name;
    }
}