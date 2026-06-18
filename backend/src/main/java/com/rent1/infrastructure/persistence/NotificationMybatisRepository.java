package com.rent1.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.rent1.domain.notification.entity.Notification;
import com.rent1.domain.notification.enums.NotificationStatus;
import com.rent1.domain.notification.enums.NotificationType;
import com.rent1.domain.notification.repository.NotificationRepository;
import com.rent1.infrastructure.persistence.mapper.NotificationMapper;
import com.rent1.infrastructure.persistence.po.NotificationPO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 消息仓储MyBatis实现
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class NotificationMybatisRepository implements NotificationRepository {

    private final NotificationMapper notificationMapper;

    @Override
    public void save(Notification notification) {
        NotificationPO po = convertToPO(notification);
        notificationMapper.insert(po);
    }

    @Override
    public void saveAll(List<Notification> notifications) {
        List<NotificationPO> poList = notifications.stream()
            .map(this::convertToPO)
            .collect(Collectors.toList());
        notificationMapper.insertBatch(poList);
    }

    @Override
    public Notification findById(String notificationId) {
        NotificationPO po = notificationMapper.selectById(notificationId);
        return po != null ? convertToEntity(po) : null;
    }

    @Override
    public List<Notification> findByMemberId(String memberId, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        LambdaQueryWrapper<NotificationPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationPO::getMemberId, memberId)
               .orderByDesc(NotificationPO::getCreateTime)
               .last("LIMIT " + offset + ", " + pageSize);
        
        List<NotificationPO> poList = notificationMapper.selectList(wrapper);
        return poList.stream()
            .map(this::convertToEntity)
            .collect(Collectors.toList());
    }

    @Override
    public List<Notification> findByMemberIdAndStatus(String memberId, NotificationStatus status,
                                                        int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        LambdaQueryWrapper<NotificationPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationPO::getMemberId, memberId)
               .eq(NotificationPO::getStatus, status.getCode())
               .orderByDesc(NotificationPO::getCreateTime)
               .last("LIMIT " + offset + ", " + pageSize);
        
        List<NotificationPO> poList = notificationMapper.selectList(wrapper);
        return poList.stream()
            .map(this::convertToEntity)
            .collect(Collectors.toList());
    }

    @Override
    public long countUnreadByMemberId(String memberId) {
        LambdaQueryWrapper<NotificationPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationPO::getMemberId, memberId)
               .eq(NotificationPO::getStatus, NotificationStatus.UNREAD.getCode());
        return notificationMapper.selectCount(wrapper);
    }

    @Override
    public long countByMemberId(String memberId) {
        LambdaQueryWrapper<NotificationPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationPO::getMemberId, memberId);
        return notificationMapper.selectCount(wrapper);
    }

    @Override
    public Notification findByDeduplicationKey(String deduplicationKey) {
        LambdaQueryWrapper<NotificationPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationPO::getDeduplicationKey, deduplicationKey);
        NotificationPO po = notificationMapper.selectOne(wrapper);
        return po != null ? convertToEntity(po) : null;
    }

    @Override
    public void updateStatus(String notificationId, NotificationStatus status, LocalDateTime readTime) {
        LambdaUpdateWrapper<NotificationPO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(NotificationPO::getNotificationId, notificationId)
               .set(NotificationPO::getStatus, status.getCode())
               .set(NotificationPO::getReadTime, readTime)
               .set(NotificationPO::getUpdateTime, LocalDateTime.now());
        notificationMapper.update(null, wrapper);
    }

    @Override
    public void updateAllStatusToRead(String memberId, LocalDateTime readTime) {
        LambdaUpdateWrapper<NotificationPO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(NotificationPO::getMemberId, memberId)
               .eq(NotificationPO::getStatus, NotificationStatus.UNREAD.getCode())
               .set(NotificationPO::getStatus, NotificationStatus.READ.getCode())
               .set(NotificationPO::getReadTime, readTime)
               .set(NotificationPO::getUpdateTime, LocalDateTime.now());
        notificationMapper.update(null, wrapper);
    }

    @Override
    public List<Notification> findByTypeAndRelatedId(NotificationType type, String relatedId) {
        LambdaQueryWrapper<NotificationPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationPO::getType, type.getCode())
               .eq(NotificationPO::getRelatedId, relatedId);
        
        List<NotificationPO> poList = notificationMapper.selectList(wrapper);
        return poList.stream()
            .map(this::convertToEntity)
            .collect(Collectors.toList());
    }

    @Override
    public List<Notification> findByMemberIdAndType(String memberId, NotificationType type,
                                                      int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        LambdaQueryWrapper<NotificationPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationPO::getMemberId, memberId)
               .eq(NotificationPO::getType, type.getCode())
               .orderByDesc(NotificationPO::getCreateTime)
               .last("LIMIT " + offset + ", " + pageSize);
        
        List<NotificationPO> poList = notificationMapper.selectList(wrapper);
        return poList.stream()
            .map(this::convertToEntity)
            .collect(Collectors.toList());
    }

    @Override
    public void deleteReadMessagesBefore(LocalDateTime beforeTime) {
        LambdaQueryWrapper<NotificationPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationPO::getStatus, NotificationStatus.READ.getCode())
               .lt(NotificationPO::getCreateTime, beforeTime);
        notificationMapper.delete(wrapper);
    }

    /**
     * 实体转换为PO
     */
    private NotificationPO convertToPO(Notification entity) {
        NotificationPO po = new NotificationPO();
        po.setNotificationId(entity.getNotificationId());
        po.setMemberId(entity.getMemberId());
        po.setType(entity.getType().getCode());
        po.setTitle(entity.getTitle());
        po.setContent(entity.getContent());
        po.setRelatedType(entity.getRelatedType().getCode());
        po.setRelatedId(entity.getRelatedId());
        po.setStatus(entity.getStatus().getCode());
        po.setCreateTime(entity.getCreateTime());
        po.setReadTime(entity.getReadTime());
        po.setUpdateTime(entity.getCreateTime());
        po.setDeduplicationKey(entity.getDeduplicationKey());
        return po;
    }

    /**
     * PO转换为实体
     */
    private Notification convertToEntity(NotificationPO po) {
        return Notification.builder()
            .notificationId(po.getNotificationId())
            .memberId(po.getMemberId())
            .type(NotificationType.fromCode(po.getType()))
            .title(po.getTitle())
            .content(po.getContent())
            .relatedType(com.rent1.domain.notification.enums.RelatedType.fromCode(po.getRelatedType()))
            .relatedId(po.getRelatedId())
            .status(NotificationStatus.fromCode(po.getStatus()))
            .createTime(po.getCreateTime())
            .readTime(po.getReadTime())
            .deduplicationKey(po.getDeduplicationKey())
            .build();
    }
}