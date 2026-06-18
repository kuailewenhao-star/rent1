package com.rent1.api.deposit.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 部分扣费退还押金请求
 * DEP-002: 押金结算（部分扣费）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PartialRefundRequest extends DepositSettlementRequest {
    
    /** 扣费金额 */
    @NotNull(message = "扣费金额不能为空")
    @DecimalMin(value = "0", message = "扣费金额不能为负数")
    private BigDecimal deductionAmount;
    
    /** 扣费原因 */
    @NotBlank(message = "扣费原因不能为空")
    private String deductionReason;
}
