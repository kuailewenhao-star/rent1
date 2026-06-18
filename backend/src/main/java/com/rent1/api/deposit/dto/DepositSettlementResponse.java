package com.rent1.api.deposit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 押金结算响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositSettlementResponse {
    
    /** 押金记录ID */
    private String recordId;
    
    /** 原始押金金额 */
    private BigDecimal originalAmount;
    
    /** 扣费金额 */
    private BigDecimal deductionAmount;
    
    /** 实际退还金额 */
    private BigDecimal actualRefundAmount;
    
    /** 扣费原因 */
    private String deductionReason;
    
    /** 押金状态 */
    private String status;
    
    /** 退还时间 */
    private LocalDateTime refundTime;
    
    /** 结算类型：FULL-全额，PARTIAL-部分 */
    private String settlementType;
}
