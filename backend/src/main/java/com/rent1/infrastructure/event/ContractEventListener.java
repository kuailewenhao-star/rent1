package com.rent1.infrastructure.event;

import com.rent1.domain.contract.event.ContractCreatedEvent;
import com.rent1.domain.contract.event.ContractTerminatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 合约领域事件监听器
 * 处理合约创建、终止等事件，触发下游领域操作
 */
@Slf4j
@Component
public class ContractEventListener {

    // ==================== ContractCreatedEvent 处理 ====================

    /**
     * 处理合约创建事件
     * 触发：
     * 1. 计费域锁定计费规则（BillingRulesLockedEvent）
     * 2. 房源域变更房间状态（空置中→已出租）
     * 3. 账单域生成首期账单
     * 4. 消息域发送入驻通知
     */
    @EventListener
    public void handleContractCreated(ContractCreatedEvent event) {
        log.info("收到合约创建事件 contractId={}, roomId={}, tenantId={}",
            event.getContractId(), event.getRoomId(), event.getTenantMemberId());

        try {
            // 1. 触发计费规则锁定（由计费域处理）
            // billingRulesDomainService.lockRules(event);

            // 2. 变更房间状态为已出租（由房源域处理）
            // roomStatusDomainService.occupy(event.getRoomId());

            // 3. 生成首期账单（由账单域处理）
            // invoiceDomainService.generateFirstInvoice(event);

            // 4. 发送入驻通知（由消息域处理）
            // notificationDomainService.sendCheckinNotification(event);

            log.info("合约创建事件处理完成 contractId={}", event.getContractId());
        } catch (Exception e) {
            log.error("处理合约创建事件异常 contractId={}", event.getContractId(), e);
            // 实际应考虑重试机制或补偿事务
        }
    }

    // ==================== ContractTerminatedEvent 处理 ====================

    /**
     * 处理合约终止事件
     * 触发：
     * 1. 账单域截断后续账单
     * 2. 押金域结算押金
     * 3. 房源域复位房间状态（已出租→空置中）
     * 4. 消息域发送终止通知
     */
    @EventListener
    public void handleContractTerminated(ContractTerminatedEvent event) {
        log.info("收到合约终止事件 contractId={}, type={}",
            event.getContractId(), event.getTerminationType());

        try {
            // VOID类型不触发下游处理
            if (ContractTerminatedEvent.TYPE_VOID.equals(event.getTerminationType())) {
                log.info("合约作废类型，不触发下游处理 contractId={}", event.getContractId());
                return;
            }

            // 1. 账单域截断后续账单（由账单域处理）
            // invoiceDomainService.truncateFutureBills(event);

            // 2. 押金域结算（如为提前解约）
            if (ContractTerminatedEvent.TYPE_EARLY_TERMINATED.equals(event.getTerminationType())) {
                // depositDomainService.settleDeposit(event);
            }

            // 3. 房源域复位房间状态（由房源域处理）
            // roomStatusDomainService.vacate(event.getRoomId());

            // 4. 发送终止通知（由消息域处理）
            // notificationDomainService.sendTerminationNotification(event);

            log.info("合约终止事件处理完成 contractId={}, type={}",
                event.getContractId(), event.getTerminationType());
        } catch (Exception e) {
            log.error("处理合约终止事件异常 contractId={}", event.getContractId(), e);
        }
    }
}
