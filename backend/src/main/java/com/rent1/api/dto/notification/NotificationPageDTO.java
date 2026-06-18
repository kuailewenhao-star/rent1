package com.rent1.api.dto.notification;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 消息列表分页响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPageDTO {

    /** 消息列表 */
    private List<NotificationListDTO> list;

    /** 当前页码 */
    private int page;

    /** 每页数量 */
    private int pageSize;

    /** 总数量 */
    private long total;

    /** 未读数量 */
    private long unreadCount;
}