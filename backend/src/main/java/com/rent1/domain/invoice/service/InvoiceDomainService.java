package com.rent1.domain.invoice.service;

import com.rent1.domain.contract.entity.ContractBillingRulesSnapshot;
import com.rent1.domain.contract.entity.Contract;
import com.rent1.domain.invoice.entity.BillingRuleItem;
import com.rent1.domain.invoice.entity.IncomeInvoice;
import com.rent1.domain.invoice.enums.FeeType;
import com.rent1.domain.invoice.enums.InvoiceStatus;
import com.rent1.domain.common.BusinessException;
import com.rent1.common.enums.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 账单领域服务 - 收入账单核心业务规则
 * 核心业务规则：
 * 1. 合约创建时自动生成首期租金账单+押金账单
 * 2. 周期性账单按独立周期自动生成
 * 3. 防重复机制：同一房间、同一费用类型、同一计费周期仅生成唯一账单
 * 4. 账单核销与逾期管理
 * 5. 合约退租/解约时截断后续账单
 */
@Slf4j
@Service
public class InvoiceDomainService {

    /**
     * 合约创建时自动生成首期账单（租金+押金）
     * 核心业务规则：
     * 1. 从快照中获取租金RENT配置金额 → 生成首期租金账单
     * 2. 从快照中获取押金DEPOSIT配置金额 → 生成押金账单（一次性）
     * 3. 账单status=PENDING待支付
     * 
     * @param contract 合约实体
     * @param billingRulesSnapshot 计费规则快照
     * @return 首期账单列表（租金账单+押金账单）
     */
    public List<IncomeInvoice> generateFirstInvoice(Contract contract, ContractBillingRulesSnapshot billingRulesSnapshot) {
        List<IncomeInvoice> invoices = new ArrayList<>();
        
        // 解析计费规则JSON
        List<BillingRuleItem> ruleItems = parseBillingRules(billingRulesSnapshot.getRulesJson());
        
        // 生成租金账单
        BillingRuleItem rentRule = findRuleByFeeType(ruleItems, FeeType.RENT.getCode());
        if (rentRule != null) {
            IncomeInvoice rentInvoice = createRentInvoice(contract, rentRule);
            invoices.add(rentInvoice);
            log.info("生成首期租金账单: contractId={}, amount={}", contract.getContractId(), rentInvoice.getAmount());
        }
        
        // 生成押金账单（一次性，无周期）
        BillingRuleItem depositRule = findRuleByFeeType(ruleItems, FeeType.DEPOSIT.getCode());
        if (depositRule != null) {
            IncomeInvoice depositInvoice = createDepositInvoice(contract, depositRule);
            invoices.add(depositInvoice);
            log.info("生成押金账单: contractId={}, amount={}", contract.getContractId(), depositInvoice.getAmount());
        }
        
        return invoices;
    }

    /**
     * 创建租金账单
     */
    private IncomeInvoice createRentInvoice(Contract contract, BillingRuleItem rentRule) {
        IncomeInvoice invoice = IncomeInvoice.builder()
            .invoiceId(generateInvoiceId())
            .contractId(contract.getContractId())
            .tenantMemberId(contract.getTenantMemberId())
            .roomId(contract.getRoomId())
            .houseSourceId(contract.getHouseSourceId())
            .landlordMemberId(contract.getLandlordMemberId())
            .feeType(FeeType.RENT)
            .amount(rentRule.getChargeValue())
            .cycleStart(contract.getStartDate())
            .cycleEnd(calculateCycleEnd(contract.getStartDate(), rentRule.getChargeCycle()))
            .dueDate(contract.getStartDate())
            .isManual(false)
            .remark(rentRule.getRemark())
            .build();
        
        invoice.initialize();
        return invoice;
    }

    /**
     * 创建押金账单（一次性，无周期）
     */
    private IncomeInvoice createDepositInvoice(Contract contract, BillingRuleItem depositRule) {
        IncomeInvoice invoice = IncomeInvoice.builder()
            .invoiceId(generateInvoiceId())
            .contractId(contract.getContractId())
            .tenantMemberId(contract.getTenantMemberId())
            .roomId(contract.getRoomId())
            .houseSourceId(contract.getHouseSourceId())
            .landlordMemberId(contract.getLandlordMemberId())
            .feeType(FeeType.DEPOSIT)
            .amount(depositRule.getChargeValue())
            .cycleStart(null) // 押金无周期
            .cycleEnd(null)
            .dueDate(contract.getStartDate())
            .isManual(false)
            .remark(depositRule.getRemark())
            .build();
        
        invoice.initialize();
        return invoice;
    }

