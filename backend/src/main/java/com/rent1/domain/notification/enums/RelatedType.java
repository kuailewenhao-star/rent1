package com.rent1.domain.notification.enums;

import lombok.Getter;

/**
 * 关联业务类型枚举
 */
@Getter
public enum RelatedType {
    
    /** 合约 */
    CONTRACT("CONTRACT", "合约"),
    
    /** 账单 */
    INVOICE("INVOICE", "账单"),
    
    /** 房间 */
    ROOM("ROOM", "房间"),
    
    /** 房源 */
    HOUSE_SOURCE("HOUSE_SOURCE", "房源"),
    
    /** 押金 */
    DEPOSIT("DEPOSIT", "押金"),
    
    /** 无关联 */
    NONE("NONE", "无关联");
    
    private final String code;
    private final String name;
    
    RelatedType(String code, String name) {
        this.code = code;
        this.name = name;
    }
    
    public static RelatedType fromCode(String code) {
        for (RelatedType type : RelatedType.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return NONE;
    }
}