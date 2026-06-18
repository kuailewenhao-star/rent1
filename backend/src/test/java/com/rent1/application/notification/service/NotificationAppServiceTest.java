package com.rent1.application.notification.service;

import com.rent1.domain.notification.entity.Notification;
import com.rent1.domain.notification.enums.NotificationStatus;
import com.rent1.domain.notification.enums.NotificationType;
import com.rent1.domain.notification.enums.RelatedType;
import com.rent1.domain.notification.repository.NotificationRepository;
import com.rent1.domain.notification.service.NotificationDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * 消息应用服务单元测试
 * 测试覆盖范围：
 * 1. 查询消息列表（正常流程、权限隔离）
 * 2. 查询未读消息列表（正常流程）
 * 3. 统计未读消息数量（正常流程）
 * 4. 标记单条消息已读（正常流程、异常场景）
 * 5. 标记全部消息已读（正常流程）
 * 6. 查询消息详情（正常流程、权限校验、异常场景）
 */
@ExtendWith(MockitoExtension.class)
class NotificationAppServiceTest {

    @Mock
    private NotificationDomainService notificationDomainService;

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationAppService notificationAppService;

    private String memberId;
    private String notificationId;
    private Notification testNotification;

    @BeforeEach
    void setUp() {
        memberId = "member_001";
        notificationId = "ntf_001";
        
        testNotification = Notification.builder()
            .notificationId(notificationId)
            .memberId(memberId)
            .type(NotificationType.RENT_REMINDER)
            .title("租金催收提醒")
            .content("您承租的房间租金账单待支付")
            .relatedType(RelatedType.INVOICE)
            .relatedId("invoice_001")
            .status(NotificationStatus.UNREAD)
            .createTime(LocalDateTime.now())
            .build();
    }

    // ==================== 查询消息列表测试 ====================

    @Test
    @DisplayName("查询消息列表-正常流程")
    void testQueryNotifications_Success() {
        // Given: 存在消息列表
        List<Notification> notifications = List.of(testNotification);
        when(notificationRepository.findByMemberId(any(), anyInt(), anyInt()))
            .thenReturn(notifications);

        // When: 查询消息列表
        List<Notification> result = notificationAppService.queryNotifications(memberId, 1, 20);

        // Then: 验证返回结果
        assertEquals(1, result.size());
        assertEquals(notificationId, result.get(0).getNotificationId());
        verify(notificationRepository, times(1)).findByMemberId(memberId, 1, 20);
    }

    @Test
    @DisplayName("查询消息列表-空列表")
    void testQueryNotifications_EmptyList() {
        // Given: 无消息
        when(notificationRepository.findByMemberId(any(), anyInt(), anyInt()))
            .thenReturn(List.of());

        // When: 查询消息列表
        List<Notification> result = notificationAppService.queryNotifications(memberId, 1, 20);

        // Then: 验证返回空列表
        assertEquals(0, result.size());
    }

    // ==================== 查询未读消息列表测试 ====================

    @Test
    @DisplayName("查询未读消息列表-正常流程")
    void testQueryUnreadNotifications_Success() {
        // Given: 存在未读消息
        List<Notification> notifications = List.of(testNotification);
        when(notificationRepository.findByMemberIdAndStatus(any(), any(), anyInt(), anyInt()))
            .thenReturn(notifications);

        // When: 查询未读消息列表
        List<Notification> result = notificationAppService.queryUnreadNotifications(memberId, 1, 20);

        // Then: 验证返回结果
        assertEquals(1, result.size());
        verify(notificationRepository, times(1)).findByMemberIdAndStatus(
            memberId, NotificationStatus.UNREAD, 1, 20
        );
    }

    // ==================== 统计未读消息数量测试 ====================

    @Test
    @DisplayName("统计未读消息数量-正常流程")
    void testCountUnreadNotifications_Success() {
        // Given: 存在未读消息
        when(notificationRepository.countUnreadByMemberId(any())).thenReturn(5L);

        // When: 统计未读消息数量
        long count = notificationAppService.countUnreadNotifications(memberId);

        // Then: 验证返回结果
        assertEquals(5L, count);
        verify(notificationRepository, times(1)).countUnreadByMemberId(memberId);
    }

    @Test
    @DisplayName("统计消息总数-正常流程")
    void testCountNotifications_Success() {
        // Given: 存在消息
        when(notificationRepository.countByMemberId(any())).thenReturn(10L);

        // When: 统计消息总数
        long count = notificationAppService.countNotifications(memberId);

        // Then: 验证返回结果
        assertEquals(10L, count);
    }

    // ==================== 标记单条消息已读测试 ====================