    /**
     * 核销账单 - 标记已支付
     * 核心业务规则：
     * 1. 房东确认租客线下转账后手动核销
     * 2. 更新status=PAID，记录paid_at时间
     * 3. 若为租金账单，停止每日催收
     * 
     * @param invoice 账单实体
     * @param paidTime 实际支付时间
     */
    public void markAsPaid(IncomeInvoice invoice, LocalDateTime paidTime) {
        if (!invoice.canPay()) {
            throw new BusinessException(ErrorCode.I102.getCode(), "账单状态不允许核销");
        }
        
        invoice.markAsPaid(paidTime);
        log.info("账单核销成功: invoiceId={}, status={}", invoice.getInvoiceId(), invoice.getStatus());
    }

    /**
     * 标记账单逾期
     * 核心业务规则：
     * 1. 逾期仅标记状态，不自动解约，不锁定房间
     * 2. 持续每日催收提醒
     * 
     * @param invoice 账单实体
     */
    public void markAsOverdue(IncomeInvoice invoice) {
        invoice.markAsOverdue();
        log.info("账单标记逾期: invoiceId={}, status={}", invoice.getInvoiceId(), invoice.getStatus());
    }

    /**
     * 房东手动录入杂费账单（房间维度）
     * 核心业务规则：
     * 1. 选择费用类型枚举（9类）
     * 2. 选择房间（必须关联有效合约）
     * 3. 输入金额、选择结算月份
     * 4. 生成1条账单，status=PENDING
     * 
     * @param contract 合约实体
     * @param feeType 费用类型
     * @param amount 金额
     * @param billMonth 结算月份
     * @param remark 备注
     * @return 杂费账单
     */
    public IncomeInvoice createManualInvoice(Contract contract, String feeType, BigDecimal amount, 
                                             String billMonth, String remark) {
        // 校验合约状态
        if (contract.isCompleted()) {
            throw new BusinessException(ErrorCode.I102.getCode(), "已完结合约无法新增账单");
        }
        
        // 解析结算月份
        LocalDate cycleStart = parseBillMonth(billMonth);
        LocalDate cycleEnd = cycleStart.plusMonths(1).minusDays(1);
        
        IncomeInvoice invoice = IncomeInvoice.builder()
            .invoiceId(generateInvoiceId())
            .contractId(contract.getContractId())
            .tenantMemberId(contract.getTenantMemberId())
            .roomId(contract.getRoomId())
            .houseSourceId(contract.getHouseSourceId())
            .landlordMemberId(contract.getLandlordMemberId())
            .feeType(FeeType.valueOf(feeType))
            .amount(amount)
            .cycleStart(cycleStart)
            .cycleEnd(cycleEnd)
            .dueDate(cycleStart)
            .isManual(true)
            .remark(remark)
            .build();
        
        invoice.initialize();
        log.info("手动录入杂费账单: contractId={}, feeType={}, amount={}", 
                 contract.getContractId(), feeType, amount);
        
        return invoice;
    }

