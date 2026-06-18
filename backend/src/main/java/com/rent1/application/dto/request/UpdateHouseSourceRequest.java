package com.rent1.application.dto.request;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDate;

/**
 * 编辑房源请求DTO
 * 
 * 接口：PUT /api/house-sources/{houseSourceId}
 * 
 * 核心业务规则：
 * - 仅允许编辑：名称、地址、总户型、承租时间
 * - 房源类型（整租/合租）创建后不可修改
 * - 停用房源不可编辑
 */
@Data
public class UpdateHouseSourceRequest {
    
    /**
     * 房源名称（≤50字符）
     */
    @NotBlank(message = "房源名称不能为空")
    @Size(max = 50, message = "房源名称最长50字符")
    private String name;
    
    /**
     * 省份
     */
    @NotBlank(message = "省份不能为空")
    private String province;
    
    /**
     * 城市
     */
    @NotBlank(message = "城市不能为空")
    private String city;
    
    /**
     * 区县
     */
    @NotBlank(message = "区县不能为空")
    private String district;
    
    /**
     * 详细地址
     */
    @NotBlank(message = "详细地址不能为空")
    private String address;
    
    /**
     * 总户型数
     */
    private Integer totalRooms;
    
    /**
     * 承租起始时间
     */
    private LocalDate leaseStart;
    
    /**
     * 承租结束时间
     */
    private LocalDate leaseEnd;
}