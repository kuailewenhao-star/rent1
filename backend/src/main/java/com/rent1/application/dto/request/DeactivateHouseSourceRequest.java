package com.rent1.application.dto.request;

import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * 停用房源请求DTO
 * 
 * 接口：PUT /api/house-sources/{houseSourceId}/status
 * 
 * 核心业务规则：
 * - 将status改为LEASE_EXPIRED或TERMINATED
 * - 下属房间不可新增出租、新建账单
 * - V1.0不支持物理删除，仅状态管控
 */
@Data
public class DeactivateHouseSourceRequest {
    
    /**
     * 新状态：LEASE_EXPIRED租期到期停用 / TERMINATED主动终止经营
     */
    @NotBlank(message = "状态不能为空")
    private String status;
    
    /**
     * 原因（可选）
     */
    private String reason;
}