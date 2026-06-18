package com.rent1.domain.contract.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 房间邀请码实体单元测试
 */
@DisplayName("房间邀请码实体测试")
class RoomInviteTest {

    @Test
    @DisplayName("有效邀请码校验 - ACTIVE状态且未过期")
    void isValid_ActiveAndNotExpired_ShouldReturnTrue() {
        // given
        RoomInvite invite = RoomInvite.builder()
            .status("ACTIVE")
            .expireTime(LocalDateTime.now().plusHours(1))
            .build();

        // when & then
        assertTrue(invite.isValid());
    }

    @Test
    @DisplayName("无效邀请码校验 - 已使用")
    void isValid_UsedStatus_ShouldReturnFalse() {
        // given
        RoomInvite invite = RoomInvite.builder()
            .status("USED")
            .expireTime(LocalDateTime.now().plusHours(1))
            .build();

        // when & then
        assertFalse(invite.isValid());
    }

    @Test
    @DisplayName("无效邀请码校验 - 已过期")
    void isValid_Expired_ShouldReturnFalse() {
        // given
        RoomInvite invite = RoomInvite.builder()
            .status("ACTIVE")
            .expireTime(LocalDateTime.now().minusHours(1))
            .build();

        // when & then
        assertFalse(invite.isValid());
    }

    @Test
    @DisplayName("无效邀请码校验 - 已过期且已使用")
    void isValid_ExpiredAndUsed_ShouldReturnFalse() {
        // given
        RoomInvite invite = RoomInvite.builder()
            .status("USED")
            .expireTime(LocalDateTime.now().minusHours(1))
            .build();

        // when & then
        assertFalse(invite.isValid());
    }
}
