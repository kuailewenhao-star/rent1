package com.rent1.api.dto.invoice;

import lombok.Data;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * 公摊费用录入请求DTO
 * 接口：POST /api/income-invoices/shared
 */
@Data
public class SharedInvoiceRequest {

    /** 费费类型（9类枚举） */
    @NotBlank(message = "费用类型不能为空")
    private String feeType;

    /** 总费用金额 */
    @NotNull(message = "总费用金额不能为空")
    @DecimalMin(value = "0.01", message = "金额必须大于0")
    @DecimalMax(value = "999999.99", message = "金额超出范围")
    private BigDecimal totalAmount;

    /** 房源ID */
    @NotBlank(message = "房源ID不能为空")
    private String houseSourceId;

    /** 结算月份（格式：yyyy-MM） */
    @NotBlank(message = "结算月份不能为空")
    private String billMonth;
}