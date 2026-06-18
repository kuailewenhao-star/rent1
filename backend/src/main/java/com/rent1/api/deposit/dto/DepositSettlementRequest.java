package com.rent1.api.deposit.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 押金结算请求基类
 */
@Data
public class DepositSettlementRequest {
    
    /** 押金记录ID */
    @NotBlank(message = "押金记录ID不能为空")
    private String depositId;
}