    @Test
    @DisplayName("标记单条消息已读-正常流程")
    void testMarkNotificationAsRead_Success() {
        // When: 标记已读
        notificationAppService.markNotificationAsRead(notificationId, memberId);

        // Then: 验证调用领域服务
        verify(notificationDomainService, times(1)).markAsRead(notificationId, memberId);
    }

    // ==================== 标记全部消息已读测试 ====================

    @Test
    @DisplayName("标记全部消息已读-正常流程")
    void testMarkAllNotificationsAsRead_Success() {
        // When: 标记全部已读
        notificationAppService.markAllNotificationsAsRead(memberId);

        // Then: 验证调用领域服务
        verify(notificationDomainService, times(1)).markAllAsRead(memberId);
    }

    // ==================== 查询消息详情测试 ====================

    @Test
    @DisplayName("查询消息详情-正常流程")
    void testGetNotificationDetail_Success() {
        // Given: 存在消息，属于当前会员
        when(notificationRepository.findById(any())).thenReturn(testNotification);

        // When: 查询消息详情
        Notification result = notificationAppService.getNotificationDetail(notificationId, memberId);

        // Then: 验证返回结果
        assertEquals(notificationId, result.getNotificationId());
        assertEquals(memberId, result.getMemberId());
    }

    @Test
    @DisplayName("查询消息详情-消息不存在-抛出异常")
    void testGetNotificationDetail_NotFound_ThrowException() {
        // Given: 消息不存在
        when(notificationRepository.findById(any())).thenReturn(null);

        // When & Then: 验证抛出异常
        assertThrows(IllegalArgumentException.class, () -> {
            notificationAppService.getNotificationDetail(notificationId, memberId);
        });
    }

    @Test
    @DisplayName("查询消息详情-无权限-抛出异常")
    void testGetNotificationDetail_NoPermission_ThrowException() {
        // Given: 存在消息，但不属于当前会员
        Notification otherNotification = Notification.builder()
            .notificationId(notificationId)
            .memberId("other_member")
            .build();
        when(notificationRepository.findById(any())).thenReturn(otherNotification);

        // When & Then: 验证抛出异常
        assertThrows(IllegalArgumentException.class, () -> {
            notificationAppService.getNotificationDetail(notificationId, memberId);
        });
    }

    // ==================== 发送提醒测试 ====================

    @Test
    @DisplayName("发送合约到期提醒-正常流程")
    void testSendContractExpiryReminder_Success() {
        // When: 发送合约到期提醒
        notificationAppService.sendContractExpiryReminder(
            "contract_001", "landlord_001", "tenant_001", "room_001",
            7, "房间A", "房源X"
        );

        // Then: 验证调用领域服务
        verify(notificationDomainService, times(1)).sendContractExpiringReminder(
            any(), any(), any(), any(), anyInt(), any(), any()
        );
    }

    @Test
    @DisplayName("发送租金催收提醒-正常流程")
    void testSendRentReminder_Success() {
        // When: 发送租金催收提醒
        notificationAppService.sendRentReminder(
            "invoice_001", "tenant_001", "房间A", "1000.00", "2026-06-20", false
        );

        // Then: 验证调用领域服务
        verify(notificationDomainService, times(1)).sendDailyRentReminder(
            any(), any(), any(), any(), any(), anyBoolean()
        );
    }

    @Test
    @DisplayName("发送杂费单次提醒-正常流程")
    void testSendMiscFeeReminder_Success() {
        // When: 发送杂费单次提醒
        notificationAppService.sendMiscFeeReminder(
            "invoice_001", "tenant_001", "水费", "房间A", "50.00"
        );

        // Then: 验证调用领域服务
        verify(notificationDomainService, times(1)).sendOneTimeReminder(
            any(), any(), any(), any(), any()
        );
    }

    @Test
    @DisplayName("发送合约状态变更通知-正常流程")
    void testSendContractStatusChangeNotification_Success() {
        // When: 发送合约状态变更通知
        notificationAppService.sendContractStatusChangeNotification(
            "contract_001", "landlord_001", "tenant_001", "EARLY_TERMINATED",
            "房间A", "房源X"
        );

        // Then: 验证调用领域服务
        verify(notificationDomainService, times(1)).sendContractStatusChangeNotification(
            any(), any(), any(), any(), any(), any()
        );
    }

    @Test
    @DisplayName("发送公摊账单通知-正常流程")
    void testSendSharedBillNotice_Success() {
        // When: 发送公摊账单通知
        notificationAppService.sendSharedBillNotice(
            "invoice_001", "tenant_001", "电费", "房间A", "100.00", "50%"
        );

        // Then: 验证调用领域服务
        verify(notificationDomainService, times(1)).sendSharedBillNotice(
            any(), any(), any(), any(), any(), any()
        );
    }
}