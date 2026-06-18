package com.rent1.apis.controller;

import com.rent1.application.dto.request.CreateRoomRequest;
import com.rent1.application.dto.request.UpdateRoomRequest;
import com.rent1.application.dto.response.RoomDetailResponse;
import com.rent1.application.service.RoomAppService;
import com.rent1.common.response.ApiResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

/**
 * 房间接口控制器
 * 
 * 职责：房间相关接口的请求接收、参数校验、响应封装
 * 
 * 接口列表：
 * - POST /api/house-sources/{houseSourceId}/rooms：新增房间
 * - PUT /api/rooms/{roomId}：编辑房间
 * - POST /api/rooms/{roomId}/images：上传房间图片
 * - DELETE /api/rooms/{roomId}/images：删除房间图片
 * - GET /api/rooms/{roomId}：房间详情
 * - GET /api/rooms：房间列表
 * 
 * 注意：
 * - 接入层仅做参数接收、基础格式校验、权限拦截、响应封装
 * - 不承载任何业务逻辑、业务判断
 * - 核心业务规则全部下沉领域层
 */
@Api(tags = "房间管理")
@Slf4j
@RestController
@RequestMapping("/")
@RequiredArgsConstructor
@Validated
public class RoomController {
    
    private final RoomAppService roomAppService;
    
    /**
     * 新增房间（合租房源）
     * 
     * 接口路径：POST /api/house-sources/{houseSourceId}/rooms
     * 鉴权要求：房东会员
     * 
     * 核心业务规则：
     * - 校验房源类型=SHARED（合租）
     * - 整租房源禁止新增房间
     * - 房间名≤20字符
     * - 金额边界：月租0~999999.99，押金0~999999.99
     * 
     * @param houseSourceId 房源ID
     * @param request 新增房间请求
     * @return 房间详情响应
     */
    @ApiOperation(value = "新增房间", notes = "房东会员新增房间，仅合租房源可新增，整租房源禁止新增")
    @PostMapping("/house-sources/{houseSourceId}/rooms")
    public ApiResponse<RoomDetailResponse> createRoom(
            @ApiParam(value = "房源ID", required = true) @PathVariable String houseSourceId,
            @Valid @RequestBody CreateRoomRequest request) {
        
        // TODO: 从请求头获取当前用户ID（房东）
        String memberId = "mock_member_id";
        
        RoomDetailResponse response = roomAppService.createRoom(memberId, houseSourceId, request);
        
        log.info("新增房间成功，房源ID：{}，房间ID：{}", houseSourceId, response.getRoomId());
        
        return ApiResponse.success(response);
    }
    
    /**
     * 编辑房间信息
     * 
     * 接口路径：PUT /api/rooms/{roomId}
     * 鉴权要求：房东会员（仅可编辑自有房间）
     * 
     * 核心业务规则：
     * - 仅空置状态允许编辑
     * - 房间名≤20字符
     * - 金额边界：月租0~999999.99，押金0~999999.99
     * 
     * @param roomId 房间ID
     * @param request 编辑房间请求
     * @return 成功响应
     */
    @ApiOperation(value = "编辑房间", notes = "房东会员编辑房间信息，仅空置状态允许编辑")
    @PutMapping("/rooms/{roomId}")
    public ApiResponse<Void> updateRoom(
            @ApiParam(value = "房间ID", required = true) @PathVariable String roomId,
            @Valid @RequestBody UpdateRoomRequest request) {
        
        // TODO: 从请求头获取当前用户ID（房东）
        String memberId = "mock_member_id";
        
        roomAppService.updateRoom(memberId, roomId, request);
        
        log.info("编辑房间成功，房间ID：{}", roomId);
        
        return ApiResponse.success();
    }
    
    /**
     * 上传房间图片
     * 
     * 接口路径：POST /api/rooms/{roomId}/images
     * 鉴权要求：房东会员
     * 
     * 核心业务规则：
     * - 单房间最多9张图片
     * - 图片格式：jpg/png，大小≤5MB
     * 
     * @param roomId 房间ID
     * @param imageUrl 图片URL
     * @return 成功响应
     */
    @ApiOperation(value = "上传房间图片", notes = "房东会员上传房间图片，单房间最多9张")
    @PostMapping("/rooms/{roomId}/images")
    public ApiResponse<Void> uploadRoomImage(
            @ApiParam(value = "房间ID", required = true) @PathVariable String roomId,
            @ApiParam(value = "图片URL", required = true) @RequestParam String imageUrl) {
        
        // TODO: 从请求头获取当前用户ID（房东）
        String memberId = "mock_member_id";
        
        // 校验图片格式和大小（应用层校验）
        validateImage(imageUrl);
        
        roomAppService.uploadRoomImage(memberId, roomId, imageUrl);
        
        log.info("上传房间图片成功，房间ID：{}", roomId);
        
        return ApiResponse.success();
    }
    
