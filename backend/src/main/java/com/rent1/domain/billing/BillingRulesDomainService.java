package com.rent1.domain.billing;

import com.rent1.common.enums.ErrorCode;
import com.rent1.domain.billing.entity.BillingItem;
import com.rent1.domain.billing.entity.BillingRules;
import com.rent1.domain.billing.entity.BillingRulesSnapshot;
import com.rent1.domain.common.BusinessException;

import java.math.BigDecimal;

/**
 * 计费规则领域服务 - 承载全部核心业务规则校验与状态流转
 *
 * 核心职责（单一入口，禁止业务规则散落至Controller/应用层）：
 * 1. 锁定校验：所有编辑操作前统一校验 isLocked
 * 2. 唯一性校验：同房间同feeType仅允许1条配置
 * 3. 金额/比例边界校验
 * 4. 系统固定项保护：租金/押金不可删除
 * 5. 计费规则锁机：合约创建瞬间调用
 * 6. 快照生成：锁定时产出不可变副本
 *
 * 设计原则：这是纯领域服务，不依赖任何基础设施（DB/缓存/HTTP），
 * 所有入参为领域对象，返回值为领域对象或抛出业务异常。
 */
public class BillingRulesDomainService {

    /**
     * 校验计费规则是否已锁定 - 全局统一入口
     * 任何编辑操作（新增/修改/删除）前必须调用
     *
     * 业务规则 R-001：已签约房间计费规则永久锁定，任何编辑操作禁止
     */
    public void validateNotLocked(BillingRules rules) {
        if (rules == null) {
            return;
        }
        if (rules.isLocked()) {
            throw new BusinessException(ErrorCode.B120);
        }
    }

    /**
     * 校验费用类型在当前计费规则中是否已存在（防重复建单）
     *
     * 业务规则 R-002：UNIQUE(room_id, fee_type)
     */
    public void validateFeeTypeNotDuplicate(BillingRules rules, FeeType feeType) {
        if (rules == null || feeType == null) {
            return;
        }
        if (rules.containsFeeType(feeType)) {
            throw new BusinessException(ErrorCode.B101);
        }
    }

    /**
     * 校验计费项金额/比例值的合法性
     *
     * 金额范围：0 ~ 999999.99（fixed模式）
     * 比例范围：0 ~ 1.0（ratio模式）
     */
    public void validateChargeValue(BillingItem item) {
        if (item == null) {
            return;
        }
        BigDecimal value = item.getChargeValue();
        if (value == null) {
            // 未传值由接入层参数校验拦截，此处放行
            return;
        }
        // 负值统一拦截
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(ErrorCode.B103);
        }
        // ratio模式：0 ~ 1.0
        if (item.getChargeType() == ChargeType.RATIO) {
            if (value.compareTo(BigDecimal.ONE) > 0) {
                throw new BusinessException(ErrorCode.B104);
            }
        }
        // fixed模式：0 ~ 999999.99
        if (item.getChargeType() == ChargeType.FIXED) {
            if (value.compareTo(new BigDecimal("999999.99")) > 0) {
                throw new BusinessException(ErrorCode.B103);
            }
        }
    }

    /**
     * 校验是否为可删除项 - 租金/押金为系统固定项，不可删除
     */
    public void validateDeletable(FeeType feeType) {
        if (feeType == null) {
            return;
        }
        if (feeType.isSystemFixed()) {
            throw new BusinessException(ErrorCode.B110);
        }
    }

    /**
     * 校验计费规则是否具备签约条件
     * 签约前置条件：至少包含租金RENT和押金DEPOSIT两项
     */
    public void validateReadyForContract(BillingRules rules) {
        if (rules == null || rules.getItems() == null || rules.getItems().isEmpty()) {
            throw new BusinessException(ErrorCode.C004);
        }
        boolean hasRent = rules.containsFeeType(FeeType.RENT);
        boolean hasDeposit = rules.containsFeeType(FeeType.DEPOSIT);
        if (!hasRent || !hasDeposit) {
            throw new BusinessException(ErrorCode.C004);
        }
    }

    /**
     * 为计费规则添加新费用项
     * 完整校验链：未锁定 → 同feeType不重复 → 金额/比例合法
     */
    public BillingRules configureNewItem(BillingRules rules, BillingItem item) {
        if (rules == null) {
            return null;
        }
        // 1. 锁定校验（R-001）
        validateNotLocked(rules);
        // 2. 同feeType唯一性校验（R-002）
        validateFeeTypeNotDuplicate(rules, item.getFeeType());
        // 3. 金额/比例边界校验
        validateChargeValue(item);
        // 4. 追加到items列表，自动设置feeTypeName/immutable标签
        item.normalizeByFeeType();
        rules.addItem(item);
        return rules;
    }

    /**
     * 更新指定费用类型的配置（仅可更新金额/周期/方式/备注，feeType不可变）
     */
    public BillingRules updateExistingItem(BillingRules rules, FeeType feeType, BillingItem updated) {
        if (rules == null || feeType == null) {
            return rules;
        }
        // 1. 锁定校验
        validateNotLocked(rules);
        // 2. 目标项必须存在
        if (!rules.containsFeeType(feeType)) {
            throw new BusinessException(ErrorCode.B101);
        }
        // 3. 金额/比例边界校验
        if (updated != null) {
            validateChargeValue(updated);
        }
        // 4. 执行更新（feeType保持不变）
        rules.updateItem(feeType, updated);
        return rules;
    }

    /**
     * 删除指定费用类型的配置
     */
    public BillingRules removeExistingItem(BillingRules rules, FeeType feeType) {
        if (rules == null || feeType == null) {
            return rules;
        }
        // 1. 锁定校验
        validateNotLocked(rules);
        // 2. 不可删除系统固定项（租金/押金）
        validateDeletable(feeType);
        // 3. 执行删除
        rules.removeItem(feeType);
        return rules;
    }

    /**
     * 锁定计费规则 + 生成快照
     * 调用方：合约创建流程（ContractDomainService）
     *
     * 流程：
     * 1. 校验计费规则完整（至少含租金+押金）
     * 2. isLocked置为true，记录lockedAt
     * 3. 产出 BillingRulesSnapshot 不可变副本
     *
     * 之后所有编辑入口都会被 validateNotLocked 拦截。
     */
    public BillingRulesSnapshot lockAndCreateSnapshot(BillingRules rules, String contractId) {
        // 1. 完整性校验
        validateReadyForContract(rules);
        // 2. 执行锁机
        rules.lock();
        // 3. 产出不可变快照（供账单域生成首期/周期账单）
        return BillingRulesSnapshot.fromBillingRules(rules, contractId);
    }
}
