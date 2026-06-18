package com.rent1.domain.common;

/**
 * 合约状态枚举
 * 合约贯穿全业务流程，管控账单生成、房间状态、统计口径
 */
public enum ContractStatus {
    /** 履约中：合约生效、租期正常进行，持续按周期生成账单 */
    ACTIVE("履约中"),

    /** 已到期完结：租期正常届满，无未结清欠费，合约归档终止 */
    EXPIRED("已到期完结"),

    /** 提前解约：租期未到手动退租，人工终止租约 */
    TERMINATED_EARLY("提前解约"),

    /** 作废：新建未履约、无账单产生的无效合约，不参与统计 */
    VOID("作废");

    private final String description;

    ContractStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