    /**
     * 删除房间图片
     * 
     * 接口路径：DELETE /api/rooms/{roomId}/images
     * 鉴权要求：房东会员
     * 
     * @param roomId 房间ID
     * @param imageUrl 图片URL
     * @return 成功响应
     */
    @ApiOperation(value = "删除房间图片", notes = "房东会员删除房间图片")
    @DeleteMapping("/rooms/{roomId}/images")
    public ApiResponse<Void> deleteRoomImage(
            @ApiParam(value = "房间ID", required = true) @PathVariable String roomId,
            @ApiParam(value = "图片URL", required = true) @RequestParam String imageUrl) {
        
        // TODO: 从请求头获取当前用户ID（房东）
        String memberId = "mock_member_id";
        
        roomAppService.deleteRoomImage(memberId, roomId, imageUrl);
        
        log.info("删除房间图片成功，房间ID：{}", roomId);
        
        return ApiResponse.success();
    }
    
    /**
     * 房间详情
     * 
     * 接口路径：GET /api/rooms/{roomId}
     * 鉴权要求：房东会员（仅可查看自有房间）
     * 
     * @param roomId 房间ID
     * @return 房间详情响应
     */
    @ApiOperation(value = "房间详情", notes = "房东会员查看房间详情")
    @GetMapping("/rooms/{roomId}")
    public ApiResponse<RoomDetailResponse> getRoomDetail(
            @ApiParam(value = "房间ID", required = true) @PathVariable String roomId) {
        
        // TODO: 从请求头获取当前用户ID（房东）
        String memberId = "mock_member_id";
        
        RoomDetailResponse response = roomAppService.getRoomDetail(memberId, roomId);
        
        log.info("查询房间详情成功，房间ID：{}", roomId);
        
        return ApiResponse.success(response);
    }
    
    /**
     * 房间列表
     * 
     * 接口路径：GET /api/rooms
     * 鉴权要求：房东会员（仅可查看自有房间）
     * 
     * @param status 状态筛选（可选）
     * @param houseSourceId 房源筛选（可选）
     * @param minRent 最小租金（可选）
     * @param maxRent 最大租金（可选）
     * @return 房间列表响应
     */
    @ApiOperation(value = "房间列表", notes = "房东会员查看房间列表，支持状态、房源、租金范围筛选")
    @GetMapping("/rooms")
    public ApiResponse<List<RoomDetailResponse>> queryRooms(
            @ApiParam(value = "状态筛选") @RequestParam(required = false) String status,
            @ApiParam(value = "房源筛选") @RequestParam(required = false) String houseSourceId,
            @ApiParam(value = "最小租金") @RequestParam(required = false) BigDecimal minRent,
            @ApiParam(value = "最大租金") @RequestParam(required = false) BigDecimal maxRent) {
        
        // TODO: 从请求头获取当前用户ID（房东）
        String memberId = "mock_member_id";
        
        List<RoomDetailResponse> response = roomAppService.queryRooms(memberId, status, houseSourceId, minRent, maxRent);
        
        log.info("查询房间列表成功，房东ID：{}，数量：{}", memberId, response.size());
        
        return ApiResponse.success(response);
    }
    
    /**
     * 校验图片格式和大小
     * 核心业务规则：图片格式jpg/png，大小≤5MB
     */
    private void validateImage(String imageUrl) {
        // 图片格式校验（应用层校验）
        if (imageUrl != null && !imageUrl.isEmpty()) {
            String lowerUrl = imageUrl.toLowerCase();
            if (!lowerUrl.endsWith(".jpg") && !lowerUrl.endsWith(".jpeg") && !lowerUrl.endsWith(".png")) {
                // 抛出业务异常
                throw new com.rent1.domain.common.BusinessException(com.rent1.common.enums.ErrorCode.V021);
            }
        }
        
        // 图片大小校验（由前端或文件上传服务校验，此处仅做格式校验）
    }
}