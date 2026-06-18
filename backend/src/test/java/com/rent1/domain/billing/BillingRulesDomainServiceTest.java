package com.rent1.domain.billing;

import com.rent1.domain.billing.entity.BillingItem;
import com.rent1.domain.billing.entity.BillingRules;
import com.rent1.domain.billing.entity.BillingRulesSnapshot;
import com.rent1.domain.common.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 计费规则领域服务 - 单元测试
 *
 * 覆盖核心业务规则：
 * 1. 新增/编辑费用项（正常流程 + 边界条件）
 * 2. 锁定后编辑拦截（R-001）
 * 3. 同房间同 feeType 唯一性校验（R-002）
 * 4. 租金/押金不可删除（系统固定项）
 * 5. 金额/比例边界校验
 * 6. 签约锁机 + 生成快照
 *
 * 本测试纯领域层，不依赖数据库/Spring，可独立快速执行。
 */
class BillingRulesDomainServiceTest {

    private BillingRulesDomainService domainService;

    @BeforeEach
    void setUp() {
        domainService = new BillingRulesDomainService();
    }

    // =====================================================
    // 正常流程 - 新增/编辑/删除
    // =====================================================

    @Test
    @DisplayName("BLL-01 空置房间新增水费配置成功")
    void addWaterFeeItem_success() {
        // given
        BillingRules rules = buildUnlockedRules();
        BillingItem waterFee = buildItem(FeeType.WATER, ChargeType.FIXED,
                new BigDecimal("30.00"), ChargeCycle.MONTHLY);

        // when
        rules = domainService.configureNewItem(rules, waterFee);

        // then
        assertNotNull(rules);
        assertEquals(3, rules.itemCount()); // RENT + DEPOSIT + WATER
        assertTrue(rules.containsFeeType(FeeType.WATER));
        assertFalse(rules.isLocked());
    }

    @Test
    @DisplayName("BLL-02 编辑租金金额 - 成功")
    void updateRentFee_success() {
        // given
        BillingRules rules = buildUnlockedRules();
        BillingItem updated = new BillingItem();
        updated.setChargeValue(new BigDecimal("2500.00"));
        updated.setChargeCycle(ChargeCycle.MONTHLY);

        // when
        rules = domainService.updateExistingItem(rules, FeeType.RENT, updated);

        // then
        BillingItem rent = rules.findItemByFeeType(FeeType.RENT).get();
        assertEquals(0, new BigDecimal("2500.00").compareTo(rent.getChargeValue()));
    }

    @Test
    @DisplayName("BLL-03 删除杂费配置 - 成功")
    void deleteWaterFee_success() {
        // given
        BillingRules rules = buildUnlockedRules();
        BillingItem waterFee = buildItem(FeeType.WATER, ChargeType.FIXED,
                new BigDecimal("30.00"), ChargeCycle.MONTHLY);
        rules = domainService.configureNewItem(rules, waterFee);

        // when
        rules = domainService.removeExistingItem(rules, FeeType.WATER);

        // then
        assertFalse(rules.containsFeeType(FeeType.WATER));
        assertEquals(2, rules.itemCount()); // RENT + DEPOSIT 保留
    }

    // =====================================================
    // 锁定后拦截 - R-001 计费锁机规则
    // =====================================================

    @Test
    @DisplayName("BLL-04 已锁定房间 - 新增费用项被拦截")
    void lockedRoom_addItem_throwsException() {
        // given
        BillingRules rules = buildLockedRules();

        // when / then
        BusinessException ex = assertThrows(BusinessException.class, () -> {
            BillingItem item = buildItem(FeeType.WATER, ChargeType.FIXED,
                    new BigDecimal("30.00"), ChargeCycle.MONTHLY);
            domainService.configureNewItem(rules, item);
        });
        assertEquals("B120", ex.getCode());
    }

    @Test
    @DisplayName("BLL-05 已锁定房间 - 编辑费用项被拦截")
    void lockedRoom_updateItem_throwsException() {
        BillingRules rules = buildLockedRules();
        BusinessException ex = assertThrows(BusinessException.class, () -> {
            BillingItem updated = new BillingItem();
            updated.setChargeValue(new BigDecimal("3000.00"));
            domainService.updateExistingItem(rules, FeeType.RENT, updated);
        });
        assertEquals("B120", ex.getCode());
    }

    @Test
    @DisplayName("BLL-06 已锁定房间 - 删除费用项被拦截")
    void lockedRoom_deleteItem_throwsException() {
        BillingRules rules = buildLockedRules();
        BusinessException ex = assertThrows(BusinessException.class, () ->
                domainService.removeExistingItem(rules, FeeType.WATER));
        assertEquals("B120", ex.getCode());
    }

    // =====================================================
    // 同类型唯一性校验 - R-002 防重复建单
    // =====================================================

    @Test
    @DisplayName("BLL-07 同房间同费用类型重复添加 - 被拦截")
    void duplicateFeeType_throwsException() {
        // given
        BillingRules rules = buildUnlockedRules(); // 已含 RENT
        BillingItem anotherRent = buildItem(FeeType.RENT, ChargeType.FIXED,
                new BigDecimal("2000.00"), ChargeCycle.MONTHLY);

        // when / then
        BusinessException ex = assertThrows(BusinessException.class, () ->
                domainService.configureNewItem(rules, anotherRent));
        assertEquals("B101", ex.getCode());
    }

    // =====================================================
    // 系统固定项不可删除
    // =====================================================

    @Test
    @DisplayName("BLL-08 删除租金 - 被拦截(系统固定项)")
    void deleteRent_throwsException() {
        BillingRules rules = buildUnlockedRules();
        BusinessException ex = assertThrows(BusinessException.class, () ->
                domainService.removeExistingItem(rules, FeeType.RENT));
        assertEquals("B110", ex.getCode());
    }

