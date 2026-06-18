package com.rent1.infrastructure.persistence.repository;

import com.rent1.domain.contract.entity.RoomInvite;
import com.rent1.domain.contract.repository.RoomInviteRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 房间邀请码仓储实现（内存实现）
 */
@Repository
public class InMemoryRoomInviteRepository implements RoomInviteRepository {

    private final ConcurrentHashMap<String, RoomInvite> store = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, RoomInvite> codeIndex = new ConcurrentHashMap<>();

    @Override
    public Optional<RoomInvite> findByInviteCode(String inviteCode) {
        return Optional.ofNullable(codeIndex.get(inviteCode));
    }

    @Override
    public Optional<RoomInvite> findActiveByRoomId(String roomId) {
        return store.values().stream()
            .filter(i -> roomId.equals(i.getRoomId()))
            .filter(i -> "ACTIVE".equals(i.getStatus()))
            .findFirst();
    }

    @Override
    public RoomInvite save(RoomInvite invite) {
        if (invite.getInviteId() == null) {
            invite.setInviteId("INV_" + System.currentTimeMillis());
        }
        store.put(invite.getInviteId(), invite);
        codeIndex.put(invite.getInviteCode(), invite);
        return invite;
    }

    @Override
    public RoomInvite update(RoomInvite invite) {
        store.put(invite.getInviteId(), invite);
        codeIndex.put(invite.getInviteCode(), invite);
        return invite;
    }
}
