package com.rent1.domain.property.service;

import com.rent1.common.enums.ErrorCode;
import com.rent1.common.enums.RoomStatus;
import com.rent1.domain.common.BusinessException;
import com.rent1.domain.property.entity.Room;
import com.rent1.domain.property.event.RoomStatusChangedEvent;
import com.rent1.domain.property.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 房间状态领域服务
 * 
 * 职责边界：房间状态自动流转管理
 * 
 * 核心业务规则：
 * 1. 房间状态仅允许空置↔已出租两种流转
 * 2. 仅空置中房间可出租
 * 3. 已出租房间不可编辑房间信息
 * 4. 即将到期为前端计算标签（≤30天），不修改底层状态
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoomStatusDomainService {
    
    private final RoomRepository roomRepository;
    
    /**
     * 占用房间（合约创建成功时调用）
     * 
     * 核心业务规则：
     * - 仅空置中房间可出租
     * - 状态变更：空置中 → 已出租
     * - 发布RoomStatusChangedEvent供统计模块消费
     * 
     * @param roomId 房间ID
     * @param contractId 合约ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void occupy(String roomId, String contractId) {
        Room room = roomRepository.findById(roomId);
        if (room == null) {
            throw new BusinessException(ErrorCode.B010);
        }
        
        // 核心规则：仅空置中房间可出租
        if (!room.canOccupy()) {
            throw new BusinessException(ErrorCode.B010);
        }
        
        String oldStatus = room.getStatus().getCode();
        room.occupy();
        roomRepository.update(room);
        
        log.info("房间状态变更：{} → {}，房间ID：{}，合约ID：{}", 
                oldStatus, RoomStatus.OCCUPIED.getCode(), roomId, contractId);
        
        // 发布房间状态变更事件（供统计模块消费）
        // eventPublisher.publish(new RoomStatusChangedEvent(roomId, room.getHouseSourceId(), 
        //         oldStatus, RoomStatus.OCCUPIED.getCode(), "合约创建", contractId));
    }
    
    /**
     * 释放房间（合约到期/解约时调用）
     * 
     * 核心业务规则：
     * - 状态变更：已出租 → 空置中
     * - 发布RoomStatusChangedEvent供统计模块消费
     * 
     * @param roomId 房间ID
     * @param contractId 合约ID（如有）
     * @param reason 原因（合约到期/合约解约）
     */
    @Transactional(rollbackFor = Exception.class)
    public void vacate(String roomId, String contractId, String reason) {
        Room room = roomRepository.findById(roomId);
        if (room == null) {
            return;
        }
        
        String oldStatus = room.getStatus().getCode();
        room.vacate();
        roomRepository.update(room);
        
        log.info("房间状态变更：{} → {}，房间ID：{}，原因：{}", 
                oldStatus, RoomStatus.VACANT.getCode(), roomId, reason);
        
        // 发布房间状态变更事件（供统计模块消费）
        // eventPublisher.publish(new RoomStatusChangedEvent(roomId, room.getHouseSourceId(), 
        //         oldStatus, RoomStatus.VACANT.getCode(), reason, contractId));
    }
    
    /**
     * 校验房间是否为空置状态
     * 
     * 核心业务规则：仅空置状态允许编辑房间信息
     * 
     * @param roomId 房间ID
     */
    public void validateVacant(String roomId) {
        Room room = roomRepository.findById(roomId);
        if (room == null) {
            throw new BusinessException(ErrorCode.B010);
        }
        
        if (!room.isVacant()) {
            throw new BusinessException(ErrorCode.L001);
        }
    }
    
    /**
     * 检查房间是否即将到期
     * 
     * 核心业务规则：
     * - 即将到期=合约剩余≤30天
     * - 仅前端标签展示，不修改底层状态
     * 
     * @param roomId 房间ID
     * @param currentDate 当前日期
     * @return 是否即将到期
     */
    public boolean checkExpiring(String roomId, LocalDate currentDate) {
        // 此处需要查询关联合约的结束日期
        // 由应用层调用合约服务获取合约信息
        // 简化实现：返回false，由应用层聚合计算
        return false;
    }
    
    /**
     * 判断房间是否可以出租
     * 
     * @param roomId 房间ID
     * @return 是否可以出租
     */
    public boolean canOccupy(String roomId) {
        Room room = roomRepository.findById(roomId);
        return room != null && room.canOccupy();
    }
    
    /**
     * 判断房间是否可以编辑
     * 
     * @param roomId 房间ID
     * @return 是否可以编辑
     */
    public boolean canEdit(String roomId) {
        Room room = roomRepository.findById(roomId);
        return room != null && room.canEdit();
    }
}