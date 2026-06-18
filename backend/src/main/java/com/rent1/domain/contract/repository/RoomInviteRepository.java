package com.rent1.domain.contract.repository;

import com.rent1.domain.contract.entity.RoomInvite;

import java.util.Optional;

/**
 * 房间邀请码仓储接口
 */
public interface RoomInviteRepository {

    /**
     * 根据邀请码查询
     */
    Optional<RoomInvite> findByInviteCode(String inviteCode);

    /**
     * 根据房间ID查询有效邀请
     */
    Optional<RoomInvite> findActiveByRoomId(String roomId);

    /**
     * 保存邀请
     */
    RoomInvite save(RoomInvite invite);

    /**
     * 更新邀请状态
     */
    RoomInvite update(RoomInvite invite);
}
