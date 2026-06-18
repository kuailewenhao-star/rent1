package com.rent1.infrastructure.scheduler;

import com.rent1.domain.contract.entity.Contract;
import com.rent1.domain.contract.repository.ContractRepository;
import com.rent1.domain.invoice.entity.IncomeInvoice;
import com.rent1.domain.invoice.enums.FeeType;
import com.rent1.domain.invoice.enums.InvoiceStatus;
import com.rent1.domain.invoice.repository.IncomeInvoiceRepository;
import com.rent1.domain.notification.service.NotificationDomainService;
import com.rent1.domain.property.entity.HouseSource;
import com.rent1.domain.property.entity.Room;
import com.rent1.domain.property.repository.HouseSourceRepository;
import com.rent1.domain.property.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 合约到期提醒定时任务
 * 核心业务规则：
 * 1. 每日08:00扫描
 * 2. 查找合约剩余天数=7或3
 * 3. 双向推送：房东+租客
 * 4. 去重：同一合约同一天同一类型只推送1次
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ContractExpiryReminderScheduler {

    private final ContractRepository contractRepository;
    private final HouseSourceRepository houseSourceRepository;
    private final RoomRepository roomRepository;
    private final NotificationDomainService notificationDomainService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 合约到期提醒定时任务
     * 每日08:00执行
     * cron: 0 0 8 * * ?
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void sendContractExpiryReminders() {
        log.info("开始执行合约到期提醒定时任务...");
        
        LocalDate today = LocalDate.now();
        
        // 查询所有履约中的合约
        List<Contract> activeContracts = contractRepository.findByStatus(com.rent1.domain.common.ContractStatus.ACTIVE);
        
        for (Contract contract : activeContracts) {
            try {
                // 计算剩余天数
                long remainingDays = contract.getRemainingDays();
                
                // 仅处理剩余天数为7或3的合约
                if (remainingDays == 7 || remainingDays == 3) {
                    // 获取房间信息
                    Room room = roomRepository.findByIdDirect(contract.getRoomId());
                    if (room == null) {
                        log.warn("房间不存在，跳过。roomId={}", contract.getRoomId());
                        continue;
                    }
                    
                    // 获取房源信息
                    HouseSource houseSource = houseSourceRepository.findByIdDirect(room.getHouseSourceId());
                    if (houseSource == null) {
                        log.warn("房源不存在，跳过。houseSourceId={}", room.getHouseSourceId());
                        continue;
                    }
                    
                    // 发送到期提醒
                    notificationDomainService.sendContractExpiringReminder(
                        contract.getContractId(),
                        contract.getLandlordMemberId(),
                        contract.getTenantMemberId(),
                        contract.getRoomId(),
                        (int) remainingDays,
                        room.getRoomName(),
                        houseSource.getName()
                    );
                    
                    log.info("合约到期提醒发送成功。contractId={}, remainingDays={}", 
                             contract.getContractId(), remainingDays);
                }
            } catch (Exception e) {
                log.error("合约到期提醒发送失败。contractId={}", contract.getContractId(), e);
            }
        }
        
        log.info("合约到期提醒定时任务执行完成。处理合约数={}", activeContracts.size());
    }
}