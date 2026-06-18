package com.rent1.application.service;

import com.rent1.domain.contract.entity.Contract;
import com.rent1.domain.contract.entity.ContractBillingRulesSnapshot;
import com.rent1.domain.contract.repository.ContractRepository;
import com.rent1.domain.invoice.entity.IncomeInvoice;
import com.rent1.domain.invoice.entity.ExpenseInvoice;
import com.rent1.domain.invoice.repository.IncomeInvoiceRepository;
import com.rent1.domain.invoice.service.InvoiceDomainService;
import com.rent1.domain.invoice.service.ExpenseInvoiceDomainService;
import com.rent1.domain.invoice.event.InvoiceGeneratedEvent;
import com.rent1.domain.invoice.event.InvoicePaidEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 收入账单应用服务 - 业务流程编排
 * 职责：
 * 1. 编排账单生成、核销、逾期管理流程
 * 2. 跨领域调度（合约域、房源域、消息域）
 * 3. 事务控制
 * 禁止行为：禁止定义计费规则细节、禁止定义计费计算逻辑
 */
@Slf4j
@Service
public class IncomeInvoiceAppService {

    private final InvoiceDomainService invoiceDomainService;
    private final IncomeInvoiceRepository incomeInvoiceRepository;
    private final ContractRepository contractRepository;

    @Autowired
    public IncomeInvoiceAppService(InvoiceDomainService invoiceDomainService,
                                   IncomeInvoiceRepository incomeInvoiceRepository,
                                   ContractRepository contractRepository) {
        this.invoiceDomainService = invoiceDomainService;
        this.incomeInvoiceRepository = incomeInvoiceRepository;
        this.contractRepository = contractRepository;
    }

    /**
     * 合约创建时自动生成首期账单（租金+押金）
     * Saga编排流程：
     * 1. 调用账单域服务生成首期账单
     * 2. 发布InvoiceGeneratedEvent
     *
     * @param contract 合约实体
     * @param billingRulesSnapshot 计费规则快照
     * @return 首期账单列表
     */
    @Transactional(rollbackFor = Exception.class)
    public List<IncomeInvoice> generateFirstInvoice(Contract contract, ContractBillingRulesSnapshot billingRulesSnapshot) {
        log.info("开始生成首期账单: contractId={}", contract.getContractId());

        // 调用领域服务生成账单
        List<IncomeInvoice> invoices = invoiceDomainService.generateFirstInvoice(contract, billingRulesSnapshot);

        // 发布账单生成事件（用于消息通知）
        for (IncomeInvoice invoice : invoices) {
            InvoiceGeneratedEvent event = buildInvoiceGeneratedEvent(invoice);
            // TODO: 发布事件到事件总线
            log.info("发布账单生成事件: invoiceId={}, feeType={}", invoice.getInvoiceId(), invoice.getFeeType());
        }

        log.info("首期账单生成完成: contractId={}, invoiceCount={}", contract.getContractId(), invoices.size());
        return invoices;
    }

    /**
     * 房东手动录入杂费账单（房间维度）
     *
     * @param contract 合约实体
     * @param feeType 费用类型
     * @param amount 金额
     * @param billMonth 结算月份
     * @param remark 备注
     * @return 杂费账单
     */
    @Transactional(rollbackFor = Exception.class)
    public IncomeInvoice manualCreate(Contract contract, String feeType, BigDecimal amount,
                                      String billMonth, String remark) {
        log.info("手动录入杂费账单: contractId={}, feeType={}, amount={}",
                 contract.getContractId(), feeType, amount);

        // 调用领域服务创建账单
        IncomeInvoice invoice = invoiceDomainService.createManualInvoice(contract, feeType, amount, billMonth, remark);

        // 发布账单生成事件（杂费仅单次提醒）
        InvoiceGeneratedEvent event = buildInvoiceGeneratedEvent(invoice);
        // TODO: 发布事件到事件总线

        log.info("杂费账单录入完成: invoiceId={}", invoice.getInvoiceId());
        return invoice;
    }

