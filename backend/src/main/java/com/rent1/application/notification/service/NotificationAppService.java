package com.rent1.application.notification.service;

import com.rent1.domain.notification.entity.Notification;
import com.rent1.domain.notification.enums.NotificationStatus;
import com.rent1.domain.notification.enums.NotificationType;
import com.rent1.domain.notification.repository.NotificationRepository;
import com.rent1.domain.notification.service.NotificationDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 消息应用服务
 * 核心职责：
 * 1. 业务流程编排、跨领域调度、事务控制
 * 2. 禁止包含核心业务规则
 * 
 * 核心业务规则下沉到领域层：
 * - 去重机制：同一合约同一天同一类型只推送1次
 * - 权限隔离：仅看个人消息
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationAppService {

    private final NotificationDomainService notificationDomainService;
    private final NotificationRepository notificationRepository;

    /**
     * 查询消息列表
     * 按会员ID过滤，按创建时间倒序，支持分页
     * 
     * @param memberId 会员ID
     * @param page 页码
     * @param pageSize 每页数量
     * @return 消息列表
     */
    public List<Notification> queryNotifications(String memberId, int page, int pageSize) {
        log.info("查询消息列表。memberId={}, page={}, pageSize={}", memberId, page, pageSize);
        
        // 领域规则：仅看个人消息（已在Repository层通过memberId过滤实现）
        return notificationRepository.findByMemberId(memberId, page, pageSize);
    }

    /**
     * 查询未读消息列表
     * 
     * @param memberId 会员ID
     * @param page 页码
     * @param pageSize 每页数量
     * @return 未读消息列表
     */
    public List<Notification> queryUnreadNotifications(String memberId, int page, int pageSize) {
        log.info("查询未读消息列表。memberId={}, page={}, pageSize={}", memberId, page, pageSize);
        
        return notificationRepository.findByMemberIdAndStatus(memberId, NotificationStatus.UNREAD, page, pageSize);
    }

    /**
     * 查询指定类型消息列表
     * 
     * @param memberId 会员ID
     * @param type 消息类型
     * @param page 页码
     * @param pageSize 每页数量
     * @return 消息列表
     */
    public List<Notification> queryNotificationsByType(String memberId, NotificationType type,
                                                         int page, int pageSize) {
        log.info("查询指定类型消息列表。memberId={}, type={}, page={}, pageSize={}", 
                 memberId, type.getCode(), page, pageSize);
        
        return notificationRepository.findByMemberIdAndType(memberId, type, page, pageSize);
    }

    /**
     * 统计未读消息数量
     * 
     * @param memberId 会员ID
     * @return 未读消息数量
     */
    public long countUnreadNotifications(String memberId) {
        log.info("统计未读消息数量。memberId={}", memberId);
        
        return notificationRepository.countUnreadByMemberId(memberId);
    }

    /**
     * 统计消息总数
     * 
     * @param memberId 会员ID
     * @return 消息总数
     */
    public long countNotifications(String memberId) {
        log.info("统计消息总数。memberId={}", memberId);
        
        return notificationRepository.countByMemberId(memberId);
    }

    /**
     * 标记单条消息已读
     * 事务控制：确保状态更新一致性
     * 
     * @param notificationId 消息ID
     * @param memberId 会员ID（用于权限校验）
     */
    @Transactional
    public void markNotificationAsRead(String notificationId, String memberId) {
        log.info("标记单条消息已读。notificationId={}, memberId={}", notificationId, memberId);
        
        // 调用领域服务执行业务规则（权限校验、状态流转）
        notificationDomainService.markAsRead(notificationId, memberId);
    }

    /**
     * 标记全部消息已读
     * 事务控制：确保批量状态更新一致性
     * 
     * @param memberId 会员ID
     */
    @Transactional
    public void markAllNotificationsAsRead(String memberId) {
        log.info("标记全部消息已读。memberId={}", memberId);
        
        // 调用领域服务执行业务规则
        notificationDomainService.markAllAsRead(memberId);
    }

    /**
     * 查询消息详情
     * 权限校验：仅能查看自己的消息
     * 
     * @param notificationId 消息ID
     * @param memberId 会员ID（用于权限校验）
     * @return 消息详情
     */
    public Notification getNotificationDetail(String notificationId, String memberId) {
        log.info("查询消息详情。notificationId={}, memberId={}", notificationId, memberId);
        
        Notification notification = notificationRepository.findById(notificationId);
        
        if (notification == null) {
            throw new IllegalArgumentException("消息不存在");
        }
        
        // 权限校验：仅能查看自己的消息
        if (!notification.getMemberId().equals(memberId)) {
            throw new IllegalArgumentException("无权限查看该消息");
        }
        
        return notification;
    }

    /**
     * 发送合约到期提醒（由定时任务调用）
     * 编排流程：查询合约信息 → 调用领域服务发送提醒
     * 
     * @param contractId 合约ID
     * @param landlordMemberId 房东会员ID
     * @param tenantMemberId 租客会员ID
     * @param roomId 房间ID
     * @param daysRemaining 剩余天数
     * @param roomName 房间名称
     * @param houseSourceName 房源名称
     */
    @Transactional
    public void sendContractExpiryReminder(String contractId, String landlordMemberId,
                                            String tenantMemberId, String roomId,
                                            int daysRemaining, String roomName,
                                            String houseSourceName) {
        log.info("发送合约到期提醒。contractId={}, daysRemaining={}", contractId, daysRemaining);
        
        // 调用领域服务执行核心业务规则
        notificationDomainService.sendContractExpiringReminder(
            contractId, landlordMemberId, tenantMemberId, roomId,
            daysRemaining, roomName, houseSourceName
        );
    }

    /**
     * 发送租金催收提醒（由定时任务调用）
     * 
     * @param invoiceId 账单ID
     * @param tenantMemberId 租客会员ID
     * @param roomName 房间名称
     * @param amount 金额
     * @param dueDate 应付日期
     * @param isOverdue 是否逾期
     */
    @Transactional
    public void sendRentReminder(String invoiceId, String tenantMemberId,
                                   String roomName, String amount,
                                   String dueDate, boolean isOverdue) {
        log.info("发送租金催收提醒。invoiceId={}, tenant={}, isOverdue={}", 
                 invoiceId, tenantMemberId, isOverdue);
        
        // 调用领域服务执行核心业务规则
        notificationDomainService.sendDailyRentReminder(
            invoiceId, tenantMemberId, roomName, amount, dueDate, isOverdue
        );
    }

    /**
     * 发送杂费单次提醒（由账单生成事件触发）
     * 
     * @param invoiceId 账单ID
     * @param tenantMemberId 租客会员ID
     * @param feeTypeName 费用类型名称
     * @param roomName 房间名称
     * @param amount 金额
     */
    @Transactional
    public void sendMiscFeeReminder(String invoiceId, String tenantMemberId,
                                      String feeTypeName, String roomName,
                                      String amount) {
        log.info("发送杂费单次提醒。invoiceId={}, tenant={}, feeType={}", 
                 invoiceId, tenantMemberId, feeTypeName);
        
        // 调用领域服务执行核心业务规则
        notificationDomainService.sendOneTimeReminder(
            invoiceId, tenantMemberId, feeTypeName, roomName, amount
        );
    }

    /**
     * 发送合约状态变更通知（由合约终止事件触发）
     * 
     * @param contractId 合约ID
     * @param landlordMemberId 房东会员ID
     * @param tenantMemberId 租客会员ID
     * @param terminationType 终止类型
     * @param roomName 房间名称
     * @param houseSourceName 房源名称
     */
    @Transactional
    public void sendContractStatusChangeNotification(String contractId, String landlordMemberId,
                                                       String tenantMemberId, String terminationType,
                                                       String roomName, String houseSourceName) {
        log.info("发送合约状态变更通知。contractId={}, terminationType={}", 
                 contractId, terminationType);
        
        // 调用领域服务执行核心业务规则
        notificationDomainService.sendContractStatusChangeNotification(
            contractId, landlordMemberId, tenantMemberId, terminationType,
            roomName, houseSourceName
        );
    }

    /**
     * 发送公摊账单通知（由公摊账单生成触发）
     * 
     * @param invoiceId 账单ID
     * @param tenantMemberId 租客会员ID
     * @param feeTypeName 费用类型名称
     * @param roomName 房间名称
     * @param amount 金额
     * @param ratio 分摊比例
     */
    @Transactional
    public void sendSharedBillNotice(String invoiceId, String tenantMemberId,
                                       String feeTypeName, String roomName,
                                       String amount, String ratio) {
        log.info("发送公摊账单通知。invoiceId={}, tenant={}, feeType={}", 
                 invoiceId, tenantMemberId, feeTypeName);
        
        // 调用领域服务执行核心业务规则
        notificationDomainService.sendSharedBillNotice(
            invoiceId, tenantMemberId, feeTypeName, roomName, amount, ratio
        );
    }
}