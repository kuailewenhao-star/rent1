package com.rent1.infrastructure.scheduler;

import com.rent1.domain.contract.entity.Contract;
import com.rent1.domain.contract.repository.ContractRepository;
import com.rent1.domain.invoice.entity.IncomeInvoice;
import com.rent1.domain.invoice.enums.FeeType;
import com.rent1.domain.invoice.enums.InvoiceStatus;
import com.rent1.domain.invoice.repository.IncomeInvoiceRepository;
import com.rent1.domain.notification.service.NotificationDomainService;
import com.rent1.domain.property.entity.Room;
import com.rent1.domain.property.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 租金账单每日催收提醒定时任务
 * 核心业务规则：
 * 1. 每日09:00扫描
 * 2. 查询status=PENDING/OVERDUE且fee_type=RENT
 * 3. 推送租客催收消息
 * 4. 账单核销后停止
 * 5. 仅租金账单每日催收，杂费仅单次提醒
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RentOverdueReminderScheduler {

    private final IncomeInvoiceRepository incomeInvoiceRepository;
    private final ContractRepository contractRepository;
    private final RoomRepository roomRepository;
    private final NotificationDomainService notificationDomainService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 租金账单每日催收提醒定时任务
     * 每日09:00执行
     * cron: 0 0 9 * * ?
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void sendDailyRentReminders() {
        log.info("开始执行租金账单每日催收提醒定时任务...");
        
        LocalDate today = LocalDate.now();
        
        // 查询所有待支付和逾期的租金账单
        List<IncomeInvoice> pendingRentInvoices = incomeInvoiceRepository.findByFeeTypeAndStatusIn(
            FeeType.RENT, 
            List.of(InvoiceStatus.PENDING, InvoiceStatus.OVERDUE)
        );
        
        for (IncomeInvoice invoice : pendingRentInvoices) {
            try {
                // 获取合约信息
                Contract contract = contractRepository.findByIdDirect(invoice.getContractId());
                if (contract == null) {
                    log.warn("合约不存在，跳过。contractId={}", invoice.getContractId());
                    continue;
                }
                
                // 获取房间信息
                Room room = roomRepository.findByIdDirect(invoice.getRoomId());
                if (room == null) {
                    log.warn("房间不存在，跳过。roomId={}", invoice.getRoomId());
                    continue;
                }
                
                // 判断是否逾期
                boolean isOverdue = invoice.isOverdue(today);
                
                // 发送租金催收提醒
                notificationDomainService.sendDailyRentReminder(
                    invoice.getInvoiceId(),
                    invoice.getTenantMemberId(),
                    room.getRoomName(),
                    invoice.getAmount().toString(),
                    invoice.getDueDate() != null ? invoice.getDueDate().format(DATE_FORMATTER) : "",
                    isOverdue
                );
                
                log.info("租金催收提醒发送成功。invoiceId={}, tenant={}, isOverdue={}",
                         invoice.getInvoiceId(), invoice.getTenantMemberId(), isOverdue);
            } catch (Exception e) {
                log.error("租金催收提醒发送失败。invoiceId={}", invoice.getInvoiceId(), e);
            }
        }
        
        log.info("租金账单每日催收提醒定时任务执行完成。处理账单数={}", pendingRentInvoices.size());
    }
}