package com.rent1.infrastructure.persistence.repository;

import com.rent1.domain.deposit.DepositRecord;
import com.rent1.domain.deposit.DepositRepository;
import com.rent1.domain.deposit.DepositStatus;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 押金仓储实现（内存实现，简化版）
 * 实际生产环境应使用MySQL/MyBatis实现
 */
@Repository
public class InMemoryDepositRepository implements DepositRepository {
    
    private final ConcurrentHashMap<String, DepositRecord> store = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);
    
    @Override
    public DepositRecord save(DepositRecord depositRecord) {
        if (depositRecord.getRecordId() == null) {
            depositRecord.setRecordId("DEP_" + idCounter.getAndIncrement());
        }
        store.put(depositRecord.getRecordId(), depositRecord);
        return depositRecord;
    }
    
    @Override
    public Optional<DepositRecord> findById(String recordId) {
        return Optional.ofNullable(store.get(recordId));
    }
    
    @Override
    public Optional<DepositRecord> findByContractId(String contractId) {
        return store.values().stream()
            .filter(dep -> contractId.equals(dep.getContractId()))
            .findFirst();
    }
    
    @Override
    public BigDecimal sumValidDepositByLandlord(String landlordMemberId) {
        return store.values().stream()
            .filter(dep -> landlordMemberId.equals(dep.getLandlordMemberId()))
            .filter(DepositRecord::isValidHeldDeposit)
            .map(DepositRecord::getOriginalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    @Override
    public int countValidDepositByLandlord(String landlordMemberId) {
        return (int) store.values().stream()
            .filter(dep -> landlordMemberId.equals(dep.getLandlordMemberId()))
            .filter(DepositRecord::isValidHeldDeposit)
            .count();
    }
    
    @Override
    public BigDecimal sumValidDepositByTenant(String tenantMemberId) {
        return store.values().stream()
            .filter(dep -> tenantMemberId.equals(dep.getTenantMemberId()))
            .filter(DepositRecord::isValidHeldDeposit)
            .map(DepositRecord::getOriginalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    @Override
    public List<DepositRecord> findAllByLandlord(String landlordMemberId) {
        return store.values().stream()
            .filter(dep -> landlordMemberId.equals(dep.getLandlordMemberId()))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<DepositRecord> findAllByTenant(String tenantMemberId) {
        return store.values().stream()
            .filter(dep -> tenantMemberId.equals(dep.getTenantMemberId()))
            .collect(Collectors.toList());
    }
    
    @Override
    public Optional<DepositRecord> findByInvoiceId(String invoiceId) {
        return store.values().stream()
            .filter(dep -> invoiceId != null && invoiceId.equals(dep.getInvoiceId()))
            .findFirst();
    }
}
