package com.rent1.application.dto.request;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * 新增房间请求DTO
 * 
 * 接口：POST /api/house-sources/{houseSourceId}/rooms
 * 
 * 核心业务规则：
 * - 校验房源类型=SHARED（合租）
 * - 整租房源禁止新增房间
 * - 房间名≤20字符
 * - 金额边界：月租0~999999.99，押金0~999999.99
 */
@Data
public class CreateRoomRequest {
    
    /**
     * 房间名称（≤20字符）
     */
    @NotBlank(message = "房间名称不能为空")
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