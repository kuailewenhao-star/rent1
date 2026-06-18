package com.rent1.application.service;

import com.rent1.application.dto.request.CreateHouseSourceRequest;
import com.rent1.application.dto.request.DeactivateHouseSourceRequest;
import com.rent1.application.dto.request.UpdateHouseSourceRequest;
import com.rent1.application.dto.response.CreateHouseSourceResponse;
import com.rent1.application.dto.response.HouseSourceDetailResponse;
import com.rent1.application.dto.response.RoomDetailResponse;
import com.rent1.common.enums.HouseSourceStatus;
import com.rent1.common.enums.HouseSourceType;
import com.rent1.domain.property.entity.HouseSource;
import com.rent1.domain.property.entity.Room;
import com.rent1.domain.property.service.PropertyDomainService;
import com.rent1.domain.property.service.RoomDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 房源应用服务
 * 
 * 职责：房源业务流程编排、跨领域调度、事务控制、入参组装
 * 
 * 注意：
 * - 应用层仅做业务流程编排，不编写核心业务校验规则
 * - 核心业务规则全部下沉领域层
 * - 入参组装、DTO转换在应用层完成
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HouseSourceAppService {
    
    private final PropertyDomainService propertyDomainService;
    private final RoomDomainService roomDomainService;
    
    /**
     * 创建房源
     * 
     * 核心业务流程：
     * 1. 组装房源实体
     * 2. 调用领域服务创建房源并生成默认房间
     * 3. 组装响应DTO
     * 
     * @param memberId 当前用户ID（房东）
     * @param request 创建请求
     * @return 创建响应
     */
    @Transactional(rollbackFor = Exception.class)
    public CreateHouseSourceResponse createHouseSource(String memberId, CreateHouseSourceRequest request) {
        log.info("创建房源请求，房东ID：{}，房源名称：{}", memberId, request.getName());
        
        // 1. 组装房源实体
        HouseSource houseSource = HouseSource.builder()
                .landlordMemberId(memberId)
                .name(request.getName())
                .province(request.getProvince())
                .city(request.getCity())
                .district(request.getDistrict())
                .address(request.getAddress())
                .totalRooms(request.getTotalRooms())
                .type(HouseSourceType.fromCode(request.getType()))
                .leaseStart(request.getLeaseStart())
                .leaseEnd(request.getLeaseEnd())
                .build();
        
        // 2. 调用领域服务创建房源并生成默认房间
        List<Room> rooms = propertyDomainService.createHouseSource(houseSource);
        
        // 3. 组装响应DTO
        List<CreateHouseSourceResponse.RoomInfo> roomInfos = rooms.stream()
                .map(room -> CreateHouseSourceResponse.RoomInfo.builder()
                        .roomId(room.getRoomId())
                        .roomName(room.getRoomName())
                        .build())
                .collect(Collectors.toList());
        
        return CreateHouseSourceResponse.builder()
                .houseSourceId(houseSource.getHouseSourceId())
                .rooms(roomInfos)
                .build();
    }
    
    /**
     * 更新房源
     * 
     * 核心业务流程：
     * 1. 调用领域服务更新房源基础信息
     * 
     * @param memberId 当前用户ID
     * @param houseSourceId 房源ID
     * @param request 更新请求
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateHouseSource(String memberId, String houseSourceId, UpdateHouseSourceRequest request) {
        log.info("更新房源请求，房东ID：{}，房源ID：{}", memberId, houseSourceId);
        
        propertyDomainService.updateHouseSource(
                houseSourceId,
                memberId,
                request.getName(),
                request.getProvince(),
                request.getCity(),
                request.getDistrict(),
                request.getAddress(),
                request.getTotalRooms(),
                request.getLeaseStart(),
                request.getLeaseEnd()
        );
    }
    
    /**
     * 停用房源
     * 
     * 核心业务流程：
     * 1. 调用领域服务变更房源状态
     * 
     * @param memberId 当前用户ID
     * @param houseSourceId 房源ID
     * @param request 停用请求
     */
    @Transactional(rollbackFor = Exception.class)
    public void deactivateHouseSource(String memberId, String houseSourceId, DeactivateHouseSourceRequest request) {
        log.info("停用房源请求，房东ID：{}，房源ID：{}，新状态：{}", memberId, houseSourceId, request.getStatus());
        
        HouseSourceStatus newStatus = HouseSourceStatus.fromCode(request.getStatus());
        propertyDomainService.deactivateHouseSource(houseSourceId, memberId, newStatus, request.getReason());
    }
    
    /**
     * 查询房源详情
     * 
     * 核心业务流程：
     * 1. 调用领域服务查询房源
     * 2. 查询房源下的房间列表
     * 3. 组装响应DTO（含房间统计）
     * 
     * @param memberId 当前用户ID
     * @param houseSourceId 房源ID
     * @return 房源详情响应
     */
    public HouseSourceDetailResponse getHouseSourceDetail(String memberId, String houseSourceId) {
        log.info("查询房源详情，房东ID：{}，房源ID：{}", memberId, houseSourceId);
        
        // 1. 查询房源
        HouseSource houseSource = propertyDomainService.getHouseSourceDetail(houseSourceId, memberId);
        
        // 2. 查询房源下的房间列表
        List<Room> rooms = roomDomainService.queryRoomsByHouseSource(houseSourceId);
        
        // 3. 组装房间响应
        List<RoomDetailResponse> roomResponses = rooms.stream()
                .map(this::convertToRoomDetailResponse)
                .collect(Collectors.toList());
        
        // 4. 计算房间统计
        int vacantCount = (int) rooms.stream().filter(Room::isVacant).count();
        int occupiedCount = (int) rooms.stream().filter(Room::isOccupied).count();
        
        HouseSourceDetailResponse.RoomStatistics statistics = HouseSourceDetailResponse.RoomStatistics.builder()
                .total(rooms.size())
                .vacant(vacantCount)
                .occupied(occupiedCount)
                .build();
        
        // 5. 组装响应DTO
        return HouseSourceDetailResponse.builder()
                .houseSourceId(houseSource.getHouseSourceId())
                .name(houseSource.getName())
                .province(houseSource.getProvince())
                .city(houseSource.getCity())
                .district(houseSource.getDistrict())
                .address(houseSource.getAddress())
                .totalRooms(houseSource.getTotalRooms())
                .type(houseSource.getType().getCode())
                .typeName(houseSource.getType().getName())
                .status(houseSource.getStatus().getCode())
                .statusName(houseSource.getStatus().getName())
                .leaseStart(houseSource.getLeaseStart())
                .leaseEnd(houseSource.getLeaseEnd())
                .createTime(houseSource.getCreateTime())
                .updateTime(houseSource.getUpdateTime())
                .rooms(roomResponses)
                .roomStatistics(statistics)
                .build();
    }
    
    /**
     * 查询房源列表
     * 
     * 核心业务流程：
     * 1. 调用领域服务查询房源列表
     * 2. 组装响应DTO
     * 
     * @param memberId 当前用户ID
     * @param status 状态筛选（可选）
     * @param type 类型筛选（可选）
     * @param city 城市筛选（可选）
     * @return 房源列表
     */
    public List<HouseSourceDetailResponse> queryHouseSources(String memberId,
                                                              String status,
                                                              String type,
                                                              String city) {
        log.info("查询房源列表，房东ID：{}", memberId);
        
        HouseSourceStatus statusEnum = status != null ? HouseSourceStatus.fromCode(status) : null;
        HouseSourceType typeEnum = type != null ? HouseSourceType.fromCode(type) : null;
        
        List<HouseSource> houseSources = propertyDomainService.queryHouseSources(memberId, statusEnum, typeEnum, city);
        
        return houseSources.stream()
                .map(hs -> HouseSourceDetailResponse.builder()
                        .houseSourceId(hs.getHouseSourceId())
                        .name(hs.getName())
                        .province(hs.getProvince())
                        .city(hs.getCity())
                        .district(hs.getDistrict())
                        .address(hs.getAddress())
                        .totalRooms(hs.getTotalRooms())
                        .type(hs.getType().getCode())
                        .typeName(hs.getType().getName())
                        .status(hs.getStatus().getCode())
                        .statusName(hs.getStatus().getName())
                        .leaseStart(hs.getLeaseStart())
                        .leaseEnd(hs.getLeaseEnd())
                        .createTime(hs.getCreateTime())
                        .updateTime(hs.getUpdateTime())
                        .build())
                .collect(Collectors.toList());
    }
    
    /**
     * 转换房间实体到响应DTO
     */
    private RoomDetailResponse convertToRoomDetailResponse(Room room) {
        return RoomDetailResponse.builder()
                .roomId(room.getRoomId())
                .houseSourceId(room.getHouseSourceId())
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