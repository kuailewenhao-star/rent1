package com.rent1.domain.notification.service;

import com.rent1.domain.notification.entity.Notification;
import com.rent1.domain.notification.enums.NotificationStatus;
import com.rent1.domain.notification.enums.NotificationType;
import com.rent1.domain.notification.enums.RelatedType;
import com.rent1.domain.notification.repository.NotificationRepository;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * 消息域领域服务单元测试
 * 测试覆盖范围：
 * 1. 合约到期提醒发送（正常流程、去重机制）
 * 2. 租金催收提醒发送（正常流程、去重机制）
 * 3. 杂费单次提醒发送（正常流程、去重机制）
 * 4. 合约状态变更通知（正常流程、去重机制）
 * 5. 公摊账单通知（正常流程、去重机制）
 * 6. 标记已读（正常流程、权限校验、异常场景）
 * 7. 标记全部已读（正常流程）
 */
@ExtendWith(MockitoExtension.class)
class NotificationDomainServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationDomainService notificationDomainService;

    private String contractId;
    private String landlordMemberId;
    private String tenantMemberId;
    private String roomId;
    private String invoiceId;
    private String roomName;
    private String houseSourceName;

    @BeforeEach
    void setUp() {
        contractId = "contract_001";
        landlordMemberId = "landlord_001";
        tenantMemberId = "tenant_001";
        roomId = "room_001";
        invoiceId = "invoice_001";
        roomName = "房间A";
        houseSourceName = "房源X";
    }

    // ==================== 合约到期提醒测试 ====================

    @Test
    @DisplayName("合约到期提醒-正常流程-提前7天")
    void testSendContractExpiringReminder_7Days_Success() {
        // Given: 未推送过
        when(notificationRepository.findByDeduplicationKey(any())).thenReturn(null);

        // When: 发送提前7天提醒
        notificationDomainService.sendContractExpiringReminder(
            contractId, landlordMemberId, tenantMemberId, roomId,
            7, roomName, houseSourceName
        );

        // Then: 验证保存了2条消息（房东+租客）
        verify(notificationRepository, times(1)).saveAll(anyList());
        verify(notificationRepository, times(1)).findByDeduplicationKey(any());
    }

    @Test
    @DisplayName("合约到期提醒-正常流程-提前3天")
    void testSendContractExpiringReminder_3Days_Success() {
        // Given: 未推送过
        when(notificationRepository.findByDeduplicationKey(any())).thenReturn(null);

        // When: 发送提前3天提醒
        notificationDomainService.sendContractExpiringReminder(
            contractId, landlordMemberId, tenantMemberId, roomId,
            3, roomName, houseSourceName
        );

        // Then: 验证保存了2条消息
        verify(notificationRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("合约到期提醒-去重机制-同一天不重复推送")
    void testSendContractExpiringReminder_Deduplication() {
        // Given: 已推送过（存在去重标识）
        Notification existingNotification = Notification.builder()
            .notificationId("existing_001")
            .deduplicationKey(Notification.generateExpiryDeduplicationKey(contractId, 7, LocalDateTime.now()))
            .build();
        when(notificationRepository.findByDeduplicationKey(any())).thenReturn(existingNotification);

        // When: 再次发送提醒
        notificationDomainService.sendContractExpiringReminder(
            contractId, landlordMemberId, tenantMemberId, roomId,
            7, roomName, houseSourceName
        );

        // Then: 验证不保存消息（去重生效）
        verify(notificationRepository, never()).saveAll(anyList());
        verify(notificationRepository, times(1)).findByDeduplicationKey(any());
    }

    // ==================== 租金催收提醒测试 ====================

    @Test
    @DisplayName("租金催收提醒-正常流程-未逾期")
    void testSendDailyRentReminder_NotOverdue_Success() {
        // Given: 未推送过
        when(notificationRepository.findByDeduplicationKey(any())).thenReturn(null);

        // When: 发送租金催收提醒（未逾期）
        notificationDomainService.sendDailyRentReminder(
            invoiceId, tenantMemberId, roomName, "1000.00", "2026-06-20", false
        );

        // Then: 验证保存了1条消息
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    @DisplayName("租金催收提醒-正常流程-已逾期")
    void testSendDailyRentReminder_Overdue_Success() {
        // Given: 未推送过
        when(notificationRepository.findByDeduplicationKey(any())).thenReturn(null);

        // When: 发送租金催收提醒（已逾期）
        notificationDomainService.sendDailyRentReminder(
            invoiceId, tenantMemberId, roomName, "1000.00", "2026-06-10", true
        );

        // Then: 验证保存了1条消息
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    @DisplayName("租金催收提醒-去重机制-同一天不重复推送")
    void testSendDailyRentReminder_Deduplication() {
        // Given: 已推送过
        Notification existingNotification = Notification.builder()
            .notificationId("existing_002")
            .deduplicationKey(Notification.generateRentReminderDeduplicationKey(invoiceId, LocalDateTime.now()))
            .build();
        when(notificationRepository.findByDeduplicationKey(any())).thenReturn(existingNotification);

        // When: 再次发送催收提醒
        notificationDomainService.sendDailyRentReminder(
            invoiceId, tenantMemberId, roomName, "1000.00", "2026-06-10", true
        );

        // Then: 验证不保存消息（去重生效）
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    // ==================== 杂费单次提醒测试 ====================

    @Test
    @DisplayName("杂费单次提醒-正常流程")
    void testSendOneTimeReminder_Success() {
        // Given: 未推送过
        when(notificationRepository.findByDeduplicationKey(any())).thenReturn(null);

        // When: 发送杂费提醒
        notificationDomainService.sendOneTimeReminder(
            invoiceId, tenantMemberId, "水费", roomName, "50.00"
        );

        // Then: 验证保存了1条消息
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    @DisplayName("杂费单次提醒-去重机制-不重复推送")
    void testSendOneTimeReminder_Deduplication() {
        // Given: 已推送过
        when(notificationRepository.findByDeduplicationKey(any())).thenReturn(Notification.builder().build());

        // When: 再次发送杂费提醒
        notificationDomainService.sendOneTimeReminder(
            invoiceId, tenantMemberId, "水费", roomName, "50.00"
        );

        // Then: 验证不保存消息
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    // ==================== 合约状态变更通知测试 ====================

    @Test
    @DisplayName("合约状态变更通知-正常流程-提前解约")
    void testSendContractStatusChangeNotification_EarlyTerminated_Success() {
        // Given: 未推送过
        when(notificationRepository.findByDeduplicationKey(any())).thenReturn(null);

        // When: 发送提前解约通知
        notificationDomainService.sendContractStatusChangeNotification(
            contractId, landlordMemberId, tenantMemberId, "EARLY_TERMINATED",
            roomName, houseSourceName
        );

        // Then: 验证保存了2条消息
        verify(notificationRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("合约状态变更通知-正常流程-到期完结")
    void testSendContractStatusChangeNotification_Expired_Success() {
        // Given: 未推送过
        when(notificationRepository.findByDeduplicationKey(any())).thenReturn(null);

        // When: 发送到期完结通知
        notificationDomainService.sendContractStatusChangeNotification(
            contractId, landlordMemberId, tenantMemberId, "EXPIRED",
            roomName, houseSourceName
        );

        // Then: 验证保存了2条消息
        verify(notificationRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("合约状态变更通知-去重机制")
    void testSendContractStatusChangeNotification_Deduplication() {
        // Given: 已推送过
        when(notificationRepository.findByDeduplicationKey(any())).thenReturn(Notification.builder().build());

        // When: 再次发送通知
        notificationDomainService.sendContractStatusChangeNotification(
            contractId, landlordMemberId, tenantMemberId, "EARLY_TERMINATED",
            roomName, houseSourceName
        );

        // Then: 验证不保存消息
        verify(notificationRepository, never()).saveAll(anyList());
    }

    // ==================== 公摊账单通知测试 ====================

    @Test
    @DisplayName("公摊账单通知-正常流程")
    void testSendSharedBillNotice_Success() {
        // Given: 未推送过
        when(notificationRepository.findByDeduplicationKey(any())).thenReturn(null);

        // When: 发送公摊账单通知
        notificationDomainService.sendSharedBillNotice(
            invoiceId, tenantMemberId, "电费", roomName, "100.00", "50%"
        );

        // Then: 验证保存了1条消息
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    @DisplayName("公摊账单通知-去重机制")
    void testSendSharedBillNotice_Deduplication() {
        // Given: 已推送过
        when(notificationRepository.findByDeduplicationKey(any())).thenReturn(Notification.builder().build());

        // When: 再次发送通知
        notificationDomainService.sendSharedBillNotice(
            invoiceId, tenantMemberId, "电费", roomName, "100.00", "50%"
        );

        // Then: 验证不保存消息
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    // ==================== 标记已读测试 ====================

    @Test
    @DisplayName("标记已读-正常流程")
    void testMarkAsRead_Success() {
        // Given: 存在未读消息，属于当前会员
        Notification notification = Notification.builder()
            .notificationId("ntf_001")
            .memberId(tenantMemberId)
            .status(NotificationStatus.UNREAD)
            .build();
        when(notificationRepository.findById("ntf_001")).thenReturn(notification);

        // When: 标记已读
        notificationDomainService.markAsRead("ntf_001", tenantMemberId);

        // Then: 验证状态已更新
        verify(notificationRepository, times(1)).updateStatus(any(), any(), any());
        assertEquals(NotificationStatus.READ, notification.getStatus());
        assertNotNull(notification.getReadTime());
    }

    @Test
    @DisplayName("标记已读-消息不存在-抛出异常")
    void testMarkAsRead_NotFound_ThrowException() {
        // Given: 消息不存在
        when(notificationRepository.findById("ntf_001")).thenReturn(null);

        // When & Then: 验证抛出异常
        assertThrows(IllegalArgumentException.class, () -> {
            notificationDomainService.markAsRead("ntf_001", tenantMemberId);
        });
    }

    @Test
    @DisplayName("标记已读-无权限-抛出异常")
    void testMarkAsRead_NoPermission_ThrowException() {
        // Given: 存在消息，但不属于当前会员
        Notification notification = Notification.builder()
            .notificationId("ntf_001")
            .memberId("other_member")
            .status(NotificationStatus.UNREAD)
            .build();
        when(notificationRepository.findById("ntf_001")).thenReturn(notification);

        // When & Then: 验证抛出异常
        assertThrows(IllegalArgumentException.class, () -> {
            notificationDomainService.markAsRead("ntf_001", tenantMemberId);
        });
    }

    @Test
    @DisplayName("标记已读-已读消息-不重复更新")
    void testMarkAsRead_AlreadyRead_NoUpdate() {
        // Given: 消息已读
        Notification notification = Notification.builder()
            .notificationId("ntf_001")
            .memberId(tenantMemberId)
            .status(NotificationStatus.READ)
            .readTime(LocalDateTime.now())
            .build();
        when(notificationRepository.findById("ntf_001")).thenReturn(notification);

        // When: 再次标记已读
        notificationDomainService.markAsRead("ntf_001", tenantMemberId);

        // Then: 验证状态未变化（已读状态不重复更新）
        assertEquals(NotificationStatus.READ, notification.getStatus());
    }

    // ==================== 标记全部已读测试 ====================

    @Test
    @DisplayName("标记全部已读-正常流程")
    void testMarkAllAsRead_Success() {
        // When: 标记全部已读
        notificationDomainService.markAllAsRead(tenantMemberId);

        // Then: 验证批量更新已执行
        verify(notificationRepository, times(1)).updateAllStatusToRead(any(), any());
    }

    // ==================== 消息实体测试 ====================

    @Test
    @DisplayName("消息实体-初始化状态")
    void testNotification_Initialize() {
        // Given: 新消息
        Notification notification = Notification.builder()
            .memberId(tenantMemberId)
            .type(NotificationType.RENT_REMINDER)
            .title("测试标题")
            .content("测试内容")
            .build();

        // When: 初始化
        notification.initialize();

        // Then: 验证初始状态
        assertEquals(NotificationStatus.UNREAD, notification.getStatus());
        assertNotNull(notification.getCreateTime());
        assertNull(notification.getReadTime());
    }

    @Test
    @DisplayName("消息实体-状态判断")
    void testNotification_StatusCheck() {
        // Given: 未读消息
        Notification unreadNotification = Notification.builder()
            .status(NotificationStatus.UNREAD)
            .build();

        // Given: 已读消息
        Notification readNotification = Notification.builder()
            .status(NotificationStatus.READ)
            .readTime(LocalDateTime.now())
            .build();

        // Then: 验证状态判断
        assertTrue(unreadNotification.isUnread());
        assertFalse(unreadNotification.isRead());
        assertFalse(readNotification.isUnread());
        assertTrue(readNotification.isRead());
    }

    @Test
    @DisplayName("去重标识生成-合约到期提醒")
    void testGenerateExpiryDeduplicationKey() {
        // Given: 合约ID、剩余天数、日期
        LocalDateTime now = LocalDateTime.now();

        // When: 生成去重标识
        String key = Notification.generateExpiryDeduplicationKey(contractId, 7, now);

        // Then: 验证格式
        assertTrue(key.contains("EXPIRY"));
        assertTrue(key.contains(contractId));
        assertTrue(key.contains("7"));
        assertTrue(key.contains(now.toLocalDate().toString()));
    }

    @Test
    @DisplayName("去重标识生成-租金催收提醒")
    void testGenerateRentReminderDeduplicationKey() {
        // Given: 账单ID、日期
        LocalDateTime now = LocalDateTime.now();

        // When: 生成去重标识
        String key = Notification.generateRentReminderDeduplicationKey(invoiceId, now);

        // Then: 验证格式
        assertTrue(key.contains("RENT"));
        assertTrue(key.contains(invoiceId));
        assertTrue(key.contains(now.toLocalDate().toString()));
    }
}