package com.rent1.api.dto.invoice;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 支出账单响应DTO
 */
@Data
public class ExpenseInvoiceResponse {

    /** 支出账单ID */
    private String expenseId;

    /** 房源名称 */
    private String houseSourceName;

    /** 支出费用类型 */
    private String costType;

    /** 支出费用类型名称 */
    private String costTypeName;

    /** 支出金额 */
    private BigDecimal amount;

    /** 支出发生时间 */
    private LocalDate costDate;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}