package com.rent1.api.dto.notification;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 消息查询请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationQueryRequest {

    /** 页码（默认1） */
    @Min(value = 1, message = "页码必须大于0")
    private int page = 1;

    /** 每页数量（默认20） */
    @Min(value = 1, message = "每页数量必须大于0")
    private int pageSize = 20;

    /** 消息类型（可选） */
    private String type;

    /** 消息状态（可选） */
    private String status;
}