    /**
     * 房源维度公摊费用录入与自动分摊
     * 核心业务规则：
     * 1. 查询该房源下所有status=OCCUPIED的房间
     * 2. 按各房间计费规则中该feeType配置的ratio值分摊
     * 3. ratio=0的房间不参与，不生成账单
     * 4. 为每个参与房间生成对应账单
     * 
     * @param contracts 在租合约列表
     * @param feeType 费用类型
     * @param totalAmount 总费用
     * @param billMonth 结算月份
     * @return 分摊后的账单列表
     */
    public List<IncomeInvoice> splitByRatio(List<Contract> contracts, String feeType, 
                                            BigDecimal totalAmount, String billMonth) {
        List<IncomeInvoice> invoices = new ArrayList<>();
        
        // 计算总比例
        BigDecimal totalRatio = BigDecimal.ZERO;
        for (Contract contract : contracts) {
            // TODO: 从计费规则快照中获取该费用类型的ratio值
            // 这里简化处理，假设每个房间平均分摊
            totalRatio = totalRatio.add(BigDecimal.ONE);
        }
        
        // 分摊生成账单
        LocalDate cycleStart = parseBillMonth(billMonth);
        LocalDate cycleEnd = cycleStart.plusMonths(1).minusDays(1);
        
        for (Contract contract : contracts) {
            // 计算分摊金额（简化处理：平均分摊）
            BigDecimal splitAmount = totalAmount.divide(
                new BigDecimal(contracts.size()), 2, BigDecimal.ROUND_HALF_UP);
            
            IncomeInvoice invoice = IncomeInvoice.builder()
                .invoiceId(generateInvoiceId())
                .contractId(contract.getContractId())
                .tenantMemberId(contract.getTenantMemberId())
                .roomId(contract.getRoomId())
                .houseSourceId(contract.getHouseSourceId())
                .landlordMemberId(contract.getLandlordMemberId())
                .feeType(FeeType.valueOf(feeType))
                .amount(splitAmount)
                .cycleStart(cycleStart)
                .cycleEnd(cycleEnd)
                .dueDate(cycleStart)
                .isManual(true)
                .remark("公摊费用分摊")
                .build();
            
            invoice.initialize();
            invoices.add(invoice);
        }
        
        log.info("公摊费用分摊生成账单: feeType={}, totalAmount={}, splitCount={}", 
                 feeType, totalAmount, invoices.size());
        
        return invoices;
    }

    /**
     * 合约退租/解约时截断后续账单
     * 核心业务规则：
     * 1. 标记合约后，不再生成新周期账单
     * 2. 退租当日已产生的账单正常保留
     * 3. 解约/完结合约永久归档，不参与任何统计
     * 
     * @param contract 合约实体
     */
    public void truncateFutureBills(Contract contract) {
        log.info("截断后续账单生成: contractId={}, terminationDate={}", 
                 contract.getContractId(), contract.getEndDate());
        // 此方法主要用于标记，实际截断逻辑在定时任务中实现
    }

    /**
     * 解析计费规则JSON
     */
    private List<BillingRuleItem> parseBillingRules(String rulesJson) {
        // TODO: 实现JSON解析逻辑
        // 这里返回示例数据
        List<BillingRuleItem> items = new ArrayList<>();
        items.add(BillingRuleItem.builder()
            .feeType("RENT")
            .chargeType("fixed")
            .chargeValue(new BigDecimal("1500"))
            .chargeCycle("MONTHLY")
            .remark("月度固定房租")
            .build());
        items.add(BillingRuleItem.builder()
            .feeType("DEPOSIT")
            .chargeType("fixed")
            .chargeValue(new BigDecimal("2000"))
            .chargeCycle("")
            .remark("押金")
            .build());
        return items;
    }

    /**
     * 根据费用类型查找计费规则项
     */
    private BillingRuleItem findRuleByFeeType(List<BillingRuleItem> items, String feeType) {
        return items.stream()
            .filter(item -> item.getFeeType().equals(feeType))
            .findFirst()
            .orElse(null);
    }

    /**
     * 计算周期结束日期
     */
    private LocalDate calculateCycleEnd(LocalDate startDate, String chargeCycle) {
        if (chargeCycle == null || chargeCycle.isEmpty()) {
            return null;
        }
        
        switch (chargeCycle) {
            case "MONTHLY":
                return startDate.plusMonths(1).minusDays(1);
            case "QUARTERLY":
                return startDate.plusMonths(3).minusDays(1);
            case "HALF_YEARLY":
                return startDate.plusMonths(6).minusDays(1);
            case "YEARLY":
                return startDate.plusYears(1).minusDays(1);
            default:
                return startDate.plusMonths(1).minusDays(1);
        }
    }

    /**
     * 解析结算月份（格式：yyyy-MM）
     */
    private LocalDate parseBillMonth(String billMonth) {
        if (billMonth == null || billMonth.isEmpty()) {
            return LocalDate.now().withDayOfMonth(1);
        }
        return LocalDate.parse(billMonth + "-01");
    }

    /**
     * 生成账单ID
     */
    private String generateInvoiceId() {
        return "INV" + System.currentTimeMillis() + String.format("%04d", (int)(Math.random() * 10000));
    }
}