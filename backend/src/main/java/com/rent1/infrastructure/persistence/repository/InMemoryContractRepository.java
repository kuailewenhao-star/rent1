package com.rent1.infrastructure.persistence.repository;

import com.rent1.domain.common.ContractStatus;
import com.rent1.domain.contract.entity.Contract;
import com.rent1.domain.contract.repository.ContractRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 合约仓储实现（内存实现，简化版）
 * 实际生产环境应使用MySQL/JPA实现
 */
@Repository
public class InMemoryContractRepository implements ContractRepository {

    // 简化：内存存储，实际应使用数据库
    private final ConcurrentHashMap<String, Contract> store = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    @Override
    public Optional<Contract> findById(String contractId) {
        return Optional.ofNullable(store.get(contractId));
    }

    @Override
    public Optional<Contract> findActiveByRoomId(String roomId) {
        return store.values().stream()
            .filter(c -> roomId.equals(c.getRoomId()))
            .filter(c -> c.getStatus() == ContractStatus.ACTIVE)
            .findFirst();
    }

    @Override
    public List<Contract> findByRoomId(String roomId) {
        return store.values().stream()
            .filter(c -> roomId.equals(c.getRoomId()))
            .collect(Collectors.toList());
    }

    @Override
    public List<Contract> findByTenantMemberId(String tenantMemberId) {
        return store.values().stream()
            .filter(c -> tenantMemberId.equals(c.getTenantMemberId()))
            .collect(Collectors.toList());
    }

    @Override
    public List<Contract> findByLandlordMemberId(String landlordMemberId) {
        return store.values().stream()
            .filter(c -> landlordMemberId.equals(c.getLandlordMemberId()))
            .collect(Collectors.toList());
    }

    @Override
    public List<Contract> findByLandlordMemberIdAndStatus(String landlordMemberId, ContractStatus status) {
        return store.values().stream()
            .filter(c -> landlordMemberId.equals(c.getLandlordMemberId()))
            .filter(c -> c.getStatus() == status)
            .collect(Collectors.toList());
    }

    @Override
    public List<Contract> findByLandlordMemberIdAndStatusIn(String landlordMemberId, List<ContractStatus> statuses) {
        return store.values().stream()
            .filter(c -> landlordMemberId.equals(c.getLandlordMemberId()))
            .filter(c -> statuses != null && statuses.contains(c.getStatus()))
            .collect(Collectors.toList());
    }

    @Override
    public List<Contract> findByTenantMemberIdAndStatus(String tenantMemberId, ContractStatus status) {
        return store.values().stream()
            .filter(c -> tenantMemberId.equals(c.getTenantMemberId()))
            .filter(c -> c.getStatus() == status)
            .collect(Collectors.toList());
    }

    @Override
    public List<Contract> findExpiredContracts(LocalDate date) {
        return store.values().stream()
            .filter(c -> c.getStatus() == ContractStatus.ACTIVE)
            .filter(c -> !c.getEndDate().isAfter(date))
            .collect(Collectors.toList());
    }

    @Override
    public Contract findByIdDirect(String contractId) {
        return store.get(contractId);
    }

    @Override
    public List<Contract> findByStatus(ContractStatus status) {
        return store.values().stream()
            .filter(c -> c.getStatus() == status)
            .collect(Collectors.toList());
    }

    @Override
    public Contract save(Contract contract) {
        if (contract.getContractId() == null) {
            contract.setContractId("CON_" + idCounter.getAndIncrement());
        }
        store.put(contract.getContractId(), contract);
        return contract;
    }

    @Override
    public Contract update(Contract contract) {
        store.put(contract.getContractId(), contract);
        return contract;
    }

    @Override
    public void delete(String contractId) {
        store.remove(contractId);
    }
}
