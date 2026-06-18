package com.rent1.application.service;

import com.rent1.domain.invoice.entity.ExpenseInvoice;
import com.rent1.domain.invoice.repository.ExpenseInvoiceRepository;
import com.rent1.domain.invoice.service.ExpenseInvoiceDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 支出账单应用服务 - 房东成本支出台账管理流程编排
 * 职责：
 * 1. 编排支出账单创建、编辑、删除流程
 * 2. 跨领域调度（房源域）
 * 3. 事务控制
 * 禁止行为：禁止定义支出账单状态、禁止定义支出账单计算逻辑
 */
@Slf4j
@Service
public class ExpenseInvoiceAppService {

    private final ExpenseInvoiceRepository expenseInvoiceRepository;
    private final ExpenseInvoiceDomainService expenseInvoiceDomainService;

    @Autowired
    public ExpenseInvoiceAppService(ExpenseInvoiceRepository expenseInvoiceRepository,
                                    ExpenseInvoiceDomainService expenseInvoiceDomainService) {
        this.expenseInvoiceRepository = expenseInvoiceRepository;
        this.expenseInvoiceDomainService = expenseInvoiceDomainService;
    }

    /**
     * 房东新增支出账单
     * 
     * @param houseSourceId 房源ID
     * @param landlordMemberId 房东会员ID
     * @param costType 支出费用类型
     * @param amount 支出金额
     * @param costDate 支出发生时间
     * @param remark 备注
     * @return 支出账单实体
     */
    @Transactional(rollbackFor = Exception.class)
    public ExpenseInvoice create(String houseSourceId, String landlordMemberId,
                                String costType, BigDecimal amount, 
                                LocalDate costDate, String remark) {
        log.info("创建支出账单: houseSourceId={}, costType={}, amount={}", 
                 houseSourceId, costType, amount);
        
        // 调用领域服务创建支出账单
        ExpenseInvoice expense = expenseInvoiceDomainService.createExpenseRecord(
            houseSourceId, landlordMemberId, costType, amount, costDate, remark);
        
        log.info("支出账单创建完成: expenseId={}", expense.getExpenseId());
        return expense;
    }

    /**
     * 编辑支出账单
     * 
     * @param expense 支出账单实体
     * @param landlordMemberId 当前房东ID
     * @param amount 新金额
     * @param costDate 新支出时间
     * @param costType 新费用类型
     * @param remark 新备注
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(ExpenseInvoice expense, String landlordMemberId,
                      BigDecimal amount, LocalDate costDate, 
                      String costType, String remark) {
        log.info("更新支出账单: expenseId={}", expense.getExpenseId());
        
        // 调用领域服务更新支出账单
        expenseInvoiceDomainService.updateRecord(expense, landlordMemberId, amount, costDate, costType, remark);
        
        log.info("支出账单更新完成: expenseId={}", expense.getExpenseId());
    }

    /**
     * 删除支出账单
     * 
     * @param expense 支出账单实体
     * @param landlordMemberId 当前房东ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(ExpenseInvoice expense, String landlordMemberId) {
        log.info("删除支出账单: expenseId={}", expense.getExpenseId());
        
        // 调用领域服务删除支出账单
        expenseInvoiceDomainService.deleteRecord(expense, landlordMemberId);
        
        log.info("支出账单删除完成: expenseId={}", expense.getExpenseId());
    }

    /**
     * 查询支出账单列表（支持多条件组合过滤）
     */
    public List<ExpenseInvoice> query(String landlordMemberId, String houseSourceId,
                                       String costType, LocalDate startDate, LocalDate endDate) {
        log.info("查询支出账单列表: landlordMemberId={}, houseSourceId={}", landlordMemberId, houseSourceId);

        List<ExpenseInvoice> result;

        if (houseSourceId != null && !houseSourceId.isEmpty()) {
            if (startDate != null && endDate != null) {
                result = expenseInvoiceRepository.findByHouseSourceIdAndCostDateBetween(
                        houseSourceId, landlordMemberId, startDate, endDate);
            } else {
                result = expenseInvoiceRepository.findByHouseSourceId(houseSourceId);
            }
        } else if (startDate != null && endDate != null) {
            result = expenseInvoiceRepository.findByLandlordMemberIdAndCostDateBetween(
                    landlordMemberId, startDate, endDate);
        } else {
            result = expenseInvoiceRepository.findByLandlordMemberId(landlordMemberId);
        }

        // 费用类型过滤
        if (costType != null && !costType.isEmpty()) {
            final String ct = costType;
            result = result.stream()
                    .filter(e -> e.getCostType() != null && e.getCostType().getCode().equals(ct))
                    .collect(Collectors.toList());
        }

        return result;
    }
}