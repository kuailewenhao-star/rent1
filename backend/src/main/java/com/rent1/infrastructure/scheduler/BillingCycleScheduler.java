package com.rent1.infrastructure.scheduler;

import com.rent1.domain.contract.entity.Contract;
import com.rent1.domain.contract.entity.ContractBillingRulesSnapshot;
import com.rent1.domain.invoice.entity.IncomeInvoice;
import com.rent1.domain.invoice.entity.BillingRuleItem;
import com.rent1.domain.invoice.enums.FeeType;
import com.rent1.domain.invoice.enums.InvoiceStatus;
import com.rent1.domain.invoice.service.InvoiceDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * 周期性账单自动生成定时任务
 * 核心业务规则：
 * 1. 每日凌晨扫描所有status=ACTIVE合约
 * 2. 逐条读取billing_rules_snapshot配置
 * 3. 对每个费用项（除押金DEPOSIT），按chargeCycle计算下一期
 * 4. 周期到期时生成账单
 * 5. 防重复：检查UNIQUE(room_id, fee_type, cycle_start, cycle_end)
 * 6. 不同费用项不同周期互不干扰
 */
@Slf4j
@Component
public class BillingCycleScheduler {

    @Autowired
    private InvoiceDomainService invoiceDomainService;

    /**
     * 每日10:00执行周期性账单生成
     * cron表达式：0 10 0 * * ? 表示每天00:10执行
     */
    @Scheduled(cron = "0 10 0 * * ?")
    public void generatePeriodicInvoices() {
        log.info("开始执行周期性账单生成任务: date={}", LocalDate.now());
        
        try {
            // TODO: 查询所有status=ACTIVE的合约
            List<Contract> activeContracts = queryActiveContracts();
            
            for (Contract contract : activeContracts) {
                generatePeriodicInvoiceForContract(contract);
            }
            
            log.info("周期性账单生成任务完成: contractCount={}", activeContracts.size());
        } catch (Exception e) {
            log.error("周期性账单生成任务异常: error={}", e.getMessage(), e);
        }
    }

    /**
     * 为单个合约生成周期性账单
     */
    private void generatePeriodicInvoiceForContract(Contract contract) {
        try {
            // TODO: 查询合约的计费规则快照
            ContractBillingRulesSnapshot snapshot = queryContractBillingRulesSnapshot(contract.getContractId());
            
            if (snapshot == null) {
                log.warn("合约计费规则快照不存在: contractId={}", contract.getContractId());
                return;
            }
            
            // 解析计费规则JSON
            List<BillingRuleItem> ruleItems = parseBillingRules(snapshot.getRulesJson());
            
            // 为每个周期性费用项生成账单（排除押金）
            for (BillingRuleItem ruleItem : ruleItems) {
                if (ruleItem.isDeposit()) {
                    continue; // 押金不生成周期账单
                }
                
                // 判断是否需要生成下一期账单
                if (shouldGenerateNextInvoice(contract, ruleItem)) {
                    generateNextPeriodicInvoice(contract, ruleItem);
                }
            }
            
        } catch (Exception e) {
            log.error("合约周期性账单生成异常: contractId={}, error={}", 
                     contract.getContractId(), e.getMessage(), e);
        }
    }

    /**
     * 判断是否需要生成下一期账单
     * 核心规则：根据计费周期判断是否到期
     */
    private boolean shouldGenerateNextInvoice(Contract contract, BillingRuleItem ruleItem) {
        LocalDate today = LocalDate.now();
        
        // TODO: 查询该合约该费用类型的最近一期账单
        IncomeInvoice lastInvoice = queryLastInvoice(contract.getRoomId(), ruleItem.getFeeType());
        
        if (lastInvoice == null) {
            // 无历史账单，判断是否需要生成首期（合约创建时已生成，此处跳过）
            return false;
        }
        
        // 计算下一期账单周期起始时间
        LocalDate nextCycleStart = calculateNextCycleStart(lastInvoice.getCycleEnd(), ruleItem.getChargeCycle());
        
        // 判断下一期周期是否在合约有效期内
        if (nextCycleStart.isAfter(contract.getEndDate())) {
            log.info("下一期账单超出合约有效期: contractId={}, feeType={}, nextCycleStart={}", 
                    contract.getContractId(), ruleItem.getFeeType(), nextCycleStart);
            return false;
        }
        
        // 判断是否已到期（下一期周期起始时间 <= 今天）
        return !nextCycleStart.isAfter(today);
    }

