package com.rent1.api.dto.invoice;

import lombok.Data;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * 手动录入杂费账单请求DTO
 * 接口：POST /api/income-invoices/manual
 */
@Data
public class ManualInvoiceRequest {

    /** 费费类型（9类枚举） */
    @NotBlank(message = "费用类型不能为空")
    private String feeType;

    /** 金额 */
    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.01", message = "金额必须大于0")
    @DecimalMax(value = "999999.99", message = "金额超出范围")
    private BigDecimal amount;

    /** 房间ID（必须关联有效合约） */
    @NotBlank(message = "房间ID不能为空")
    private String roomId;

    /** 结算月份（格式：yyyy-MM） */
    @NotBlank(message = "结算月份不能为空")
    private String billMonth;

    /** 备注 */
    @Size(max = 200, message = "备注最长200字符")
    private String remark;
}