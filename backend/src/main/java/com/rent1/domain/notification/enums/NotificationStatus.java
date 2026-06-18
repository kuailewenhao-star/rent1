package com.rent1.domain.notification.enums;

import lombok.Getter;

/**
 * 消息状态枚举
 */
@Getter
public enum NotificationStatus {
    
    /** 未读 */
    UNREAD("UNREAD", "未读"),
    
    /** 已读 */
    READ("READ", "已读");
    
    private final String code;
    private final String name;
    
    NotificationStatus(String code, String name) {
        this.code = code;
        this.name = name;
    }
    
    /**
     * 根据code获取枚举
     */
    public static NotificationStatus fromCode(String code) {
        for (NotificationStatus status : NotificationStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return UNREAD;
    }
}