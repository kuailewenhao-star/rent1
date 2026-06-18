package com.rent1.domain.invoice.repository;

import com.rent1.domain.invoice.entity.IncomeInvoice;
import com.rent1.domain.invoice.enums.FeeType;
import com.rent1.domain.invoice.enums.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 收入账单仓储接口
 */
public interface IncomeInvoiceRepository {
    
    /**
     * 根据ID查询账单
     */
    Optional<IncomeInvoice> findById(String invoiceId);
    
    /**
     * 根据合约ID查询账单列表
     */
    List<IncomeInvoice> findByContractId(String contractId);
    
    /**
     * 根据房间ID查询账单列表
     */
    List<IncomeInvoice> findByRoomId(String roomId);
    
    /**
     * 根据租客ID查询账单列表
     */
    List<IncomeInvoice> findByTenantMemberId(String tenantMemberId);
    
    /**
     * 根据房东ID查询账单列表
     */
    List<IncomeInvoice> findByLandlordMemberId(String landlordMemberId);
    
    /**
     * 查询押金账单（特定合约的DEPOSIT类型账单）
     */
    Optional<IncomeInvoice> findDepositByContractId(String contractId);
    
    /**
     * 根据费用类型和房间查询账单
     */
    List<IncomeInvoice> findByRoomIdAndFeeType(String roomId, FeeType feeType);

    /**
     * 根据房源ID查询账单列表（数据域统计专用）
     */
    List<IncomeInvoice> findByHouseSourceId(String houseSourceId);

    /**
     * 根据房东ID + 计费周期范围查询（数据域统计专用）
     */
    List<IncomeInvoice> findByLandlordMemberIdAndCycleIntersect(String landlordMemberId,
                                                                  LocalDate from, LocalDate to);
    
    /**
     * 保存账单
     */
    IncomeInvoice save(IncomeInvoice invoice);
    
    /**
     * 更新账单
     */
    IncomeInvoice update(IncomeInvoice invoice);

    /**
     * 根据费用类型和状态列表查询账单
     */
    List<IncomeInvoice> findByFeeTypeAndStatusIn(FeeType feeType, List<InvoiceStatus> statuses);

    /**
     * 根据ID查询账单（非Optional版本）
     */
    IncomeInvoice findByIdDirect(String invoiceId);
}
