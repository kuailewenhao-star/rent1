package com.rent1.application.service;

import com.rent1.application.dto.request.CreateRoomRequest;
import com.rent1.application.dto.request.UpdateRoomRequest;
import com.rent1.application.dto.response.RoomDetailResponse;
import com.rent1.common.enums.RoomStatus;
import com.rent1.domain.property.entity.HouseSource;
import com.rent1.domain.property.entity.Room;
import com.rent1.domain.property.repository.HouseSourceRepository;
import com.rent1.domain.property.service.RoomDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 房间应用服务
 * 
 * 职责：房间业务流程编排、跨领域调度、事务控制、入参组装
 * 
 * 注意：
 * - 应用层仅做业务流程编排，不编写核心业务校验规则
 * - 核心业务规则全部下沉领域层
 * - 入参组装、DTO转换在应用层完成
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoomAppService {
    
    private final RoomDomainService roomDomainService;
    private final HouseSourceRepository houseSourceRepository;
    
    /**
     * 新增房间（合租房源）
     * 
     * 核心业务流程：
     * 1. 调用领域服务创建房间
     * 2. 组装响应DTO
     * 
     * @param memberId 当前用户ID
     * @param houseSourceId 房源ID
     * @param request 创建请求
     * @return 房间详情响应
     */
    @Transactional(rollbackFor = Exception.class)
    public RoomDetailResponse createRoom(String memberId, String houseSourceId, CreateRoomRequest request) {
        log.info("新增房间请求，房东ID：{}，房源ID：{}", memberId, houseSourceId);
        
        Room room = roomDomainService.createRoom(
                houseSourceId,
                memberId,
                request.getRoomName(),
                request.getArea(),
                request.getMonthlyRent(),
                request.getDeposit()
        );
        
        return convertToRoomDetailResponse(room);
    }
    
    /**
     * 编辑房间信息
     * 
     * 核心业务流程：
     * 1. 调用领域服务更新房间信息
     * 
     * @param memberId 当前用户ID
     * @param roomId 房间ID
     * @param request 更新请求
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateRoom(String memberId, String roomId, UpdateRoomRequest request) {
        log.info("编辑房间请求，房东ID：{}，房间ID：{}", memberId, roomId);
        
        roomDomainService.updateRoom(
                roomId,
                memberId,
                request.getRoomName(),
                request.getArea(),
                request.getMonthlyRent(),
                request.getDeposit()
        );
    }
    
    /**
     * 上传房间图片
     * 
     * 核心业务流程：
     * 1. 校验图片格式和大小（应用层校验）
     * 2. 调用领域服务添加图片
     * 
     * @param memberId 当前用户ID
     * @param roomId 房间ID
     * @param imageUrl 图片URL
     */
    @Transactional(rollbackFor = Exception.class)
    public void uploadRoomImage(String memberId, String roomId, String imageUrl) {
        log.info("上传房间图片请求，房东ID：{}，房间ID：{}", memberId, roomId);
        
        roomDomainService.uploadRoomImage(roomId, memberId, imageUrl);
    }
    
    /**
     * 删除房间图片
     * 
     * @param memberId 当前用户ID
     * @param roomId 房间ID
     * @param imageUrl 图片URL
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteRoomImage(String memberId, String roomId, String imageUrl) {
        log.info("删除房间图片请求，房东ID：{}，房间ID：{}", memberId, roomId);
        
        roomDomainService.deleteRoomImage(roomId, memberId, imageUrl);
    }
    
    /**
     * 查询房间详情
     * 
     * @param memberId 当前用户ID
     * @param roomId 房间ID
     * @return 房间详情响应
     */
    public RoomDetailResponse getRoomDetail(String memberId, String roomId) {
        log.info("查询房间详情，房东ID：{}，房间ID：{}", memberId, roomId);
        
        Room room = roomDomainService.getRoomDetail(roomId, memberId);
        return convertToRoomDetailResponse(room);
    }
    
    /**
     * 查询房间列表
     * 
     * 核心业务流程：
     * 1. 调用领域服务查询房间列表
     * 2. 组装响应DTO
     * 
     * @param memberId 当前用户ID
     * @param status 状态筛选（可选）
     * @param houseSourceId 房源筛选（可选）
     * @param minRent 最小租金（可选）
     * @param maxRent 最大租金（可选）
     * @return 房间列表
     */
    public List<RoomDetailResponse> queryRooms(String memberId,
                                                String status,
                                                String houseSourceId,
                                                BigDecimal minRent,
                                                BigDecimal maxRent) {
        log.info("查询房间列表，房东ID：{}", memberId);
        
        RoomStatus statusEnum = status != null ? RoomStatus.fromCode(status) : null;
        
        List<Room> rooms = roomDomainService.queryRooms(memberId, statusEnum, houseSourceId, minRent, maxRent);
        
        return rooms.stream()
                .map(this::convertToRoomDetailResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 转换房间实体到响应DTO
     */
    private RoomDetailResponse convertToRoomDetailResponse(Room room) {
        // 获取房源名称
        HouseSource houseSource = houseSourceRepository.findById(room.getHouseSourceId());
        String houseSourceName = houseSource != null ? houseSource.getName() : "";
        
        return RoomDetailResponse.builder()
                .roomId(room.getRoomId())
                .houseSourceId(room.getHouseSourceId())
                .houseSourceName(houseSourceName)
                .roomName(room.getRoomName())
                .area(room.getArea())
                .monthlyRent(room.getMonthlyRent())
                .deposit(room.getDeposit())
                .images(room.getImages())
                .status(room.getStatus().getCode())
                .statusName(room.getStatus().getName())
                .expiringSoon(false) // 需要合约服务计算
                .createTime(room.getCreateTime())
                .updateTime(room.getUpdateTime())
                .build();
    }
}