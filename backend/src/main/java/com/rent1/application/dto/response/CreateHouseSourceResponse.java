package com.rent1.application.dto.response;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 新增房源响应DTO
 * 
 * 接口：POST /api/house-sources
 * 
 * 出参：
 * - houseSourceId：房源ID
 * - rooms：生成的房间列表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateHouseSourceResponse {
    
    /**
     * 房源ID
     */
    private String houseSourceId;
    
    /**
     * 生成的房间列表
     */
    private List<RoomInfo> rooms;
    
    /**
     * 房间信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoomInfo {
        
        /**
         * 房间ID
         */
        private String roomId;
        
        /**
         * 房间名称
         */
        private String roomName;
    }
}