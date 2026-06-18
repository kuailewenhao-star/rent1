package com.rent1.domain.invoice.enums;

import lombok.Getter;

/**
 * 计费周期枚举
 * 除押金为一次性收入外，其余所有周期性收入费用，均支持单独选择计费周期
 */
@Getter
public enum ChargeCycle {
    
    /** 每月 - 按自然月周期生成账单 */
    MONTHLY("MONTHLY", "每月"),
    
    /** 每三月（季度） - 按季度周期生成账单 */
    QUARTERLY("QUARTERLY", "每三月"),
    
    /** 每六月（半年） - 按半年度周期生成账单 */
    HALF_YEARLY("HALF_YEARLY", "每六月"),
    
    /** 每年 - 按年度周期生成账单 */
    YEARLY("YEARLY", "每年");
    
    private final String code;
    private final String name;
    
    ChargeCycle(String code, String name) {
        this.code = code;
        this.name = name;
    }
}