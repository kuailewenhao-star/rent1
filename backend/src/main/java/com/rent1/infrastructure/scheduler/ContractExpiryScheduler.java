package com.rent1.infrastructure.scheduler;

import com.rent1.application.contract.service.ContractAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 合约到期自动退租定时任务
 * 触发时间：每日00:00
 *
 * 核心逻辑：
 * 1. 每日00:00定时扫描
 * 2. 查询：end_date <= 今日 AND status = ACTIVE
 * 3. 对每条到期合约：
 *    - 变更status为EXPIRED
 *    - 触发账单截断（不再生成后续周期账单）
 *    - 触发房间复位（已出租→空置中）
 *    - 发布ContractTerminatedEvent(type=EXPIRED)
 *    - 推送到期提醒给房东+租客
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ContractExpiryScheduler {

    private final ContractAppService contractAppService;

    /**
     * 合约到期自动退租处理
     * 每日凌晨00:00执行
     *
     * Cron表达式：秒 分 时 日 月 周
     * 0 0 0 * * ? = 每日00:00:00
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void processExpiredContracts() {
        log.info("开始执行合约到期自动退租任务");

        try {
            LocalDate today = LocalDate.now();
            int count = contractAppService.batchExpireContracts(today);

            log.info("合约到期自动退租任务完成 date={}, 处理数量={}", today, count);
        } catch (Exception e) {
            log.error("合约到期自动退租任务执行异常", e);
        }
    }

    /**
     * 手动触发到期扫描（用于测试）
     *
     * @param date 指定日期
     * @return 处理数量
     */
    public int manualTrigger(LocalDate date) {
        log.info("手动触发合约到期扫描 date={}", date);
        return contractAppService.batchExpireContracts(date);
    }
}
