package com.rent1.domain.invoice.enums;

import lombok.Getter;

/**
 * 收入账单状态枚举
 * 状态流转：待支付 → 已支付 / 逾期未付
 * 押金账单特殊状态：押金已收
 */
@Getter
public enum InvoiceStatus {
    
    /** 待支付 */
    PENDING("PENDING", "待支付"),
    
    /** 已支付 */
    PAID("PAID", "已支付"),
    
    /** 逾期未付 */
    OVERDUE("OVERDUE", "逾期未付"),
    
    /** 押金已收（押金账单专用） */
    DEPOSIT_RECEIVED("DEPOSIT_RECEIVED", "押金已收"),
    
    /** 作废 */
    VOID("VOID", "作废");
    
    private final String code;
    private final String name;
    
    InvoiceStatus(String code, String name) {
        this.code = code;
        this.name = name;
    }
}