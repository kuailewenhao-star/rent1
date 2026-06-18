package com.rent1.domain.billing;

import lombok.Getter;

/**
 * 计费周期枚举
 * MONTHLY：每月
 * QUARTERLY：每季度（3个月）
 * HALF_YEARLY：每半年（6个月）
 * YEARLY：每年（12个月）
 *
 * 说明：押金DEPOSIT无计费周期，为一次性费用
 */
@Getter
public enum ChargeCycle {

    MONTHLY("MONTHLY", "每月", 1),
    QUARTERLY("QUARTERLY", "每季度", 3),
    HALF_YEARLY("HALF_YEARLY", "每半年", 6),
    YEARLY("YEARLY", "每年", 12);

    private final String code;
    private final String name;
    private final int months;

    ChargeCycle(String code, String name, int months) {
        this.code = code;
        this.name = name;
        this.months = months;
    }

    public static ChargeCycle fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (ChargeCycle cycle : values()) {
            if (cycle.getCode().equalsIgnoreCase(code)) {
                return cycle;
            }
        }
        return null;
    }
}
