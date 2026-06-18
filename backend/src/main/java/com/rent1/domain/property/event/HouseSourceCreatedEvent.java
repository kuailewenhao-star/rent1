package com.rent1.domain.property.event;

import lombok.Getter;
import java.time.LocalDateTime;

/**
 * 房源创建事件
 * 
 * 触发时机：房源创建成功
 * 订阅方：初始化计费规则模板
 */
@Getter
public class HouseSourceCreatedEvent {
    
    /**
     * 房源ID
     */
    private final String houseSourceId;
    
    /**
     * 房东会员ID
     */
    private final String landlordMemberId;
    
    /**
     * 房源类型
     */
    private final String type;
    
    /**
     * 创建的房间列表
     */
    private final java.util.List<String> roomIds;
    
    /**
     * 事件发生时间
     */
    private final LocalDateTime occurredOn;
    
    public HouseSourceCreatedEvent(String houseSourceId, String landlordMemberId, 
                                   String type, java.util.List<String> roomIds) {
        this.houseSourceId = houseSourceId;
        this.landlordMemberId = landlordMemberId;
        this.type = type;
        this.roomIds = roomIds;
        this.occurredOn = LocalDateTime.now();
    }
}