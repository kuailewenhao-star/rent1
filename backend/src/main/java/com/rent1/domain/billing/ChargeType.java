package com.rent1.domain.billing;

import lombok.Getter;

/**
 * 计费方式枚举
 * fixed：固定值 - 每月/每季度等收取固定金额
 * ratio：比例分摊 - 按总费用的比例分摊（公摊场景）
 */
@Getter
public enum ChargeType {

    FIXED("fixed", "固定值"),
    RATIO("ratio", "比例分摊");

    private final String code;
    private final String name;

    ChargeType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static ChargeType fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (ChargeType type : values()) {
            if (type.getCode().equalsIgnoreCase(code)) {
                return type;
            }
        }
        return null;
    }
}
