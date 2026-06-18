package com.rent1.domain.invoice.enums;

import lombok.Getter;

/**
 * 支出账单费用类型枚举（固定10类）
 * 支出账单统一固定类目，仅支持下拉选择，杜绝类目杂乱
 */
@Getter
public enum CostType {
    
    /** 房源租金支出 - 房东支付给大房东的整屋承租租金 */
    HOUSE_RENT("HOUSE_RENT", "房源租金支出"),
    
    /** 房源押金支出 - 房东支付给大房东的承租保证金 */
    HOUSE_DEPOSIT("HOUSE_DEPOSIT", "房源押金支出"),
    
    /** 水费支出 */
    WATER("WATER", "水费支出"),
    
    /** 电费支出 */
    ELECTRIC("ELECTRIC", "电费支出"),
    
    /** 燃气费支出 */
    GAS("GAS", "燃气费支出"),
    
    /** 宽带费支出 */
    BROADBAND("BROADBAND", "宽带费支出"),
    
    /** 物业费支出 */
    PROPERTY("PROPERTY", "物业费支出"),
    
    /** 垃圾清运费支出 */
    TRASH("TRASH", "垃圾清运费支出"),
    
    /** 房屋维修支出 */
    REPAIR("REPAIR", "房屋维修支出"),
    
    /** 其他支出 */
    OTHER("OTHER", "其他支出");
    
    private final String code;
    private final String name;
    
    CostType(String code, String name) {
        this.code = code;
        this.name = name;
    }
}