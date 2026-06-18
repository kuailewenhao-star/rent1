package com.rent1.api.dto.notification;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 消息列表响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationListDTO {

    /** 消息ID */
    private String notificationId;

    /** 消息类型 */
    private String type;

    /** 消息类型名称 */
    private String typeName;

    /** 消息标题 */
    private String title;

    /** 消息内容 */
    private String content;

    /** 关联业务类型 */
    private String relatedType;

    /** 关联业务ID */
    private String relatedId;

    /** 消息状态 */
    private String status;

    /** 消息状态名称 */
    private String statusName;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 阅读时间 */
    private LocalDateTime readTime;
}