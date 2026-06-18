package com.rent1.infrastructure.scheduler;

import com.rent1.domain.invoice.entity.IncomeInvoice;
import com.rent1.domain.invoice.enums.InvoiceStatus;
import com.rent1.domain.invoice.service.InvoiceDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * 账单逾期检测定时任务
 * 核心业务规则：
 * 1. 每日09:00扫描
 * 2. 查询：status=PENDING AND due_date < 今日
 * 3. 更新status=OVERDUE
 * 4. 逾期仅标记状态，不自动解约，不锁定房间
 * 5. 持续每日催收提醒
 */
@Slf4j
@Component
public class OverdueInvoiceScheduler {

    @Autowired
    private InvoiceDomainService invoiceDomainService;

    /**
     * 每日09:00执行账单逾期检测
     * cron表达式：0 0 9 * * ? 表示每天09:00执行
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void checkOverdueInvoices() {
        log.info("开始执行账单逾期检测任务: date={}", LocalDate.now());
        
        try {
            LocalDate today = LocalDate.now();
            
            // TODO: 查询所有status=PENDING且due_date < 今日的账单
            List<IncomeInvoice> pendingInvoices = queryPendingInvoices(today);
            
            for (IncomeInvoice invoice : pendingInvoices) {
                markInvoiceAsOverdue(invoice);
            }
            
            log.info("账单逾期检测任务完成: overdueCount={}", pendingInvoices.size());
        } catch (Exception e) {
            log.error("账单逾期检测任务异常: error={}", e.getMessage(), e);
        }
    }

    /**
     * 标记账单逾期
     */
    private void markInvoiceAsOverdue(IncomeInvoice invoice) {
        try {
            // 调用领域服务标记逾期
            invoiceDomainService.markAsOverdue(invoice);
            
            log.info("账单标记逾期成功: invoiceId={}, dueDate={}", 
                     invoice.getInvoiceId(), invoice.getDueDate());
            
            // TODO: 触发逾期催收提醒（通过事件总线）
            
        } catch (Exception e) {
            log.error("账单标记逾期异常: invoiceId={}, error={}", 
                     invoice.getInvoiceId(), e.getMessage(), e);
        }
    }

    /**
     * 查询待支付且已过期的账单
     * TODO: 实现Repository查询
     */
    private List<IncomeInvoice> queryPendingInvoices(LocalDate today) {
        // 此处返回空列表，实际实现需要Repository层支持
        // 查询条件：status = PENDING AND due_date < today
        return List.of();
    }
}