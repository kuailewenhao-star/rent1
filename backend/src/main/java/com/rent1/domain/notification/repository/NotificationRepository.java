package com.rent1.domain.notification.repository;

import com.rent1.domain.notification.entity.Notification;
import com.rent1.domain.notification.enums.NotificationStatus;
import com.rent1.domain.notification.enums.NotificationType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息仓储接口
 */
public interface NotificationRepository {

    /**
     * 保存消息
     */
    void save(Notification notification);

    /**
     * 批量保存消息
     */
    void saveAll(List<Notification> notifications);

    /**
     * 根据ID查询消息
     */
    Notification findById(String notificationId);

    /**
     * 根据会员ID查询消息列表（按创建时间倒序）
     */
    List<Notification> findByMemberId(String memberId, int page, int pageSize);

    /**
     * 根据会员ID和状态查询消息列表
     */
    List<Notification> findByMemberIdAndStatus(String memberId, NotificationStatus status, 
                                                int page, int pageSize);

    /**
     * 根据会员ID统计未读消息数量
     */
    long countUnreadByMemberId(String memberId);

    /**
     * 根据会员ID统计消息总数
     */
    long countByMemberId(String memberId);

    /**
     * 根据去重标识查询消息（用于防重复推送）
     */
    Notification findByDeduplicationKey(String deduplicationKey);

    /**
     * 更新消息状态
     */
    void updateStatus(String notificationId, NotificationStatus status, LocalDateTime readTime);

    /**
     * 批量更新消息状态（标记全部已读）
     */
    void updateAllStatusToRead(String memberId, LocalDateTime readTime);

    /**
     * 根据类型和关联ID查询消息
     */
    List<Notification> findByTypeAndRelatedId(NotificationType type, String relatedId);

    /**
     * 根据会员ID和类型查询消息
     */
    List<Notification> findByMemberIdAndType(String memberId, NotificationType type, 
                                              int page, int pageSize);

    /**
     * 删除指定日期之前的已读消息（历史消息清理）
     */
    void deleteReadMessagesBefore(LocalDateTime beforeTime);
}