    /**
     * 生成下一期周期性账单
     */
    private void generateNextPeriodicInvoice(Contract contract, BillingRuleItem ruleItem) {
        // TODO: 查询最近一期账单
        IncomeInvoice lastInvoice = queryLastInvoice(contract.getRoomId(), ruleItem.getFeeType());
        
        LocalDate nextCycleStart = calculateNextCycleStart(lastInvoice.getCycleEnd(), ruleItem.getChargeCycle());
        LocalDate nextCycleEnd = calculateCycleEnd(nextCycleStart, ruleItem.getChargeCycle());
        
        // 防重复：检查是否已存在相同周期的账单
        if (existsInvoice(contract.getRoomId(), ruleItem.getFeeType(), nextCycleStart, nextCycleEnd)) {
            log.warn("账单已存在，跳过生成: roomId={}, feeType={}, cycleStart={}", 
                    contract.getRoomId(), ruleItem.getFeeType(), nextCycleStart);
            return;
        }
        
        // 调用领域服务生成周期性账单
        IncomeInvoice invoice = invoiceDomainService.createManualInvoice(
            contract, 
            ruleItem.getFeeType(), 
            ruleItem.getChargeValue(), 
            nextCycleStart.toString().substring(0, 7), // 格式：yyyy-MM
            ruleItem.getRemark()
        );
        
        // 更新账单周期信息
        invoice.setCycleStart(nextCycleStart);
        invoice.setCycleEnd(nextCycleEnd);
        invoice.setIsManual(false);
        
        log.info("周期性账单生成成功: contractId={}, feeType={}, cycleStart={}, amount={}", 
                contract.getContractId(), ruleItem.getFeeType(), nextCycleStart, invoice.getAmount());
    }

    /**
     * 计算下一期周期起始时间
     */
    private LocalDate calculateNextCycleStart(LocalDate lastCycleEnd, String chargeCycle) {
        if (lastCycleEnd == null) {
            return LocalDate.now().withDayOfMonth(1);
        }
        
        // 下一期起始时间 = 上期结束时间 + 1天
        LocalDate nextStart = lastCycleEnd.plusDays(1);
        
        return nextStart;
    }

    /**
     * 计算周期结束时间
     */
    private LocalDate calculateCycleEnd(LocalDate cycleStart, String chargeCycle) {
        if (cycleStart == null || chargeCycle == null) {
            return cycleStart.plusMonths(1).minusDays(1);
        }
        
        switch (chargeCycle) {
            case "MONTHLY":
                return cycleStart.plusMonths(1).minusDays(1);
            case "QUARTERLY":
                return cycleStart.plusMonths(3).minusDays(1);
            case "HALF_YEARLY":
                return cycleStart.plusMonths(6).minusDays(1);
            case "YEARLY":
                return cycleStart.plusYears(1).minusDays(1);
            default:
                return cycleStart.plusMonths(1).minusDays(1);
        }
    }

    /**
     * 查询所有ACTIVE状态合约
     * TODO: 实现Repository查询
     */
    private List<Contract> queryActiveContracts() {
        // 此处返回空列表，实际实现需要Repository层支持
        return List.of();
    }

    /**
     * 查询合约计费规则快照
     * TODO: 实现Repository查询
     */
    private ContractBillingRulesSnapshot queryContractBillingRulesSnapshot(String contractId) {
        // 此处返回null，实际实现需要Repository层支持
        return null;
    }

    /**
     * 解析计费规则JSON
     * TODO: 实现JSON解析逻辑
     */
    private List<BillingRuleItem> parseBillingRules(String rulesJson) {
        // 此处返回空列表，实际实现需要Repository层支持
        return List.of();
    }

    /**
     * 查询最近一期账单
     * TODO: 实现Repository查询
     */
    private IncomeInvoice queryLastInvoice(String roomId, String feeType) {
        // 此处返回null，实际实现需要Repository层支持
        return null;
    }

    /**
     * 检查账单是否已存在（防重复）
     * TODO: 实现Repository查询
     */
    private boolean existsInvoice(String roomId, String feeType, LocalDate cycleStart, LocalDate cycleEnd) {
        // 此处返回false，实际实现需要Repository层支持
        return false;
    }
}