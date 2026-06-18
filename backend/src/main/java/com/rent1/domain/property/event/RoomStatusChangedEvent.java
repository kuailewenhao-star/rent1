package com.rent1.domain.property.event;

import lombok.Getter;
import java.time.LocalDateTime;

/**
 * 房间状态变更事件
 * 
 * 触发时机：房间状态变更（空置↔已出租）
 * 订阅方：统计模块更新看板数据
 */
@Getter
public class RoomStatusChangedEvent {
    
    /**
     * 房间ID
     */
    private final String roomId;
    
    /**
     * 房源ID
     */
    private final String houseSourceId;
    
    /**
     * 原状态
     */
    private final String oldStatus;
    
    /**
     * 新状态
     */
    private final String newStatus;
    
    /**
     * 变更原因（合约创建/合约到期/合约解约）
     */
    private final String reason;
    
    /**
     * 关联合约ID（如有）
     */
    private final String contractId;
    
    /**
     * 事件发生时间
     */
    private final LocalDateTime occurredOn;
    
    public RoomStatusChangedEvent(String roomId, String houseSourceId, 
                                  String oldStatus, String newStatus,
                                  String reason, String contractId) {
        this.roomId = roomId;
        this.houseSourceId = houseSourceId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.reason = reason;
        this.contractId = contractId;
        this.occurredOn = LocalDateTime.now();
    }
    
    /**
     * 判断是否为占用事件（空置→已出租）
     */
    public boolean isOccupied() {
        return "VACANT".equals(oldStatus) && "OCCUPIED".equals(newStatus);
    }
    
    /**
     * 判断是否为释放事件（已出租→空置）
     */
    public boolean isVacated() {
        return "OCCUPIED".equals(oldStatus) && "VACANT".equals(newStatus);
    }
}