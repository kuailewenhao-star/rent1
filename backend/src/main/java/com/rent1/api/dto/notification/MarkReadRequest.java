package com.rent1.api.dto.notification;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * 标记已读请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarkReadRequest {

    /** 消息ID */
    @NotBlank(message = "消息ID不能为空")
    private String notificationId;
}