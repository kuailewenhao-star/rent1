package com.rent1.domain.invoice.entity;

import com.rent1.domain.invoice.enums.FeeType;
import com.rent1.domain.invoice.enums.InvoiceStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 收入账单实体 - 租客缴费账单
 * 核心业务规则：
 * 1. 账单生成（系统自动周期生成 + 房东手动录入杂费）
 * 2. 账单状态流转：待支付 → 已支付 / 逾期未付
 * 3. 公摊分摊逻辑：房源维度录入，按合约比例拆分
 * 4. 差异化提醒：租金日催缴、杂费单次提醒
 * 5. 防重复机制：同一房间、同一费用类型、同一计费周期仅生成唯一账单
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncomeInvoice {

    /** 账单ID */
    private String invoiceId;

    /** 关联合约ID */
    private String contractId;

    /** 租客会员ID */
    private String tenantMemberId;

    /** 房间ID */
    private String roomId;

    /** 房源ID（冗余字段，便于查询） */
    private String houseSourceId;

    /** 房东会员ID */
    private String landlordMemberId;

    /** 费用类型：RENT/DEPOSIT/WATER/ELECTRIC/GAS/BROADBAND/PROPERTY/TRASH/OTHER */
    private FeeType feeType;

    /** 金额 */
    private BigDecimal amount;

    /** 计费周期起始时间 */
    private LocalDate cycleStart;

    /** 计费周期结束时间 */
    private LocalDate cycleEnd;

    /** 应付日期 */
    private LocalDate dueDate;

    /** 实际支付时间 */
    private LocalDateTime paidTime;

    /** 是否手动录入 */
    private Boolean isManual;

    /** 账单状态：PENDING/PAID/OVERDUE/DEPOSIT_RECEIVED/VOID */
    private InvoiceStatus status;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /**
     * 初始化账单 - 设置初始状态
     */
    public void initialize() {
        this.status = InvoiceStatus.PENDING;
        this.isManual = false;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 核销账单 - 标记已支付
     */
    public void markAsPaid(LocalDateTime paidTime) {
        if (this.status != InvoiceStatus.PENDING && this.status != InvoiceStatus.OVERDUE) {
            throw new IllegalStateException("账单状态不允许核销");
        }
        this.status = this.feeType == FeeType.DEPOSIT ? InvoiceStatus.DEPOSIT_RECEIVED : InvoiceStatus.PAID;
        this.paidTime = paidTime;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 标记逾期
     */
    public void markAsOverdue() {
        if (this.status == InvoiceStatus.PENDING) {
            this.status = InvoiceStatus.OVERDUE;
            this.updateTime = LocalDateTime.now();
        }
    }

    /**
     * 作废账单
     */
    public void voidInvoice() {
        this.status = InvoiceStatus.VOID;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 校验账单是否可核销
     */
    public boolean canPay() {
        return this.status == InvoiceStatus.PENDING || this.status == InvoiceStatus.OVERDUE;
    }

    /**
     * 校验账单是否逾期
     */
    public boolean isOverdue(LocalDate today) {
        return this.status == InvoiceStatus.PENDING 
            && this.dueDate != null 
            && this.dueDate.isBefore(today);
    }

    /**
     * 校验账单是否有效（非作废）
     */
    public boolean isValid() {
        return this.status != InvoiceStatus.VOID;
    }

    /**
     * 获取账单周期描述
     */
    public String getCycleDescription() {
        if (cycleStart == null || cycleEnd == null) {
            return "";
        }
        return cycleStart.toString() + " 至 " + cycleEnd.toString();
    }
}