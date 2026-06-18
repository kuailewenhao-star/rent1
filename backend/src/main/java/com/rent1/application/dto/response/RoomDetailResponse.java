package com.rent1.application.dto.response;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 房间详情响应DTO
 * 
 * 接口：GET /api/rooms/{roomId}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomDetailResponse {
    
    /**
     * 房间ID
     */
    private String roomId;
    
    /**
     * 所属房源ID
     */
    private String houseSourceId;
    
    /**
     * 所属房源名称
     */
    private String houseSourceName;
    
    /**
     * 房间名称
     */
    private String roomName;
    
    /**
     * 房间面积（平方米）
     */
    private BigDecimal area;
    
    /**
     * 月租金额
     */
    private BigDecimal monthlyRent;
    
    /**
     * 押金金额
     */
    private BigDecimal deposit;
    
    /**
     * 房间图片URL列表
     */
    private List<String> images;
    
    /**
     * 房间状态：VACANT空置中/OCCUPIED已出租
     */
    private String status;
    
    /**
     * 房间状态名称
     */
    private String statusName;
    
    /**
     * 是否即将到期（合约剩余≤30天）
     */
    private Boolean expiringSoon;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}