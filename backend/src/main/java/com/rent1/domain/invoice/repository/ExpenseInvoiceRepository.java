package com.rent1.domain.invoice.repository;

import com.rent1.domain.invoice.entity.ExpenseInvoice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 支出账单仓储接口
 */
public interface ExpenseInvoiceRepository {

    Optional<ExpenseInvoice> findById(String expenseId);

    List<ExpenseInvoice> findByHouseSourceId(String houseSourceId);

    List<ExpenseInvoice> findByLandlordMemberId(String landlordMemberId);

    /**
     * 按房东ID+日期范围查询支出账单
     */
    List<ExpenseInvoice> findByLandlordMemberIdAndCostDateBetween(String landlordMemberId,
                                                                   LocalDate from, LocalDate to);

    /**
     * 按房源ID+日期范围查询支出账单
     */
    List<ExpenseInvoice> findByHouseSourceIdAndCostDateBetween(String houseSourceId, String landlordMemberId,
                                                                LocalDate from, LocalDate to);

    BigDecimal sumAmountByLandlordMemberIdAndCostDateBetween(String landlordMemberId,
                                                              LocalDate from, LocalDate to);

    ExpenseInvoice save(ExpenseInvoice expense);

    ExpenseInvoice update(ExpenseInvoice expense);

    void delete(String expenseId);
}