    /**
     * 房源维度公摊费用录入与自动分摊
     *
     * @param contracts 在租合约列表
     * @param feeType 费用类型
     * @param totalAmount 总费用
     * @param billMonth 结算月份
     * @return 分摊后的账单列表
     */
    @Transactional(rollbackFor = Exception.class)
    public List<IncomeInvoice> createSharedInvoice(List<Contract> contracts, String feeType,
                                                   BigDecimal totalAmount, String billMonth) {
        log.info("公摊费用录入: feeType={}, totalAmount={}, contractCount={}",
                 feeType, totalAmount, contracts.size());

        // 调用领域服务分摊生成账单
        List<IncomeInvoice> invoices = invoiceDomainService.splitByRatio(contracts, feeType, totalAmount, billMonth);

        // 发布账单生成事件
        for (IncomeInvoice invoice : invoices) {
            InvoiceGeneratedEvent event = buildInvoiceGeneratedEvent(invoice);
            // TODO: 发布事件到事件总线
        }

        log.info("公摊费用分摊完成: invoiceCount={}", invoices.size());
        return invoices;
    }

    /**
     * 账单核销（已支付状态变更）
     *
     * @param invoice 账单实体
     * @param paidTime 实际支付时间（可选）
     */
    @Transactional(rollbackFor = Exception.class)
    public void verifyPayment(IncomeInvoice invoice, LocalDateTime paidTime) {
        log.info("账单核销: invoiceId={}", invoice.getInvoiceId());

        // 调用领域服务核销账单
        LocalDateTime actualPaidTime = paidTime != null ? paidTime : LocalDateTime.now();
        invoiceDomainService.markAsPaid(invoice, actualPaidTime);

        // 发布账单核销事件（停止催收提醒）
        InvoicePaidEvent event = buildInvoicePaidEvent(invoice, actualPaidTime);
        // TODO: 发布事件到事件总线

        log.info("账单核销完成: invoiceId={}, status={}", invoice.getInvoiceId(), invoice.getStatus());
    }

    /**
     * 合约退租/解约时截断后续账单
     *
     * @param contract 合约实体
     */
    @Transactional(rollbackFor = Exception.class)
    public void truncateFutureBills(Contract contract) {
        log.info("截断后续账单: contractId={}", contract.getContractId());

        // 调用领域服务截断账单
        invoiceDomainService.truncateFutureBills(contract);

        log.info("后续账单截断完成: contractId={}", contract.getContractId());
    }

    /**
     * 按租客+条件查询收入账单列表（支持 status/feeType/billMonth 过滤 + 分页）
     */
    @Transactional(readOnly = true)
    public List<IncomeInvoice> queryByTenant(String tenantMemberId, String status,
                                              String feeType, String billMonth,
                                              int page, int pageSize) {
        log.info("租客查询收入账单列表: tenantMemberId={}, status={}, feeType={}, billMonth={}",
                 tenantMemberId, status, feeType, billMonth);

        List<IncomeInvoice> invoices = incomeInvoiceRepository.findByTenantMemberId(tenantMemberId);

        if (status != null && !status.isEmpty()) {
            final String s = status;
            invoices = invoices.stream()
                    .filter(inv -> inv.getStatus() != null && inv.getStatus().name().equals(s))
                    .collect(Collectors.toList());
        }

        if (feeType != null && !feeType.isEmpty()) {
            final String ft = feeType;
            invoices = invoices.stream()
                    .filter(inv -> inv.getFeeType() != null && inv.getFeeType().getCode().equals(ft))
                    .collect(Collectors.toList());
        }

        if (billMonth != null && !billMonth.isEmpty()) {
            LocalDate monthStart = LocalDate.parse(billMonth + "-01");
            LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
            invoices = invoices.stream()
                    .filter(inv -> inv.getCycleStart() != null
                            && !inv.getCycleStart().isBefore(monthStart)
                            && !inv.getCycleStart().isAfter(monthEnd))
                    .collect(Collectors.toList());
        }

        // 分页
        int from = (page - 1) * pageSize;
        int to = Math.min(from + pageSize, invoices.size());
        if (from >= invoices.size()) {
            return List.of();
        }
        return invoices.subList(from, to);
    }

