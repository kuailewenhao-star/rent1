package com.rent1.domain.contract.repository;

import com.rent1.domain.contract.entity.ContractBillingRulesSnapshot;

import java.util.Optional;

/**
 * 计费规则快照仓储接口
 */
public interface ContractBillingRulesSnapshotRepository {

    /**
     * 根据快照ID查询
     */
    Optional<ContractBillingRulesSnapshot> findById(String snapshotId);

    /**
     * 根据合约ID查询快照
     */
    Optional<ContractBillingRulesSnapshot> findByContractId(String contractId);

    /**
     * 保存快照
     */
    ContractBillingRulesSnapshot save(ContractBillingRulesSnapshot snapshot);

    /**
     * 删除快照（通常不删除，快照永久保留）
     */
    void delete(String snapshotId);
}
