package com.rent1.domain.deposit;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 押金记录实体 - 押金台账式结算管理
 * 
 * 核心业务规则：
 * 1. 押金与收入账单(DEPOSIT类型)关联
 * 2. 押金结算两种模式：全额退还、部分扣费后退还
 * 3. 已退押金不纳入有效押金统计
 * 4. 押金记录不可篡改，仅新增结算记录
 * 
 * 数据隔离规则：
 * - 房东仅可操作自有房源下的押金
 * - 租客仅可查看本人押金的退还状态
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepositRecord {
    
    /** 押金记录ID */
    private String recordId;
    
    /** 关联合约ID */
    private String contractId;
    
    /** 关联押金账单ID */
    private String invoiceId;
    
    /** 租客会员ID */
    private String tenantMemberId;
    
    /** 房东会员ID */
    private String landlordMemberId;
    
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
    private DepositStatus status;
    
    /** 退还时间 */
    private LocalDateTime refundTime;
    
    /** 创建时间 */
    private LocalDateTime createTime;
    
    /** 更新时间 */
    private LocalDateTime updateTime;
    
    /**
     * 创建押金记录 - 初始状态为HELD持有中
     */
    public void initialize(BigDecimal originalAmount) {
        this.originalAmount = originalAmount;
        this.deductionAmount = BigDecimal.ZERO;
        this.actualRefundAmount = BigDecimal.ZERO;
        this.status = DepositStatus.HELD;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 全额退还押金
     * @return 实际退还金额
     */
    public BigDecimal fullRefund() {
        if (this.status != DepositStatus.HELD) {
            throw new IllegalStateException("押金已结算，不可重复操作");
        }
        this.actualRefundAmount = this.originalAmount;
        this.deductionAmount = BigDecimal.ZERO;
        this.deductionReason = null;
        this.status = DepositStatus.REFUNDED;
        this.refundTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
        return this.actualRefundAmount;
    }
    
    /**
     * 部分扣费后退还
     * @param deductionAmount 扣费金额
     * @param deductionReason 扣费原因
     * @return 实际退还金额
     */
    public BigDecimal partialRefund(BigDecimal deductionAmount, String deductionReason) {
        if (this.status != DepositStatus.HELD) {
            throw new IllegalStateException("押金已结算，不可重复操作");
        }
        if (deductionAmount.compareTo(this.originalAmount) > 0) {
            throw new IllegalArgumentException("扣费金额不能超过押金总额");
        }
        this.deductionAmount = deductionAmount;
        this.deductionReason = deductionReason;
        this.actualRefundAmount = this.originalAmount.subtract(deductionAmount);
        this.status = DepositStatus.REFUNDED;
        this.refundTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
        return this.actualRefundAmount;
    }
    
    /**
     * 校验押金是否可结算
     */
    public boolean canSettle() {
        return this.status == DepositStatus.HELD;
    }
    
    /**
     * 校验是否为有效持有押金
     * 有效押金：状态为HELD且关联的押金账单已支付
     */
    public boolean isValidHeldDeposit() {
        return this.status == DepositStatus.HELD;
    }
}
