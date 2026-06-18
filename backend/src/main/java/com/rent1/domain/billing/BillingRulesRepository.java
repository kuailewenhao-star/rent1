package com.rent1.domain.billing;

import com.rent1.domain.billing.entity.BillingRules;

import java.util.Optional;

/**
 * 计费规则仓储接口（领域层定义，由基础设施层实现）
 * 保持领域层对基础设施层的依赖倒置
 */
public interface BillingRulesRepository {

    /**
     * 按房间ID查找计费规则
     * 每房间仅对应1套计费规则（room_id唯一约束）
     */
    Optional<BillingRules> findByRoomId(String roomId);

    /**
     * 按rulesId查找
     */
    Optional<BillingRules> findById(String rulesId);

    /**
     * 保存新的计费规则
     */
    void save(BillingRules rules);

    /**
     * 更新计费规则
     */
    void update(BillingRules rules);
}
