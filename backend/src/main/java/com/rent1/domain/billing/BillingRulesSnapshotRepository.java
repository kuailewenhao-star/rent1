package com.rent1.domain.billing;

import com.rent1.domain.billing.entity.BillingRulesSnapshot;

import java.util.Optional;

/**
 * 计费规则快照仓储接口
 */
public interface BillingRulesSnapshotRepository {

    /**
     * 按合约ID查找快照（1合约对应1快照）
     */
    Optional<BillingRulesSnapshot> findByContractId(String contractId);

    /**
     * 保存新的快照
     */
    void save(BillingRulesSnapshot snapshot);
}
