package com.rent1.domain.notification.entity;

import com.rent1.domain.notification.enums.NotificationStatus;
import com.rent1.domain.notification.enums.NotificationType;
import com.rent1.domain.notification.enums.RelatedType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 消息实体 - 小程序站内消息
 * 核心业务规则：
 * 1. 合约到期提醒：提前7天、前3天双向推送（房东+租客）
 * 2. 租金账单每日催收提醒（未支付状态下每日循环）
 * 3. 杂费账单单次提醒（生成后仅触发一次）
 * 4. 合约状态变更通知（解约/完结/作废）
 * 5. 去重机制：同一合约同一天同一类型只推送1次
 * 6. 权限隔离：仅看个人消息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    /** 消息ID */
    private String notificationId;

    /** 接收者会员ID */
    private String memberId;

    /** 消息类型 */
    private NotificationType type;

    /** 消息标题 */
    private String title;

    /** 消息内容 */
    private String content;

    /** 关联业务类型 */
    private RelatedType relatedType;

    /** 关联业务ID */
    private String relatedId;

    /** 消息状态：UNREAD/READ */
    private NotificationStatus status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 阅读时间 */
    private LocalDateTime readTime;

    /** 去重标识（用于防止重复推送） */
    private String deduplicationKey;

    /**
     * 初始化消息 - 设置初始状态
     */
    public void initialize() {
        this.status = NotificationStatus.UNREAD;
        this.createTime = LocalDateTime.now();
    }

    /**
     * 标记已读
     */
    public void markAsRead() {
        if (this.status == NotificationStatus.UNREAD) {
            this.status = NotificationStatus.READ;
            this.readTime = LocalDateTime.now();
        }
    }

    /**
     * 校验是否未读
     */
    public boolean isUnread() {
        return this.status == NotificationStatus.UNREAD;
    }

    /**
     * 校验是否已读
     */
    public boolean isRead() {
        return this.status == NotificationStatus.READ;
    }

    /**
     * 生成去重标识
     * 格式：{memberId}:{type}:{relatedId}:{date}
     * 用于防止同一用户同一天同一类型消息重复推送
     */
    public static String generateDeduplicationKey(String memberId, NotificationType type, 
                                                    String relatedId, LocalDateTime date) {
        return memberId + ":" + type.getCode() + ":" + relatedId + ":" + date.toLocalDate();
    }

    /**
     * 生成合约到期提醒去重标识
     * 格式：{contractId}:{days}:{date}
     * 用于防止同一合约同一天重复推送到期提醒
     */
    public static String generateExpiryDeduplicationKey(String contractId, int daysRemaining, 
                                                          LocalDateTime date) {
        return "EXPIRY:" + contractId + ":" + daysRemaining + ":" + date.toLocalDate();
    }

    /**
     * 生成租金催收去重标识
     * 格式：{invoiceId}:{date}
     * 用于防止同一账单同一天重复催收
     */
    public static String generateRentReminderDeduplicationKey(String invoiceId, LocalDateTime date) {
        return "RENT:" + invoiceId + ":" + date.toLocalDate();
    }
}