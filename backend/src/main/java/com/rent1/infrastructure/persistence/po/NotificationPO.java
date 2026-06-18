package com.rent1.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息持久化对象（PO）
 * 对应数据库表：notification
 */
@Data
@TableName("notification")
public class NotificationPO {

    /** 消息ID */
    @TableId(type = IdType.ASSIGN_ID)
    private String notificationId;

    /** 接收者会员ID */
    private String memberId;

    /** 消息类型 */
    private String type;

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

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 阅读时间 */
    private LocalDateTime readTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 去重标识 */
    private String deduplicationKey;
}