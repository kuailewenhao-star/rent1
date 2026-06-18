package com.rent1.application.dto.request;

import lombok.Data;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * 编辑房间请求DTO
 * 
 * 接口：PUT /api/rooms/{roomId}
 * 
 * 核心业务规则：
 * - 仅空置状态允许编辑
 * - 房间名≤20字符
 * - 金额边界：月租0~999999.99，押金0~999999.99
 */
@Data
public class UpdateRoomRequest {
    
    /**
     * 房间名称（≤20字符）
     */
    @Size(max = 20, message = "房间名最长20字符")
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
}