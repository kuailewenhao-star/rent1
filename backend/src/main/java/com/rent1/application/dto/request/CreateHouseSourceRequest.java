package com.rent1.application.dto.request;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

/**
 * 新增房源请求DTO
 * 
 * 接口：POST /api/house-sources
 * 
 * 核心业务规则：
 * - 房源名称≤50字符
 * - 省/市/区三级结构化选择
 * - 承租结束时间不能早于开始时间
 * - 整租房源自动生成1间"整套"房间
 * - 合租房源自动生成2间房间
 */
@Data
public class CreateHouseSourceRequest {
    
    /**
     * 房源名称（必填，≤50字符）
     */
    @NotBlank(message = "房源名称不能为空")
    @Size(max = 50, message = "房源名称最长50字符")
    private String name;
    
    /**
     * 省份（必填）
     */
    @NotBlank(message = "省份不能为空")
    private String province;
    
    /**
     * 城市（必填）
     */
    @NotBlank(message = "城市不能为空")
    private String city;
    
    /**
     * 区县（必填）
     */
    @NotBlank(message = "区县不能为空")
    private String district;
    
    /**
     * 详细地址（必填）
     */
    @NotBlank(message = "详细地址不能为空")
    private String address;
    
    /**
     * 总户型数（必填）
     */
    @NotNull(message = "总户型数不能为空")
    private Integer totalRooms;
    
    /**
     * 房源类型（必填）：ENTIRE整租/SHARED合租
     */
    @NotBlank(message = "房源类型不能为空")
    private String type;
    
    /**
     * 承租起始时间（必填）
     */
    @NotNull(message = "承租起始时间不能为空")
    private LocalDate leaseStart;
    
    /**
     * 承租结束时间（必填）
     */
    @NotNull(message = "承租结束时间不能为空")
    private LocalDate leaseEnd;
}