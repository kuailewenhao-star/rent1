package com.rent1.application.dto.response;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 房源详情响应DTO
 * 
 * 接口：GET /api/house-sources/{houseSourceId}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HouseSourceDetailResponse {
    
    /**
     * 房源ID
     */
    private String houseSourceId;
    
    /**
     * 房源名称
     */
    private String name;
    
    /**
     * 省份
     */
    private String province;
    
    /**
     * 城市
     */
    private String city;
    
    /**
     * 区县
     */
    private String district;
    
    /**
     * 详细地址
     */
    private String address;
    
    /**
     * 总户型数
     */
    private Integer totalRooms;
    
    /**
     * 房源类型：ENTIRE整租/SHARED合租
     */
    private String type;
    
    /**
     * 房源类型名称
     */
    private String typeName;
    
    /**
     * 房源业务状态：NORMAL正常/LEASE_EXPIRED到期停用/TERMINATED终止经营/VOID作废
     */
    private String status;
    
    /**
     * 房源业务状态名称
     */
    private String statusName;
    
    /**
     * 承租起始时间
     */
    private LocalDate leaseStart;
    
    /**
     * 承租结束时间
     */
    private LocalDate leaseEnd;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 房间列表
     */
    private List<RoomDetailResponse> rooms;
    
    /**
     * 房间统计
     */
    private RoomStatistics roomStatistics;
    
    /**
     * 房间统计信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoomStatistics {
        
        /**
         * 总房间数
         */
        private Integer total;
        
        /**
         * 空置房间数
         */
        private Integer vacant;
        
        /**
         * 已出租房间数
         */
        private Integer occupied;
    }
}