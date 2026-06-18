package com.rent1.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 房间状态枚举
 * VACANT：空置中
 * OCCUPIED：已出租
 * 
 * 注意：即将到期为前端计算标签（合约剩余≤30天），不修改底层状态
 */
@Getter
public enum RoomStatus {
    
    VACANT("VACANT", "空置中"),
    OCCUPIED("OCCUPIED", "已出租");
    
    @EnumValue
    private final String code;
    private final String name;
    
    RoomStatus(String code, String name) {
        this.code = code;
        this.name = name;
    }
    
    public static RoomStatus fromCode(String code) {
        for (RoomStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown RoomStatus code: " + code);
    }
    
    /**
     * 判断是否可以出租
     * 核心业务规则：仅空置中房间可出租
     */
    public boolean canOccupy() {
        return this == VACANT;
    }
    
    /**
     * 判断是否可以编辑
     * 核心业务规则：仅空置状态允许编辑房间信息
     */
    public boolean canEdit() {
        return this == VACANT;
    }
}