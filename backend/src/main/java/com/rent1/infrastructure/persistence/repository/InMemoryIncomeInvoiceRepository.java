package com.rent1.infrastructure.persistence.repository;

import com.rent1.domain.invoice.entity.IncomeInvoice;
import com.rent1.domain.invoice.enums.FeeType;
import com.rent1.domain.invoice.enums.InvoiceStatus;
import com.rent1.domain.invoice.repository.IncomeInvoiceRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 收入账单仓储实现（内存实现，简化版）
 */
@Repository
public class InMemoryIncomeInvoiceRepository implements IncomeInvoiceRepository {
    
    private final ConcurrentHashMap<String, IncomeInvoice> store = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);
    
    @Override
    public Optional<IncomeInvoice> findById(String invoiceId) {
        return Optional.ofNullable(store.get(invoiceId));
    }
    
    @Override
    public List<IncomeInvoice> findByContractId(String contractId) {
        return store.values().stream()
            .filter(inv -> contractId.equals(inv.getContractId()))
            .collect(Collectors.toList());
    }

    @Override
    public List<IncomeInvoice> findByRoomId(String roomId) {
        return store.values().stream()
            .filter(inv -> roomId.equals(inv.getRoomId()))
            .collect(Collectors.toList());
    }

    @Override
    public List<IncomeInvoice> findByTenantMemberId(String tenantMemberId) {
        return store.values().stream()
            .filter(inv -> tenantMemberId.equals(inv.getTenantMemberId()))
            .collect(Collectors.toList());
    }

    @Override
    public List<IncomeInvoice> findByLandlordMemberId(String landlordMemberId) {
        return store.values().stream()
            .filter(inv -> landlordMemberId.equals(inv.getLandlordMemberId()))
            .collect(Collectors.toList());
    }
    
    @Override
    public Optional<IncomeInvoice> findDepositByContractId(String contractId) {
        return store.values().stream()
            .filter(inv -> contractId.equals(inv.getContractId()))
            .filter(inv -> inv.getFeeType() == FeeType.DEPOSIT)
            .findFirst();
    }
    
    @Override
    public List<IncomeInvoice> findByRoomIdAndFeeType(String roomId, FeeType feeType) {
        return store.values().stream()
            .filter(inv -> roomId.equals(inv.getRoomId()))
            .filter(inv -> inv.getFeeType() == feeType)
            .collect(Collectors.toList());
    }

    @Override
    public List<IncomeInvoice> findByHouseSourceId(String houseSourceId) {
        return store.values().stream()
            .filter(inv -> houseSourceId.equals(inv.getHouseSourceId()))
            .filter(inv -> inv.getStatus() != InvoiceStatus.VOID)
            .collect(Collectors.toList());
    }

    @Override
    public List<IncomeInvoice> findByLandlordMemberIdAndCycleIntersect(String landlordMemberId,
                                                                        LocalDate from, LocalDate to) {
        return store.values().stream()
                .filter(inv -> landlordMemberId.equals(inv.getLandlordMemberId()))
                .filter(inv -> inv.getStatus() != InvoiceStatus.VOID)
                .filter(inv -> matchesPeriod(inv, from, to))
                .collect(Collectors.toList());
    }

    private boolean matchesPeriod(IncomeInvoice inv, LocalDate from, LocalDate to) {
        LocalDate start = inv.getCycleStart();
        LocalDate end = inv.getCycleEnd();
        if (start == null && end == null) {
            // 押金账单，不跟随周期
            return true;
        }
        LocalDate s = start != null ? start : end;
        LocalDate e = end != null ? end : start;
        return !s.isAfter(to) && !e.isBefore(from);
    }
    
    @Override
    public IncomeInvoice findByIdDirect(String invoiceId) {
        return store.get(invoiceId);
    }

    @Override
    public List<IncomeInvoice> findByFeeTypeAndStatusIn(FeeType feeType, List<InvoiceStatus> statuses) {
        return store.values().stream()
            .filter(inv -> inv.getFeeType() == feeType)
            .filter(inv -> statuses != null && statuses.contains(inv.getStatus()))
            .collect(Collectors.toList());
    }

    @Override
    public IncomeInvoice save(IncomeInvoice invoice) {
        if (invoice.getInvoiceId() == null) {
            invoice.setInvoiceId("INV_" + idCounter.getAndIncrement());
        }
        store.put(invoice.getInvoiceId(), invoice);
        return invoice;
    }
    
    @Override
    public IncomeInvoice update(IncomeInvoice invoice) {
        store.put(invoice.getInvoiceId(), invoice);
        return invoice;
    }
}
