package com.rent1.api.contract.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 合约解约请求DTO
 */
@Data
public class TerminateContractRequest {

    /** 终止类型：LANDLORD / TENANT / MUTUAL */
    @NotNull(message = "终止类型不能为空")
    private String terminationType;

    /** 退还金额（押金全额退） */
    @NotNull(message = "退还金额不能为空")
    private BigDecimal refundAmount;

    /** 扣费金额（可选，如有扣费） */
    private BigDecimal deductionAmount;

    /** 扣费原因（可选） */
    private String deductionRemark;
}
