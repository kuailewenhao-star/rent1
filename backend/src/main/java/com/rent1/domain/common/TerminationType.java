package com.rent1.domain.common;

/**
 * 终止类型枚举
 * 用于区分合约终止的具体原因
 */
public enum TerminationType {
    /** 房东发起终止 */
    LANDLORD("房东发起"),

    /** 租客发起终止 */
    TENANT("租客发起"),

    /** 双方协商终止 */
    MUTUAL("协商终止");

    private final String description;

    TerminationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
