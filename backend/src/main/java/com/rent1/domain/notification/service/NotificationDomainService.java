package com.rent1.domain.notification.service;

import com.rent1.domain.notification.entity.Notification;
import com.rent1.domain.notification.enums.NotificationType;
import com.rent1.domain.notification.enums.RelatedType;
import com.rent1.domain.notification.repository.NotificationRepository;
import com.rent1.domain.notification.event.NotificationGeneratedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 消息域领域服务
 * 核心业务规则：
 * 1. 合约到期提醒：提前7天、前3天双向推送（房东+租客）
 * 2. 租金账单每日催收提醒（未支付状态下每日循环）
 * 3. 杂费账单单次提醒（生成后仅触发一次）
 * 4. 合约状态变更通知（解约/完结/作废）
 * 5. 去重机制：同一合约同一天同一类型只推送1次
 * 6. 权限隔离：仅看个人消息
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationDomainService {

    private final NotificationRepository notificationRepository;

    /**
     * 发送合约到期提醒（提前7天、前3天）
     * 双向推送：房东+租客
     * 去重机制：同一合约同一天同一类型只推送1次
     * 
     * @param contractId 合约ID
     * @param landlordMemberId 房东会员ID
     * @param tenantMemberId 租客会员ID
     * @param roomId 房间ID
     * @param daysRemaining 剩余天数（7或3）
     * @param roomName 房间名称
     * @param houseSourceName 房源名称
     */
    public void sendContractExpiringReminder(String contractId, String landlordMemberId,
                                               String tenantMemberId, String roomId,
                                               int daysRemaining, String roomName,
                                               String houseSourceName) {
        LocalDateTime now = LocalDateTime.now();
        
        // 生成去重标识，防止同一合约同一天重复推送
        String deduplicationKey = Notification.generateExpiryDeduplicationKey(contractId, daysRemaining, now);
        
        // 检查是否已推送过
        if (notificationRepository.findByDeduplicationKey(deduplicationKey) != null) {
            log.info("合约到期提醒已推送过，跳过重复推送。contractId={}, daysRemaining={}", contractId, daysRemaining);
            return;
        }
        
        List<Notification> notifications = new ArrayList<>();
        
        // 构建房东消息
        String landlordTitle = "合约即将到期提醒";
        String landlordContent = String.format(
            "您的房源【%s】房间【%s】租约将于%d天后到期，请及时安排续租或退租事宜。",
            houseSourceName, roomName, daysRemaining
        );
        
        Notification landlordNotification = Notification.builder()
            .memberId(landlordMemberId)
            .type(NotificationType.CONTRACT_EXPIRING)
            .title(landlordTitle)
            .content(landlordContent)
            .relatedType(RelatedType.CONTRACT)
            .relatedId(contractId)
            .deduplicationKey(deduplicationKey)
            .build();
        landlordNotification.initialize();
        notifications.add(landlordNotification);
        
        // 构建租客消息
        String tenantTitle = "租约即将到期提醒";
        String tenantContent = String.format(
            "您承租的房间【%s】租约将于%d天后到期，请联系房东续租或办理退租手续。",
            roomName, daysRemaining
        );
        
        Notification tenantNotification = Notification.builder()
            .memberId(tenantMemberId)
            .type(NotificationType.CONTRACT_EXPIRING)
            .title(tenantTitle)
            .content(tenantContent)
            .relatedType(RelatedType.CONTRACT)
            .relatedId(contractId)
            .deduplicationKey(deduplicationKey)
            .build();
        tenantNotification.initialize();
        notifications.add(tenantNotification);
        
        // 批量保存消息
        notificationRepository.saveAll(notifications);
        
        log.info("合约到期提醒发送成功。contractId={}, daysRemaining={}, landlord={}, tenant={}",
                 contractId, daysRemaining, landlordMemberId, tenantMemberId);
    }

    /**
     * 发送租金账单每日催收提醒
     * 仅推送租客，每日循环催收，账单核销后停止
     * 去重机制：同一账单同一天只推送1次
     * 
     * @param invoiceId 账单ID
     * @param tenantMemberId 租客会员ID
     * @param roomName 房间名称
     * @param amount 金额
     * @param dueDate 应付日期
     * @param isOverdue 是否逾期
     */
    public void sendDailyRentReminder(String invoiceId, String tenantMemberId,
                                        String roomName, String amount,
                                        String dueDate, boolean isOverdue) {
        LocalDateTime now = LocalDateTime.now();
        
        // 生成去重标识，防止同一账单同一天重复催收
        String deduplicationKey = Notification.generateRentReminderDeduplicationKey(invoiceId, now);
        
        // 检查是否已推送过
        if (notificationRepository.findByDeduplicationKey(deduplicationKey) != null) {
            log.info("租金催收提醒已推送过，跳过重复推送。invoiceId={}", invoiceId);
            return;
        }
        
        // 构建催收消息
        String title = isOverdue ? "租金逾期催收提醒" : "租金账单待支付提醒";
        String content = isOverdue
            ? String.format("您承租的房间【%s】租金账单已逾期，应付金额：%s元，应付日期：%s，请尽快缴纳。", roomName, amount, dueDate)
            : String.format("您承租的房间【%s】租金账单待支付，应付金额：%s元，应付日期：%s，请及时缴纳。", roomName, amount, dueDate);
        
        Notification notification = Notification.builder()
            .memberId(tenantMemberId)
            .type(NotificationType.RENT_REMINDER)
            .title(title)
            .content(content)
            .relatedType(RelatedType.INVOICE)
            .relatedId(invoiceId)
            .deduplicationKey(deduplicationKey)
            .build();
        notification.initialize();
        
        // 保存消息
        notificationRepository.save(notification);
        
        log.info("租金催收提醒发送成功。invoiceId={}, tenant={}, isOverdue={}",
                 invoiceId, tenantMemberId, isOverdue);
    }

    /**
     * 发送杂费账单单次提醒
     * 仅推送租客，生成后仅触发一次，无每日催收
     * 
     * @param invoiceId 账单ID
     * @param tenantMemberId 租客会员ID
     * @param feeTypeName 费用类型名称
     * @param roomName 房间名称
     * @param amount 金额
     */
    public void sendOneTimeReminder(String invoiceId, String tenantMemberId,
                                      String feeTypeName, String roomName,
                                      String amount) {
        LocalDateTime now = LocalDateTime.now();
        
        // 生成去重标识（杂费仅推送一次）
        String deduplicationKey = Notification.generateDeduplicationKey(
            tenantMemberId, NotificationType.MISC_FEE_REMINDER, invoiceId, now
        );
        
        // 检查是否已推送过
        if (notificationRepository.findByDeduplicationKey(deduplicationKey) != null) {
            log.info("杂费提醒已推送过，跳过重复推送。invoiceId={}", invoiceId);
            return;
        }
        
        // 构建杂费提醒消息
        String title = "杂费账单到账通知";
        String content = String.format(
            "您承租的房间【%s】产生%s账单，金额：%s元，请及时缴纳。",
            roomName, feeTypeName, amount
        );
        
        Notification notification = Notification.builder()
            .memberId(tenantMemberId)
            .type(NotificationType.MISC_FEE_REMINDER)
            .title(title)
            .content(content)
            .relatedType(RelatedType.INVOICE)
            .relatedId(invoiceId)
            .deduplicationKey(deduplicationKey)
            .build();
        notification.initialize();
        
        // 保存消息
        notificationRepository.save(notification);
        
        log.info("杂费提醒发送成功。invoiceId={}, tenant={}, feeType={}",
                 invoiceId, tenantMemberId, feeTypeName);
    }

    /**
     * 发送合约状态变更通知
     * 双向推送：房东+租客
     * 状态类型：解约/完结/作废
     * 
     * @param contractId 合约ID
     * @param landlordMemberId 房东会员ID
     * @param tenantMemberId 租客会员ID
     * @param terminationType 终止类型（EXPIRED/EARLY_TERMINATED/VOID）
     * @param roomName 房间名称
     * @param houseSourceName 房源名称
     */
    public void sendContractStatusChangeNotification(String contractId, String landlordMemberId,
                                                       String tenantMemberId, String terminationType,
                                                       String roomName, String houseSourceName) {
        LocalDateTime now = LocalDateTime.now();
        
        // 生成去重标识
        String deduplicationKey = Notification.generateDeduplicationKey(
            landlordMemberId, NotificationType.CONTRACT_STATUS_CHANGE, contractId, now
        );
        
        // 检查是否已推送过
        if (notificationRepository.findByDeduplicationKey(deduplicationKey) != null) {
            log.info("合约状态变更通知已推送过，跳过重复推送。contractId={}", contractId);
            return;
        }
        
        // 根据终止类型生成不同文案
        String statusName = getStatusName(terminationType);
        String landlordTitle = "合约状态变更通知";
        String landlordContent = String.format(
            "您的房源【%s】房间【%s】租约已%s，请及时处理押金结算事宜。",
            houseSourceName, roomName, statusName
        );
        
        String tenantTitle = "租约状态变更通知";
        String tenantContent = String.format(
            "您承租的房间【%s】租约已%s，请联系房东办理押金退还手续。",
            roomName, statusName
        );
        
        List<Notification> notifications = new ArrayList<>();
        
        // 构建房东消息
        Notification landlordNotification = Notification.builder()
            .memberId(landlordMemberId)
            .type(NotificationType.CONTRACT_STATUS_CHANGE)
            .title(landlordTitle)
            .content(landlordContent)
            .relatedType(RelatedType.CONTRACT)
            .relatedId(contractId)
            .deduplicationKey(deduplicationKey)
            .build();
        landlordNotification.initialize();
        notifications.add(landlordNotification);
        
        // 构建租客消息
        Notification tenantNotification = Notification.builder()
            .memberId(tenantMemberId)
            .type(NotificationType.CONTRACT_STATUS_CHANGE)
            .title(tenantTitle)
            .content(tenantContent)
            .relatedType(RelatedType.CONTRACT)
            .relatedId(contractId)
            .deduplicationKey(deduplicationKey)
            .build();
        tenantNotification.initialize();
        notifications.add(tenantNotification);
        
        // 批量保存消息
        notificationRepository.saveAll(notifications);
        
        log.info("合约状态变更通知发送成功。contractId={}, terminationType={}, landlord={}, tenant={}",
                 contractId, terminationType, landlordMemberId, tenantMemberId);
    }

    /**
     * 发送公摊账单到账通知
     * 仅推送租客，单次通知
     * 
     * @param invoiceId 账单ID
     * @param tenantMemberId 租客会员ID
     * @param feeTypeName 费用类型名称
     * @param roomName 房间名称
     * @param amount 金额
     * @param ratio 分摊比例
     */
    public void sendSharedBillNotice(String invoiceId, String tenantMemberId,
                                       String feeTypeName, String roomName,
                                       String amount, String ratio) {
        LocalDateTime now = LocalDateTime.now();
        
        // 生成去重标识
        String deduplicationKey = Notification.generateDeduplicationKey(
            tenantMemberId, NotificationType.SHARED_BILL_NOTICE, invoiceId, now
        );
        
        // 检查是否已推送过
        if (notificationRepository.findByDeduplicationKey(deduplicationKey) != null) {
            log.info("公摊账单通知已推送过，跳过重复推送。invoiceId={}", invoiceId);
            return;
        }
        
        // 构建公摊账单通知消息
        String title = "公摊费用账单到账通知";
        String content = String.format(
            "您承租的房间【%s】产生公摊%s账单，分摊比例：%s，金额：%s元，请及时缴纳。",
            roomName, feeTypeName, ratio, amount
        );
        
        Notification notification = Notification.builder()
            .memberId(tenantMemberId)
            .type(NotificationType.SHARED_BILL_NOTICE)
            .title(title)
            .content(content)
            .relatedType(RelatedType.INVOICE)
            .relatedId(invoiceId)
            .deduplicationKey(deduplicationKey)
            .build();
        notification.initialize();
        
        // 保存消息
        notificationRepository.save(notification);
        
        log.info("公摊账单通知发送成功。invoiceId={}, tenant={}, feeType={}",
                 invoiceId, tenantMemberId, feeTypeName);
    }

    /**
     * 标记消息已读
     * 
     * @param notificationId 消息ID
     * @param memberId 会员ID（用于权限校验）
     */
    public void markAsRead(String notificationId, String memberId) {
        Notification notification = notificationRepository.findById(notificationId);
        
        if (notification == null) {
            throw new IllegalArgumentException("消息不存在");
        }
        
        // 权限校验：仅能标记自己的消息
        if (!notification.getMemberId().equals(memberId)) {
            throw new IllegalArgumentException("无权限操作该消息");
        }
        
        notification.markAsRead();
        notificationRepository.updateStatus(notificationId, notification.getStatus(), notification.getReadTime());
        
        log.info("消息已标记已读。notificationId={}, memberId={}", notificationId, memberId);
    }

    /**
     * 标记全部消息已读
     * 
     * @param memberId 会员ID
     */
    public void markAllAsRead(String memberId) {
        LocalDateTime now = LocalDateTime.now();
        notificationRepository.updateAllStatusToRead(memberId, now);
        
        log.info("全部消息已标记已读。memberId={}", memberId);
    }

    /**
     * 根据终止类型获取状态名称
     */
    private String getStatusName(String terminationType) {
        switch (terminationType) {
            case "EXPIRED":
                return "到期完结";
            case "EARLY_TERMINATED":
                return "提前解约";
            case "VOID":
                return "作废";
            default:
                return "终止";
        }
    }
}