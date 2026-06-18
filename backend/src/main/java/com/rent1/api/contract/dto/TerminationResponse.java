package com.rent1.api.contract.dto;

import lombok.Data;
import lombok.Builder;

/**
 * 合约终止响应DTO
 */
@Data
@Builder
public class TerminationResponse {

    /** 合约ID */
    private String contractId;

    /** 终止类型 */
    private String terminationType;

    /** 退还金额 */
    private String refundAmount;

    /** 扣费金额 */
    private String deductionAmount;
}
