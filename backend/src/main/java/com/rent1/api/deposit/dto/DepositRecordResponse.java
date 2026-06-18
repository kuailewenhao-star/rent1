package com.rent1.api.deposit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 押金记录响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositRecordResponse {
    
    /** 押金记录ID */
    private String recordId;
    
    /** 关联合约ID */
    private String contractId;
    
    /** 关联房间ID */
    private String roomId;
    
    /** 关联房源ID */
    private String houseSourceId;
    
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
    
    /** 押金状态名称 */
    private String statusName;
    
    /** 退还时间 */
    private LocalDateTime refundTime;
    
    /** 创建时间 */
    private LocalDateTime createTime;
}
