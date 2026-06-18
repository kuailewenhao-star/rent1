package com.rent1.api.controller;

import com.rent1.application.notification.service.NotificationAppService;
import com.rent1.domain.notification.entity.Notification;
import com.rent1.domain.notification.enums.NotificationStatus;
import com.rent1.domain.notification.enums.NotificationType;
import com.rent1.common.response.ApiResponse;
import com.rent1.api.dto.notification.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import com.rent1.infrastructure.security.DataScopeContext;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 消息接口控制器
 * 核心职责：
 * 1. 参数接收、基础格式校验、权限拦截、响应封装
 * 2. 禁止包含任何业务逻辑、业务判断
 * 
 * 接口列表：
 * - GET  /api/v1/notifications         查询消息列表（分页）
 * - GET  /api/v1/notifications/unread  查询未读消息列表
 * - GET  /api/v1/notifications/count   统计未读消息数量
 * - GET  /api/v1/notifications/{id}    查询消息详情
 * - PUT  /api/v1/notifications/{id}/read 标记单条消息已读
 * - PUT  /api/v1/notifications/read-all 标记全部消息已读
 */
@Slf4j
@Api(tags = "消息管理")
@RestController
@RequestMapping("/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationAppService notificationAppService;

    /**
     * 查询消息列表（分页）
     * 权限隔离：仅返回当前会员的消息
     * 
     * @param memberId 会员ID（从请求头获取）
     * @param request 查询请求参数
     * @return 消息列表分页响应
     */
    @ApiOperation("查询消息列表（分页）")
    @GetMapping
    public ApiResponse<NotificationPageDTO> queryNotifications(
            @Valid @ModelAttribute NotificationQueryRequest request) {

        String memberId = DataScopeContext.getMemberId();
        log.info("查询消息列表。memberId={}, page={}, pageSize={}, type={}, status={}",
                 memberId, request.getPage(), request.getPageSize(), 
                 request.getType(), request.getStatus());
        
        // 参数校验：page和pageSize已在DTO中通过@Valid校验
        
        List<Notification> notifications;
        
        // 根据请求参数类型查询
        if (request.getType() != null && !request.getType().isEmpty()) {
            // 按类型查询
            NotificationType type = NotificationType.fromCode(request.getType());
            notifications = notificationAppService.queryNotificationsByType(
                memberId, type, request.getPage(), request.getPageSize()
            );
        } else if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            // 按状态查询
            NotificationStatus status = NotificationStatus.fromCode(request.getStatus());
            if (status == NotificationStatus.UNREAD) {
                notifications = notificationAppService.queryUnreadNotifications(
                    memberId, request.getPage(), request.getPageSize()
                );
            } else {
                // 已读消息查询（暂不支持，返回空列表）
                notifications = List.of();
            }
        } else {
            // 查询全部消息
            notifications = notificationAppService.queryNotifications(
                memberId, request.getPage(), request.getPageSize()
            );
        }
        
        // 统计数量
        long total = notificationAppService.countNotifications(memberId);
        long unreadCount = notificationAppService.countUnreadNotifications(memberId);
        
        // 转换为DTO
        List<NotificationListDTO> list = notifications.stream()
            .map(this::convertToListDTO)
            .collect(Collectors.toList());
        
        NotificationPageDTO pageDTO = new NotificationPageDTO();
        pageDTO.setList(list);
        pageDTO.setPage(request.getPage());
        pageDTO.setPageSize(request.getPageSize());
        pageDTO.setTotal(total);
        pageDTO.setUnreadCount(unreadCount);
        
        return ApiResponse.success(pageDTO);
    }

    /**
     * 查询未读消息列表
     * 
     * @param memberId 会员ID
     * @param page 页码
     * @param pageSize 每页数量
     * @return 未读消息列表
     */
    @ApiOperation("查询未读消息列表")
    @GetMapping("/unread")
    public ApiResponse<NotificationPageDTO> queryUnreadNotifications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {

        String memberId = DataScopeContext.getMemberId();
        log.info("查询未读消息列表。memberId={}, page={}, pageSize={}", memberId, page, pageSize);
        
        // 参数校验
        if (page < 1) {
            return ApiResponse.error("PARAM_ERROR", "页码必须大于0");
        }
        if (pageSize < 1) {
            return ApiResponse.error("PARAM_ERROR", "每页数量必须大于0");
        }
        
        List<Notification> notifications = notificationAppService.queryUnreadNotifications(
            memberId, page, pageSize
        );
        
        long total = notificationAppService.countNotifications(memberId);
        long unreadCount = notificationAppService.countUnreadNotifications(memberId);
        
        List<NotificationListDTO> list = notifications.stream()
            .map(this::convertToListDTO)
            .collect(Collectors.toList());
        
        NotificationPageDTO pageDTO = new NotificationPageDTO();
        pageDTO.setList(list);
        pageDTO.setPage(page);
        pageDTO.setPageSize(pageSize);
        pageDTO.setTotal(total);
        pageDTO.setUnreadCount(unreadCount);
        
        return ApiResponse.success(pageDTO);
    }

    /**
     * 统计未读消息数量
     * 
     * @param memberId 会员ID
     * @return 未读消息数量
     */
    @ApiOperation("统计未读消息数量")
    @GetMapping("/count")
    public ApiResponse<UnreadCountDTO> countUnreadNotifications() {

        String memberId = DataScopeContext.getMemberId();
        log.info("统计未读消息数量。memberId={}", memberId);
        
        long unreadCount = notificationAppService.countUnreadNotifications(memberId);
        long totalCount = notificationAppService.countNotifications(memberId);
        
        UnreadCountDTO dto = new UnreadCountDTO(unreadCount, totalCount);
        
        return ApiResponse.success(dto);
    }

    /**
     * 查询消息详情
     * 权限校验：仅能查看自己的消息
     * 
     * @param memberId 会员ID
     * @param notificationId 消息ID
     * @return 消息详情
     */
    @ApiOperation("查询消息详情")
    @GetMapping("/{notificationId}")
    public ApiResponse<NotificationListDTO> getNotificationDetail(
            @PathVariable String notificationId) {

        String memberId = DataScopeContext.getMemberId();
        log.info("查询消息详情。memberId={}, notificationId={}", memberId, notificationId);
        
        // 参数校验
        if (notificationId == null || notificationId.isEmpty()) {
            return ApiResponse.error("PARAM_ERROR", "消息ID不能为空");
        }
        
        try {
            Notification notification = notificationAppService.getNotificationDetail(
                notificationId, memberId
            );
            
            NotificationListDTO dto = convertToListDTO(notification);
            
            return ApiResponse.success(dto);
        } catch (IllegalArgumentException e) {
            log.warn("查询消息详情失败。memberId={}, notificationId={}, error={}",
                     memberId, notificationId, e.getMessage());
            return ApiResponse.error("NOTIFICATION_NOT_FOUND", e.getMessage());
        }
    }

    /**
     * 标记单条消息已读
     * 权限校验：仅能标记自己的消息
     * 
     * @param memberId 会员ID
     * @param notificationId 消息ID
     * @return 操作结果
     */
    @ApiOperation("标记单条消息已读")
    @PutMapping("/{notificationId}/read")
    public ApiResponse<Void> markNotificationAsRead(
            @PathVariable String notificationId) {

        String memberId = DataScopeContext.getMemberId();
        log.info("标记单条消息已读。memberId={}, notificationId={}", memberId, notificationId);
        
        // 参数校验
        if (notificationId == null || notificationId.isEmpty()) {
            return ApiResponse.error("PARAM_ERROR", "消息ID不能为空");
        }
        
        try {
            notificationAppService.markNotificationAsRead(notificationId, memberId);
            
            return ApiResponse.success(null);
        } catch (IllegalArgumentException e) {
            log.warn("标记消息已读失败。memberId={}, notificationId={}, error={}",
                     memberId, notificationId, e.getMessage());
            return ApiResponse.error("NOTIFICATION_ERROR", e.getMessage());
        }
    }

    /**
     * 标记全部消息已读
     * 
     * @param memberId 会员ID
     * @return 操作结果
     */
    @ApiOperation("标记全部消息已读")
    @PutMapping("/read-all")
    public ApiResponse<Void> markAllNotificationsAsRead() {

        String memberId = DataScopeContext.getMemberId();
        log.info("标记全部消息已读。memberId={}", memberId);
        
        notificationAppService.markAllNotificationsAsRead(memberId);
        
        return ApiResponse.success(null);
    }

    /**
     * 实体转换为列表DTO
     */
    private NotificationListDTO convertToListDTO(Notification entity) {
        NotificationListDTO dto = new NotificationListDTO();
        dto.setNotificationId(entity.getNotificationId());
        dto.setType(entity.getType().getCode());
        dto.setTypeName(entity.getType().getName());
        dto.setTitle(entity.getTitle());
        dto.setContent(entity.getContent());
        dto.setRelatedType(entity.getRelatedType().getCode());
        dto.setRelatedId(entity.getRelatedId());
        dto.setStatus(entity.getStatus().getCode());
        dto.setStatusName(entity.getStatus().getName());
        dto.setCreateTime(entity.getCreateTime());
        dto.setReadTime(entity.getReadTime());
        return dto;
    }
}