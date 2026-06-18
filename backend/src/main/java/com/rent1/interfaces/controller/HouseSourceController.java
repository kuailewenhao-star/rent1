package com.rent1.apis.controller;

import com.rent1.application.dto.request.CreateHouseSourceRequest;
import com.rent1.application.dto.request.DeactivateHouseSourceRequest;
import com.rent1.application.dto.request.UpdateHouseSourceRequest;
import com.rent1.application.dto.response.CreateHouseSourceResponse;
import com.rent1.application.dto.response.HouseSourceDetailResponse;
import com.rent1.application.service.HouseSourceAppService;
import com.rent1.common.response.ApiResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 房源接口控制器
 * 
 * 职责：房源相关接口的请求接收、参数校验、响应封装
 * 
 * 接口列表：
 * - POST /api/house-sources：新增房源
 * - PUT /api/house-sources/{houseSourceId}：编辑房源
 * - PUT /api/house-sources/{houseSourceId}/status：停用房源
 * - GET /api/house-sources/{houseSourceId}：房源详情
 * - GET /api/house-sources：房源列表
 * 
 * 注意：
 * - 接入层仅做参数接收、基础格式校验、权限拦截、响应封装
 * - 不承载任何业务逻辑、业务判断
 * - 核心业务规则全部下沉领域层
 */
@Api(tags = "房源管理")
@Slf4j
@RestController
@RequestMapping("/house-sources")
@RequiredArgsConstructor
@Validated
public class HouseSourceController {
    
    private final HouseSourceAppService houseSourceAppService;
    
    /**
     * 新增房源
     * 
     * 接口路径：POST /api/house-sources
     * 鉴权要求：房东会员
     * 
     * 核心业务规则：
     * - 房源名称≤50字符
     * - 省/市/区三级结构化选择
     * - 承租结束时间不能早于开始时间
     * - 整租房源自动生成1间"整套"房间
     * - 合租房源自动生成2间房间
     * 
     * @param request 新增房源请求
     * @return 新增房源响应
     */
    @ApiOperation(value = "新增房源", notes = "房东会员新增房源，整租房源自动生成1间房间，合租房源自动生成2间房间")
    @PostMapping
    public ApiResponse<CreateHouseSourceResponse> createHouseSource(
            @Valid @RequestBody CreateHouseSourceRequest request) {
        
        // TODO: 从请求头获取当前用户ID（房东）
        String memberId = "mock_member_id";
        
        CreateHouseSourceResponse response = houseSourceAppService.createHouseSource(memberId, request);
        
        log.info("新增房源成功，房源ID：{}", response.getHouseSourceId());
        
        return ApiResponse.success(response);
    }
    
    /**
     * 编辑房源
     * 
     * 接口路径：PUT /api/house-sources/{houseSourceId}
     * 鉴权要求：房东会员（仅可编辑自有房源）
     * 
     * 核心业务规则：
     * - 仅允许编辑：名称、地址、总户型、承租时间
     * - 房源类型（整租/合租）创建后不可修改
     * - 停用房源不可编辑
     * 
     * @param houseSourceId 房源ID
     * @param request 编辑房源请求
     * @return 成功响应
     */
    @ApiOperation(value = "编辑房源", notes = "房东会员编辑房源基础信息，房源类型不可修改，停用房源不可编辑")
    @PutMapping("/{houseSourceId}")
    public ApiResponse<Void> updateHouseSource(
            @ApiParam(value = "房源ID", required = true) @PathVariable String houseSourceId,
            @Valid @RequestBody UpdateHouseSourceRequest request) {
        
        // TODO: 从请求头获取当前用户ID（房东）
        String memberId = "mock_member_id";
        
        houseSourceAppService.updateHouseSource(memberId, houseSourceId, request);
        
        log.info("编辑房源成功，房源ID：{}", houseSourceId);
        
        return ApiResponse.success();
    }
    
    /**
     * 停用房源
     * 
     * 接口路径：PUT /api/house-sources/{houseSourceId}/status
     * 鉴权要求：房东会员（仅可停用自有房源）
     * 
     * 核心业务规则：
     * - 将status改为LEASE_EXPIRED或TERMINATED
     * - 下属房间不可新增出租、新建账单
     * - V1.0不支持物理删除，仅状态管控
     * 
     * @param houseSourceId 房源ID
     * @param request 停用房源请求
     * @return 成功响应
     */
    @ApiOperation(value = "停用房源", notes = "房东会员停用房源，状态改为租期到期停用或主动终止经营")
    @PutMapping("/{houseSourceId}/status")
    public ApiResponse<Void> deactivateHouseSource(
            @ApiParam(value = "房源ID", required = true) @PathVariable String houseSourceId,
            @Valid @RequestBody DeactivateHouseSourceRequest request) {
        
        // TODO: 从请求头获取当前用户ID（房东）
        String memberId = "mock_member_id";
        
        houseSourceAppService.deactivateHouseSource(memberId, houseSourceId, request);
        
        log.info("停用房源成功，房源ID：{}，新状态：{}", houseSourceId, request.getStatus());
        
        return ApiResponse.success();
    }
    
    /**
     * 房源详情
     * 
     * 接口路径：GET /api/house-sources/{houseSourceId}
     * 鉴权要求：房东会员（仅可查看自有房源）
     * 
     * @param houseSourceId 房源ID
     * @return 房源详情响应
     */
    @ApiOperation(value = "房源详情", notes = "房东会员查看房源详情，包含房间列表和房间统计")
    @GetMapping("/{houseSourceId}")
    public ApiResponse<HouseSourceDetailResponse> getHouseSourceDetail(
            @ApiParam(value = "房源ID", required = true) @PathVariable String houseSourceId) {
        
        // TODO: 从请求头获取当前用户ID（房东）
        String memberId = "mock_member_id";
        
        HouseSourceDetailResponse response = houseSourceAppService.getHouseSourceDetail(memberId, houseSourceId);
        
        log.info("查询房源详情成功，房源ID：{}", houseSourceId);
        
        return ApiResponse.success(response);
    }
    
    /**
     * 房源列表
     * 
     * 接口路径：GET /api/house-sources
     * 鉴权要求：房东会员（仅可查看自有房源）
     * 
     * @param status 状态筛选（可选）
     * @param type 类型筛选（可选）
     * @param city 城市筛选（可选）
     * @return 房源列表响应
     */
    @ApiOperation(value = "房源列表", notes = "房东会员查看房源列表，支持状态、类型、城市筛选")
    @GetMapping
    public ApiResponse<List<HouseSourceDetailResponse>> queryHouseSources(
            @ApiParam(value = "状态筛选") @RequestParam(required = false) String status,
            @ApiParam(value = "类型筛选") @RequestParam(required = false) String type,
            @ApiParam(value = "城市筛选") @RequestParam(required = false) String city) {
        
        // TODO: 从请求头获取当前用户ID（房东）
        String memberId = "mock_member_id";
        
        List<HouseSourceDetailResponse> response = houseSourceAppService.queryHouseSources(memberId, status, type, city);
        
        log.info("查询房源列表成功，房东ID：{}，数量：{}", memberId, response.size());
        
        return ApiResponse.success(response);
    }
}