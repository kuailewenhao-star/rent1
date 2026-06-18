package com.rent1.infrastructure.persistence.repository;

import com.rent1.domain.contract.entity.ContractBillingRulesSnapshot;
import com.rent1.domain.contract.repository.ContractBillingRulesSnapshotRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 计费规则快照仓储实现（内存实现）
 */
@Repository
public class InMemoryContractBillingRulesSnapshotRepository implements ContractBillingRulesSnapshotRepository {

    private final ConcurrentHashMap<String, ContractBillingRulesSnapshot> store = new ConcurrentHashMap<>();

    @Override
    public Optional<ContractBillingRulesSnapshot> findById(String snapshotId) {
        return Optional.ofNullable(store.get(snapshotId));
    }

    @Override
    public Optional<ContractBillingRulesSnapshot> findByContractId(String contractId) {
        return store.values().stream()
            .filter(s -> contractId.equals(s.getContractId()))
            .findFirst();
    }

    @Override
    public ContractBillingRulesSnapshot save(ContractBillingRulesSnapshot snapshot) {
        if (snapshot.getSnapshotId() == null) {
            snapshot.setSnapshotId("SNAP_" + System.currentTimeMillis());
        }
        store.put(snapshot.getSnapshotId(), snapshot);
        return snapshot;
    }

    @Override
    public void delete(String snapshotId) {
        store.remove(snapshotId);
    }
}
