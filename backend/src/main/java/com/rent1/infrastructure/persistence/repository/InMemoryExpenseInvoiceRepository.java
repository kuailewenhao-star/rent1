package com.rent1.infrastructure.persistence.repository;

import com.rent1.domain.invoice.entity.ExpenseInvoice;
import com.rent1.domain.invoice.repository.ExpenseInvoiceRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 支出账单仓储实现（内存实现，简化版）
 */
@Repository
public class InMemoryExpenseInvoiceRepository implements ExpenseInvoiceRepository {

    private final ConcurrentHashMap<String, ExpenseInvoice> store = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    @Override
    public Optional<ExpenseInvoice> findById(String expenseId) {
        return Optional.ofNullable(store.get(expenseId));
    }

    @Override
    public List<ExpenseInvoice> findByHouseSourceId(String houseSourceId) {
        return store.values().stream()
                .filter(e -> houseSourceId.equals(e.getHouseSourceId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<ExpenseInvoice> findByLandlordMemberId(String landlordMemberId) {
        return store.values().stream()
                .filter(e -> landlordMemberId.equals(e.getLandlordMemberId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<ExpenseInvoice> findByLandlordMemberIdAndCostDateBetween(String landlordMemberId,
                                                                          LocalDate from, LocalDate to) {
        return store.values().stream()
                .filter(e -> landlordMemberId.equals(e.getLandlordMemberId()))
                .filter(e -> e.getCostDate() != null
                        && !e.getCostDate().isBefore(from)
                        && !e.getCostDate().isAfter(to))
                .collect(Collectors.toList());
    }

    @Override
    public List<ExpenseInvoice> findByHouseSourceIdAndCostDateBetween(String houseSourceId,
                                                                      String landlordMemberId,
                                                                      LocalDate from, LocalDate to) {
        return store.values().stream()
                .filter(e -> houseSourceId.equals(e.getHouseSourceId()))
                .filter(e -> landlordMemberId == null || landlordMemberId.equals(e.getLandlordMemberId()))
                .filter(e -> e.getCostDate() != null
                        && !e.getCostDate().isBefore(from)
                        && !e.getCostDate().isAfter(to))
                .collect(Collectors.toList());
    }

    @Override
    public BigDecimal sumAmountByLandlordMemberIdAndCostDateBetween(String landlordMemberId,
                                                                     LocalDate from, LocalDate to) {
        return store.values().stream()
                .filter(e -> landlordMemberId.equals(e.getLandlordMemberId()))
                .filter(e -> e.getCostDate() != null
                        && !e.getCostDate().isBefore(from)
                        && !e.getCostDate().isAfter(to))
                .map(ExpenseInvoice::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public ExpenseInvoice save(ExpenseInvoice expense) {
        if (expense.getExpenseId() == null) {
            expense.setExpenseId("EXP_" + idCounter.getAndIncrement());
        }
        store.put(expense.getExpenseId(), expense);
        return expense;
    }

    @Override
    public ExpenseInvoice update(ExpenseInvoice expense) {
        store.put(expense.getExpenseId(), expense);
        return expense;
    }

    @Override
    public void delete(String expenseId) {
        store.remove(expenseId);
    }
}
