package com.rent1.api.dto.invoice;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 收入账单响应DTO
 */
@Data
public class IncomeInvoiceResponse {

    /** 账单ID */
    private String invoiceId;

    /** 合约ID */
    private String contractId;

    /** 租客姓名（脱敏） */
    private String tenantName;

    /** 房间名称 */
    private String roomName;

    /** 房源名称 */
    private String houseSourceName;

    /** 费用类型 */
    private String feeType;

    /** 费用类型名称 */
    private String feeTypeName;

    /** 金额 */
    private BigDecimal amount;

    /** 计费周期起始时间 */
    private LocalDate cycleStart;

    /** 计费周期结束时间 */
    private LocalDate cycleEnd;

    /** 周期描述 */
    private String cycleDescription;

    /** 应付日期 */
    private LocalDate dueDate;

    /** 实际支付时间 */
    private LocalDateTime paidTime;

    /** 账单状态 */
    private String status;

    /** 账单状态名称 */
    private String statusName;

    /** 是否手动录入 */
    private Boolean isManual;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 是否逾期 */
    private Boolean isOverdue;

    /** 逾期天数 */
    private Integer overdueDays;
}