package com.rent1.domain.invoice.entity;

import com.rent1.domain.invoice.enums.CostType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 支出账单实体 - 房东成本支出台账
 * 核心业务规则：
 * 1. 无任何自动生成机制：所有支出账单仅支持房东手动录入、手动编辑
 * 2. 无账单状态体系：支出账单为房东个人经营成本台账，无需缴费、无需核销
 * 3. 专属关联边界：支出账单仅绑定【房源ID+房东用户ID】，不关联合约、不关联租客
 * 4. 全场景录入适配：支持周期性固定成本、一次性临时成本、公摊成本全场景录入
 * 5. 权限隔离规则：租客无任何支出账单查看、操作权限，仅房东可视可操作
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseInvoice {

    /** 支出账单ID */
    private String expenseId;

    /** 关联房源ID（必填，锁定成本归属房源） */
    private String houseSourceId;

    /** 房东会员ID */
    private String landlordMemberId;

    /** 支出费用类型（10类固定枚举） */
    private CostType costType;

    /** 支出金额 */
    private BigDecimal amount;

    /** 支出发生时间 */
    private LocalDate costDate;

    /** 支出备注 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /**
     * 初始化支出账单
     */
    public void initialize() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 更新支出账单信息
     */
    public void update(BigDecimal amount, LocalDate costDate, CostType costType, String remark) {
        this.amount = amount;
        this.costDate = costDate;
        this.costType = costType;
        this.remark = remark;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 校验支出账单是否属于指定房东
     */
    public boolean belongsTo(String landlordId) {
        return this.landlordMemberId != null && this.landlordMemberId.equals(landlordId);
    }

    /**
     * 校验金额有效性
     */
    public boolean isValidAmount() {
        return this.amount != null && this.amount.compareTo(BigDecimal.ZERO) > 0;
    }
}