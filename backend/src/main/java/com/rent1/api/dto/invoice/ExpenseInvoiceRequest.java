package com.rent1.api.dto.invoice;

import lombok.Data;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 支出账单创建请求DTO
 * 接口：POST /api/expense-invoices
 */
@Data
public class ExpenseInvoiceRequest {

    /** 支出费用类型（10类枚举） */
    @NotBlank(message = "费用类型不能为空")
    private String costType;

    /** 房源ID（必填） */
    @NotBlank(message = "房源ID不能为空")
    private String houseSourceId;

    /** 支出金额 */
    @NotNull(message = "支出金额不能为空")
    @DecimalMin(value = "0.01", message = "金额必须大于0")
    @DecimalMax(value = "999999.99", message = "金额超出范围")
    private BigDecimal amount;

    /** 支出发生时间（必填） */
    @NotNull(message = "支出发生时间不能为空")
    private LocalDate costDate;

    /** 备注 */
    @Size(max = 500, message = "备注最长500字符")
    private String remark;
}