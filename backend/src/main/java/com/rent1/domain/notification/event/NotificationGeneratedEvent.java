package com.rent1.domain.notification.event;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 消息生成领域事件
 * 触发时机：消息创建成功
 * 订阅方：消息推送服务（发送站内消息）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationGeneratedEvent {

    /** 消息ID */
    private String notificationId;

    /** 接收者会员ID */
    private String memberId;

    /** 消息类型 */
    private String type;

    /** 消息标题 */
    private String title;

    /** 事件发生时间 */
    private LocalDateTime occurredOn;
}