package com.rent1.domain.deposit;

/**
 * 押金状态枚举
 * 核心业务规则：
 * 1. HELD-持有中：押金已缴纳，处于有效持有状态
 * 2. REFUNDED-已退还：押金已全额退还
 * 3. 押金结算台账记录，不可篡改
 */
public enum DepositStatus {
    
    /** 持有中 - 押金已缴纳，处于有效状态 */
    HELD("HELD", "持有中"),
    
    /** 已退还 - 押金已全额退还 */
    REFUNDED("REFUNDED", "已退还");
    
    private final String code;
    private final String name;
    
    DepositStatus(String code, String name) {
        this.code = code;
        this.name = name;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getName() {
        return name;
    }
}
