package com.rent1.domain.invoice.enums;

import lombok.Getter;

/**
 * 收入账单费用类型枚举（固定9类）
 * 核心业务规则：
 * 1. 租金、押金为系统强制固定项，默认自带、不可删除、不可修改名称
 * 2. 杂费为可选配置项，房东可按需新增、编辑、删除
 * 3. 所有费用项类型仅支持系统枚举选择，不支持用户手动自定义输入费用名称
 */
@Getter
public enum FeeType {
    
    /** 租金（核心固定必选项） */
    RENT("RENT", "租金", true),
    
    /** 押金（核心固定必选项） */
    DEPOSIT("DEPOSIT", "押金", true),
    
    /** 水费（可选收入） */
    WATER("WATER", "水费", false),
    
    /** 电费（可选收入） */
    ELECTRIC("ELECTRIC", "电费", false),
    
    /** 燃气费（可选收入） */
    GAS("GAS", "燃气费", false),
    
    /** 宽带费（可选收入） */
    BROADBAND("BROADBAND", "宽带费", false),
    
    /** 物业费（可选收入） */
    PROPERTY("PROPERTY", "物业费", false),
    
    /** 垃圾清运费（可选收入） */
    TRASH("TRASH", "垃圾清运费", false),
    
    /** 其他杂费（兜底通用收入） */
    OTHER("OTHER", "其他杂费", false);
    
    private final String code;
    private final String name;
    /** 是否为系统固定项（租金、押金不可删除） */
    private final boolean immutable;
    
    FeeType(String code, String name, boolean immutable) {
        this.code = code;
        this.name = name;
        this.immutable = immutable;
    }
}