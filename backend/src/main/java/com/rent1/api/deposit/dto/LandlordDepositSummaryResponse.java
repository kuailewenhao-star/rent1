package com.rent1.api.deposit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 房东押金汇总响应
 * DEP-003: 房东查看当前有效持有押金总额
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LandlordDepositSummaryResponse {
    
    /** 当前有效持有押金总额 */
    private BigDecimal totalValidDeposit;
    
    /** 有效押金记录条数 */
    private int count;
}