    /**
     * 按房东+条件查询收入账单列表（支持 status/roomId/feeType/billMonth 过滤 + 分页）
     */
    @Transactional(readOnly = true)
    public List<IncomeInvoice> queryByLandlord(String landlordMemberId, String status,
                                                String roomId, String feeType,
                                                String billMonth, int page, int pageSize) {
        log.info("查询收入账单列表: landlordMemberId={}, roomId={}", landlordMemberId, roomId);

        List<IncomeInvoice> invoices;

        if (roomId != null && !roomId.isEmpty()) {
            invoices = incomeInvoiceRepository.findByRoomId(roomId);
        } else {
            invoices = incomeInvoiceRepository.findByLandlordMemberId(landlordMemberId);
        }

        // 状态过滤
        if (status != null && !status.isEmpty()) {
            final String s = status;
            invoices = invoices.stream()
                    .filter(inv -> inv.getStatus() != null && inv.getStatus().name().equals(s))
                    .collect(Collectors.toList());
        }

        // 费用类型过滤
        if (feeType != null && !feeType.isEmpty()) {
            final String ft = feeType;
            invoices = invoices.stream()
                    .filter(inv -> inv.getFeeType() != null && inv.getFeeType().getCode().equals(ft))
                    .collect(Collectors.toList());
        }

        // 结算月份过滤（cycleStart 落在指定月份内）
        if (billMonth != null && !billMonth.isEmpty()) {
            LocalDate monthStart = LocalDate.parse(billMonth + "-01");
            LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
            invoices = invoices.stream()
                    .filter(inv -> inv.getCycleStart() != null
                            && !inv.getCycleStart().isBefore(monthStart)
                            && !inv.getCycleStart().isAfter(monthEnd))
                    .collect(Collectors.toList());
        }

        // 分页
        int from = (page - 1) * pageSize;
        int to = Math.min(from + pageSize, invoices.size());
        if (from >= invoices.size()) {
            return List.of();
        }
        return invoices.subList(from, to);
    }

    /**
     * 构建账单生成事件
     */
    private InvoiceGeneratedEvent buildInvoiceGeneratedEvent(IncomeInvoice invoice) {
        return InvoiceGeneratedEvent.builder()
            .invoiceId(invoice.getInvoiceId())
            .contractId(invoice.getContractId())
            .tenantMemberId(invoice.getTenantMemberId())
            .landlordMemberId(invoice.getLandlordMemberId())
            .roomId(invoice.getRoomId())
            .houseSourceId(invoice.getHouseSourceId())
            .feeType(invoice.getFeeType().getCode())
            .amount(invoice.getAmount())
            .cycleStart(invoice.getCycleStart())
            .cycleEnd(invoice.getCycleEnd())
            .dueDate(invoice.getDueDate())
            .isManual(invoice.getIsManual())
            .build();
    }

    /**
     * 构建账单核销事件
     */
    private InvoicePaidEvent buildInvoicePaidEvent(IncomeInvoice invoice, LocalDateTime paidTime) {
        return InvoicePaidEvent.builder()
            .invoiceId(invoice.getInvoiceId())
            .contractId(invoice.getContractId())
            .tenantMemberId(invoice.getTenantMemberId())
            .landlordMemberId(invoice.getLandlordMemberId())
            .roomId(invoice.getRoomId())
            .houseSourceId(invoice.getHouseSourceId())
            .feeType(invoice.getFeeType().getCode())
            .paidAmount(invoice.getAmount())
            .paidTime(paidTime)
            .build();
    }
}
