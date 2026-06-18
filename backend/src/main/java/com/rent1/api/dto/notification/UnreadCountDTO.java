package com.rent1.api.dto.notification;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 消息未读数量响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnreadCountDTO {

    /** 未读消息数量 */
    private long unreadCount;

    /** 消息总数 */
    private long totalCount;
}