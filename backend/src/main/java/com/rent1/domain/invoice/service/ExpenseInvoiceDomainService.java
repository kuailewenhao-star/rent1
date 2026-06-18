package com.rent1.domain.invoice.service;

import com.rent1.domain.invoice.entity.ExpenseInvoice;
import com.rent1.domain.invoice.enums.CostType;
import com.rent1.domain.common.BusinessException;
import com.rent1.common.enums.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 支出账单领域服务 - 房东成本支出台账管理
 * 核心业务规则：
 * 1. 无任何自动生成机制：所有支出账单仅支持房东手动录入、手动编辑
 * 2. 无账单状态体系：支出账单为房东个人经营成本台账，无需缴费、无需核销
 * 3. 专属关联边界：支出账单仅绑定【房源ID+房东用户ID】，不关联合约、不关联租客
 * 4. 全场景录入适配：支持周期性固定成本、一次性临时成本、公摊成本全场景录入
 * 5. 权限隔离规则：租客无任何支出账单查看、操作权限，仅房东可视可操作
 */
@Slf4j
@Service
public class ExpenseInvoiceDomainService {

    /**
     * 创建支出账单
     * 核心业务规则：
     * 1. 选择10类支出枚举
     * 2. 关联房源ID（必填）
     * 3. 输入金额
     * 4. 选择支出发生时间（必填）
     * 5. 可选备注
     * 6. 无状态体系，纯台账记录
     * 
     * @param houseSourceId 房源ID
     * @param landlordMemberId 房东会员ID
     * @param costType 支出费用类型
     * @param amount 支出金额
     * @param costDate 支出发生时间
     * @param remark 备注
     * @return 支出账单实体
     */
    public ExpenseInvoice createExpenseRecord(String houseSourceId, String landlordMemberId,
                                              String costType, BigDecimal amount, 
                                              LocalDate costDate, String remark) {
        // 校验必填字段
        if (houseSourceId == null || houseSourceId.isEmpty()) {
            throw new BusinessException(ErrorCode.E001.getCode(), "请选择关联房源");
        }
        if (costDate == null) {
            throw new BusinessException(ErrorCode.E002.getCode(), "请选择支出发生时间");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.E003.getCode(), "金额必须大于0");
        }
        
        ExpenseInvoice expense = ExpenseInvoice.builder()
            .expenseId(generateExpenseId())
            .houseSourceId(houseSourceId)
            .landlordMemberId(landlordMemberId)
            .costType(CostType.valueOf(costType))
            .amount(amount)
            .costDate(costDate)
            .remark(remark)
            .build();
        
        expense.initialize();
        log.info("创建支出账单: houseSourceId={}, costType={}, amount={}", 
                 houseSourceId, costType, amount);
        
        return expense;
    }

    /**
     * 更新支出账单
     * 核心业务规则：
     * 1. 校验账单属于当前房东
     * 2. 可修改：金额、支出时间、备注、费用类型
     * 
     * @param expense 支出账单实体
     * @param landlordMemberId 当前房东ID
     * @param amount 新金额
     * @param costDate 新支出时间
     * @param costType 新费用类型
     * @param remark 新备注
     */
    public void updateRecord(ExpenseInvoice expense, String landlordMemberId,
                            BigDecimal amount, LocalDate costDate, 
                            String costType, String remark) {
        // 校验归属权限
        if (!expense.belongsTo(landlordMemberId)) {
            throw new BusinessException(ErrorCode.P001.getCode(), "无权限编辑该支出账单");
        }
        
        // 校验金额有效性
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.E003.getCode(), "金额必须大于0");
        }
        
        expense.update(amount, costDate, CostType.valueOf(costType), remark);
        log.info("更新支出账单: expenseId={}, amount={}", expense.getExpenseId(), amount);
    }

    /**
     * 删除支出账单
     * 核心业务规则：
     * 1. 校验归属当前房东
     * 2. 物理删除记录
     * 
     * @param expense 支出账单实体
     * @param landlordMemberId 当前房东ID
     */
    public void deleteRecord(ExpenseInvoice expense, String landlordMemberId) {
        // 校验归属权限
        if (!expense.belongsTo(landlordMemberId)) {
            throw new BusinessException(ErrorCode.P001.getCode(), "无权限删除该支出账单");
        }
        
        log.info("删除支出账单: expenseId={}", expense.getExpenseId());
        // 实际删除操作在Repository层执行
    }

    /**
     * 校验支出账单数据有效性
     */
    public void validateExpenseData(String houseSourceId, BigDecimal amount, LocalDate costDate) {
        if (houseSourceId == null || houseSourceId.isEmpty()) {
            throw new BusinessException(ErrorCode.E001.getCode(), "请选择关联房源");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.E003.getCode(), "金额必须大于0");
        }
        if (costDate == null) {
            throw new BusinessException(ErrorCode.E002.getCode(), "请选择支出发生时间");
        }
    }

    /**
     * 生成支出账单ID
     */
    private String generateExpenseId() {
        return "EXP" + System.currentTimeMillis() + String.format("%04d", (int)(Math.random() * 10000));
    }
}