    @Test
    @DisplayName("BLL-09 删除押金 - 被拦截(系统固定项)")
    void deleteDeposit_throwsException() {
        BillingRules rules = buildUnlockedRules();
        BusinessException ex = assertThrows(BusinessException.class, () ->
                domainService.removeExistingItem(rules, FeeType.DEPOSIT));
        assertEquals("B110", ex.getCode());
    }

    // =====================================================
    // 金额/比例边界校验
    // =====================================================

    @Test
    @DisplayName("BLL-10 固定金额超出 999,999.99 - 被拦截")
    void amountExceedsUpperLimit_throwsException() {
        BillingRules rules = buildUnlockedRules();
        BillingItem item = buildItem(FeeType.WATER, ChargeType.FIXED,
                new BigDecimal("1000000.00"), ChargeCycle.MONTHLY);
        BusinessException ex = assertThrows(BusinessException.class, () ->
                domainService.configureNewItem(rules, item));
        assertEquals("B103", ex.getCode());
    }

    @Test
    @DisplayName("BLL-11 比例分摊值超出 1.0 - 被拦截")
    void ratioExceedsUpperLimit_throwsException() {
        BillingRules rules = buildUnlockedRules();
        BillingItem item = buildItem(FeeType.WATER, ChargeType.RATIO,
                new BigDecimal("1.5"), ChargeCycle.MONTHLY);
        BusinessException ex = assertThrows(BusinessException.class, () ->
                domainService.configureNewItem(rules, item));
        assertEquals("B104", ex.getCode());
    }

    @Test
    @DisplayName("BLL-12 金额为 0 - 允许(可用于免除费用场景)")
    void amountZero_allowed() {
        BillingRules rules = buildUnlockedRules();
        BillingItem item = buildItem(FeeType.WATER, ChargeType.FIXED,
                BigDecimal.ZERO, ChargeCycle.MONTHLY);
        rules = domainService.configureNewItem(rules, item);
        assertTrue(rules.containsFeeType(FeeType.WATER));
    }

    @Test
    @DisplayName("BLL-13 金额为负值 - 被拦截")
    void amountNegative_throwsException() {
        BillingRules rules = buildUnlockedRules();
        BillingItem item = buildItem(FeeType.WATER, ChargeType.FIXED,
                new BigDecimal("-10.00"), ChargeCycle.MONTHLY);
        BusinessException ex = assertThrows(BusinessException.class, () ->
                domainService.configureNewItem(rules, item));
        assertEquals("B103", ex.getCode());
    }

    // =====================================================
    // 签约锁机 + 生成快照
    // =====================================================

    @Test
    @DisplayName("BLL-14 签约锁定并生成快照 - 成功")
    void lockAndCreateSnapshot_success() {
        // given
        BillingRules rules = buildUnlockedRules(); // 含 RENT + DEPOSIT
        String contractId = "contract-001";

        // when
        BillingRulesSnapshot snapshot = domainService.lockAndCreateSnapshot(rules, contractId);

        // then
        assertTrue(rules.isLocked()); // 计费规则已锁定
        assertNotNull(rules.getLockedAt());
        assertEquals(contractId, snapshot.getContractId());
        assertEquals(2, snapshot.getRulesJson().size()); // RENT + DEPOSIT
        // 快照内容与锁定时一致
        assertEquals(rules.getItems().size(), snapshot.getRulesJson().size());
    }

    @Test
    @DisplayName("BLL-15 缺少租金配置 - 无法签约")
    void missingRent_cannotLock() {
        BillingRules rules = new BillingRules();
        rules.initialize("room-001");
        // 手动只添加押金 (不添加租金)
        BillingItem deposit = buildItem(FeeType.DEPOSIT, ChargeType.FIXED,
                new BigDecimal("2000.00"), null);
        rules.addItem(deposit);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                domainService.lockAndCreateSnapshot(rules, "contract-002"));
        assertEquals("C004", ex.getCode()); // 合约相关错误: 计费规则不完整
    }

    // =====================================================
    // 工具方法
    // =====================================================

    private BillingRules buildUnlockedRules() {
        BillingRules rules = new BillingRules();
        rules.initialize("room-001");
        // 系统默认: RENT
        BillingItem rent = buildItem(FeeType.RENT, ChargeType.FIXED,
                new BigDecimal("2000.00"), ChargeCycle.MONTHLY);
        rules.addItem(rent);
        // 系统默认: DEPOSIT
        BillingItem deposit = buildItem(FeeType.DEPOSIT, ChargeType.FIXED,
                new BigDecimal("2000.00"), null); // 押金无周期
        rules.addItem(deposit);
        return rules;
    }

    private BillingRules buildLockedRules() {
        BillingRules rules = buildUnlockedRules();
        // 添加水费(便于测试删除)
        BillingItem water = buildItem(FeeType.WATER, ChargeType.FIXED,
                new BigDecimal("30.00"), ChargeCycle.MONTHLY);
        rules.addItem(water);
        // 锁定
        rules.lock();
        return rules;
    }

    private BillingItem buildItem(FeeType feeType, ChargeType chargeType,
                                   BigDecimal chargeValue, ChargeCycle chargeCycle) {
        BillingItem item = new BillingItem();
        item.setFeeType(feeType);
        item.setChargeType(chargeType);
        item.setChargeValue(chargeValue);
        item.setChargeCycle(chargeCycle);
        item.setRemark("测试备注");
        item.normalizeByFeeType();
        return item;
    }
}
