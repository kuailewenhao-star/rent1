package com.rent1.api.deposit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 租客押金响应
 * DEP-004: 租客查看当前有效押金
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantDepositResponse {
    
    /** 有效押金金额 */
    private BigDecimal validDeposit;
}
