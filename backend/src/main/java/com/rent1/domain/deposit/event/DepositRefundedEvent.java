package com.rent1.domain.deposit.event;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 押金退还领域事件
 * 
 * 触发时机：押金结算完成后触发
 * 
 * 订阅方/处理逻辑：
 * - 消息域：发送押金退还通知
 * - 数据域：更新押金统计数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepositRefundedEvent {
    
    /** 押金记录ID */
    private String recordId;
    
    /** 关联合约ID */
    private String contractId;
    
    /** 租客会员ID */
    private String tenantMemberId;
    
    /** 房东会员ID */
    private String landlordMemberId;
    
    /** 原始押金金额 */
    private BigDecimal originalAmount;
    
    /** 扣费金额 */
    private BigDecimal deductionAmount;
    
    /** 实际退还金额 */
    private BigDecimal actualRefundAmount;
    
    /** 扣费原因 */
    private String deductionReason;
    
    /** 退还时间 */
    private LocalDateTime refundTime;
    
    /** 事件发生时间 */
    private LocalDateTime eventTime;
    
    /**
     * 创建押金退还事件
     */
    public static DepositRefundedEvent create(String recordId, String contractId,
                                            String tenantMemberId, String landlordMemberId,
                                            BigDecimal originalAmount, BigDecimal deductionAmount,
                                            BigDecimal actualRefundAmount, String deductionReason) {
        return new DepositRefundedEventBuilder()
            .recordId(recordId)
            .contractId(contractId)
            .tenantMemberId(tenantMemberId)
            .landlordMemberId(landlordMemberId)
            .originalAmount(originalAmount)
            .deductionAmount(deductionAmount)
            .actualRefundAmount(actualRefundAmount)
            .deductionReason(deductionReason)
            .refundTime(LocalDateTime.now())
            .eventTime(LocalDateTime.now())
            .build();
    }

    public static DepositRefundedEventBuilder builder() {
        return new DepositRefundedEventBuilder();
    }

    public static class DepositRefundedEventBuilder {
        private String recordId;
        private String contractId;
        private String tenantMemberId;
        private String landlordMemberId;
        private BigDecimal originalAmount;
        private BigDecimal deductionAmount;
        private BigDecimal actualRefundAmount;
        private String deductionReason;
        private LocalDateTime refundTime;
        private LocalDateTime eventTime;

        public DepositRefundedEventBuilder recordId(String recordId) { this.recordId = recordId; return this; }
        public DepositRefundedEventBuilder contractId(String contractId) { this.contractId = contractId; return this; }
        public DepositRefundedEventBuilder tenantMemberId(String tenantMemberId) { this.tenantMemberId = tenantMemberId; return this; }
        public DepositRefundedEventBuilder landlordMemberId(String landlordMemberId) { this.landlordMemberId = landlordMemberId; return this; }
        public DepositRefundedEventBuilder originalAmount(BigDecimal originalAmount) { this.originalAmount = originalAmount; return this; }
        public DepositRefundedEventBuilder deductionAmount(BigDecimal deductionAmount) { this.deductionAmount = deductionAmount; return this; }
        public DepositRefundedEventBuilder actualRefundAmount(BigDecimal actualRefundAmount) { this.actualRefundAmount = actualRefundAmount; return this; }
        public DepositRefundedEventBuilder deductionReason(String deductionReason) { this.deductionReason = deductionReason; return this; }
        public DepositRefundedEventBuilder refundTime(LocalDateTime refundTime) { this.refundTime = refundTime; return this; }
        public DepositRefundedEventBuilder eventTime(LocalDateTime eventTime) { this.eventTime = eventTime; return this; }

        public DepositRefundedEvent build() {
            return new DepositRefundedEvent(recordId, contractId, tenantMemberId, landlordMemberId,
                originalAmount, deductionAmount, actualRefundAmount, deductionReason, refundTime, eventTime);
        }
    }
}
