package com.rent1.api.contract.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * 合约作废请求DTO
 */
@Data
public class VoidContractRequest {

    /** 作废原因（可选） */
    private String reason;
}
