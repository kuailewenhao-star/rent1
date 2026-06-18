package com.rent1.domain.notification.enums;

import lombok.Getter;

/**
 * 消息类型枚举
 * 核心业务规则：
 * 1. 合约到期提醒：提前7天、前3天双向推送
 * 2. 账单差异化提醒：租金日循环催收、杂费单次通知
 * 3. 合约状态变更提醒：解约/完结/作废通知
 * 4. 公摊账单到账通知
 */
@Getter
public enum NotificationType {
    
    /** 合约到期提醒 */
    CONTRACT_EXPIRING("CONTRACT_EXPIRING", "合约到期提醒", true),
    
    /** 租金催收提醒（每日循环） */
    RENT_REMINDER("RENT_REMINDER", "租金催收提醒", true),
    
    /** 杂费提醒（单次） */
    MISC_FEE_REMINDER("MISC_FEE_REMINDER", "杂费提醒", false),
    
    /** 合约状态变更通知 */
    CONTRACT_STATUS_CHANGE("CONTRACT_STATUS_CHANGE", "合约状态变更通知", false),
    
    /** 公摊账单到账通知 */
    SHARED_BILL_NOTICE("SHARED_BILL_NOTICE", "公摊账单到账通知", false),
    
    /** 系统消息 */
    GENERAL("GENERAL", "系统消息", false);
    
    private final String code;
    private final String name;
    /** 是否为周期性提醒（租金催收为每日循环） */
    private final boolean recurring;
    
    NotificationType(String code, String name, boolean recurring) {
        this.code = code;
        this.name = name;
        this.recurring = recurring;
    }
    
    /**
     * 根据code获取枚举
     */
    public static NotificationType fromCode(String code) {
        for (NotificationType type : NotificationType.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return GENERAL;
    }
}