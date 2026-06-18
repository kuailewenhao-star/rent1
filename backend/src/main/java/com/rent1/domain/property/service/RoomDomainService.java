package com.rent1.domain.property.service;

import com.rent1.common.enums.ErrorCode;
import com.rent1.common.enums.HouseSourceType;
import com.rent1.common.enums.RoomStatus;
import com.rent1.domain.common.BusinessException;
import com.rent1.domain.property.entity.HouseSource;
import com.rent1.domain.property.entity.Room;
import com.rent1.domain.property.repository.HouseSourceRepository;
import com.rent1.domain.property.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 房间领域服务
 * 
 * 职责边界：房间基础信息管理
 * 
 * 核心业务规则：
 * 1. 合租房源新增房间，整租房源禁止新增
 * 2. 合租房源最少保留2间，不可删至不足2间
 * 3. 仅空置状态允许编辑房间信息
 * 4. 单房间最多9张图片
 * 5. 金额边界：月租0~999999.99，押金0~999999.99
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoomDomainService {
    
    private final HouseSourceRepository houseSourceRepository;
    private final RoomRepository roomRepository;
    private final RoomStatusDomainService roomStatusDomainService;
    
    /**
     * 合租房源新增房间
     * 
     * 核心业务规则：
     * - 校验房源类型=SHARED（合租）
     * - 整租房源禁止新增房间
     * - 创建房间记录，状态=VACANT
     * 
     * @param houseSourceId 房源ID
     * @param memberId 当前用户ID
     * @param roomName 房间名称（≤20字符）
     * @param area 面积
     * @param monthlyRent 月租金额
     * @param deposit 押金金额
     * @return 新增的房间实体
     */
    @Transactional(rollbackFor = Exception.class)
    public Room createRoom(String houseSourceId, String memberId,
                          String roomName, BigDecimal area,
                          BigDecimal monthlyRent, BigDecimal deposit) {
        // 1. 查询房源
        HouseSource houseSource = houseSourceRepository.findById(houseSourceId);
        if (houseSource == null) {
            throw new BusinessException(ErrorCode.P001);
        }
        
        // 2. 校验归属权限
        if (!houseSource.belongsTo(memberId)) {
            throw new BusinessException(ErrorCode.P001);
        }
        
        // 3. 核心规则：整租房源禁止新增房间
        if (houseSource.isEntire()) {
            throw new BusinessException(ErrorCode.B001);
        }
        
        // 4. 校验房间名称长度
        if (roomName != null && roomName.length() > 20) {
            throw new BusinessException(ErrorCode.B002);
        }
        
        // 5. 校验金额边界
        validateAmount(monthlyRent);
        validateAmount(deposit);
        
        // 6. 创建房间
        Room room = Room.builder()
                .houseSourceId(houseSourceId)
                .roomName(roomName)
                .area(area)
                .monthlyRent(monthlyRent)
                .deposit(deposit)
                .status(RoomStatus.VACANT)
                .build();
        
        roomRepository.save(room);
        
        log.info("房间创建成功，房间ID：{}，房源ID：{}", room.getRoomId(), houseSourceId);
        
        return room;
    }
    
    /**
     * 编辑房间信息（仅空置状态允许）
     * 
     * 核心业务规则：
     * - 校验房间状态=VACANT（无有效合约）
     * - 更新：面积、月租金额、押金
     * - 已出租房间拦截
     * - 金额边界：月租0~999999.99，押金0~999999.99
     * 
     * @param roomId 房间ID
     * @param memberId 当前用户ID
     * @param roomName 房间名称
     * @param area 面积
     * @param monthlyRent 月租金额
     * @param deposit 押金金额
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateRoom(String roomId, String memberId,
                          String roomName, BigDecimal area,
                          BigDecimal monthlyRent, BigDecimal deposit) {
        // 1. 查询房间
        Room room = roomRepository.findById(roomId);
        if (room == null) {
            throw new BusinessException(ErrorCode.B010);
        }
        
        // 2. 查询房源并校验归属权限
        HouseSource houseSource = houseSourceRepository.findById(room.getHouseSourceId());
        if (houseSource == null || !houseSource.belongsTo(memberId)) {
            throw new BusinessException(ErrorCode.P001);
        }
        
        // 3. 核心规则：仅空置状态允许编辑
        roomStatusDomainService.validateVacant(roomId);
        
        // 4. 校验房间名称长度
        if (roomName != null && roomName.length() > 20) {
            throw new BusinessException(ErrorCode.B002);
        }
        
        // 5. 校验金额边界
        validateAmount(monthlyRent);
        validateAmount(deposit);
        
        // 6. 更新房间信息
        room.updateInfo(roomName, area, monthlyRent, deposit);
        roomRepository.update(room);
        
        log.info("房间更新成功，房间ID：{}", roomId);
    }
    
    /**
     * 上传房间图片
     * 
     * 核心业务规则：
     * - 校验房间归属当前房东
     * - 单房间最多9张图片
     * - 图片格式：jpg/png，大小≤5MB（由应用层校验）
     * 
     * @param roomId 房间ID
     * @param memberId 当前用户ID
     * @param imageUrl 图片URL
     */
    @Transactional(rollbackFor = Exception.class)
    public void uploadRoomImage(String roomId, String memberId, String imageUrl) {
        // 1. 查询房间
        Room room = roomRepository.findById(roomId);
        if (room == null) {
            throw new BusinessException(ErrorCode.B010);
        }
        
        // 2. 查询房源并校验归属权限
        HouseSource houseSource = houseSourceRepository.findById(room.getHouseSourceId());
        if (houseSource == null || !houseSource.belongsTo(memberId)) {
            throw new BusinessException(ErrorCode.P001);
        }
        
        // 3. 添加图片（核心规则：最多9张）
        room.addImage(imageUrl);
        roomRepository.update(room);
        
        log.info("房间图片上传成功，房间ID：{}，图片URL：{}", roomId, imageUrl);
    }
    
    /**
     * 删除房间图片
     * 
     * @param roomId 房间ID
     * @param memberId 当前用户ID
     * @param imageUrl 图片URL
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteRoomImage(String roomId, String memberId, String imageUrl) {
        // 1. 查询房间
        Room room = roomRepository.findById(roomId);
        if (room == null) {
            throw new BusinessException(ErrorCode.B010);
        }
        
        // 2. 查询房源并校验归属权限
        HouseSource houseSource = houseSourceRepository.findById(room.getHouseSourceId());
        if (houseSource == null || !houseSource.belongsTo(memberId)) {
            throw new BusinessException(ErrorCode.P001);
        }
        
        // 3. 删除图片
        room.removeImage(imageUrl);
        roomRepository.update(room);
        
        log.info("房间图片删除成功，房间ID：{}，图片URL：{}", roomId, imageUrl);
    }
    
    /**
     * 查询房间列表（按房源分组聚合展示）
     * 
     * 核心业务规则：
     * - 按房东ID过滤
     * - 支持状态/房源/租金范围筛选
     * - 即将到期房间（合约剩余≤30天）高亮标识
     * - 租客角色仅看承租房间
     * 
     * @param memberId 房东会员ID
     * @param status 状态筛选（可选）
     * @param houseSourceId 房源筛选（可选）
     * @param minRent 最小租金（可选）
     * @param maxRent 最大租金（可选）
     * @return 房间列表
     */
    public List<Room> queryRooms(String memberId, RoomStatus status,
                                 String houseSourceId, BigDecimal minRent,
                                 BigDecimal maxRent) {
        return roomRepository.findByLandlordWithFilters(memberId, status, 
                houseSourceId, minRent, maxRent);
    }
    
    /**
     * 查询房源下的所有房间
     * 
     * @param houseSourceId 房源ID
     * @return 房间列表
     */
    public List<Room> queryRoomsByHouseSource(String houseSourceId) {
        return roomRepository.findByHouseSourceId(houseSourceId);
    }
    
    /**
     * 查询房间详情
     * 
     * @param roomId 房间ID
     * @param memberId 当前用户ID
     * @return 房间实体
     */
    public Room getRoomDetail(String roomId, String memberId) {
        Room room = roomRepository.findById(roomId);
        if (room == null) {
            throw new BusinessException(ErrorCode.B010);
        }
        
        // 查询房源并校验归属权限
        HouseSource houseSource = houseSourceRepository.findById(room.getHouseSourceId());
        if (houseSource == null || !houseSource.belongsTo(memberId)) {
            throw new BusinessException(ErrorCode.P001);
        }
        
        return room;
    }
    
    /**
     * 校验金额边界
     * 核心业务规则：金额范围0~999999.99
     */
    private void validateAmount(BigDecimal amount) {
        if (amount != null) {
            if (amount.compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException(ErrorCode.V020);
            }
            if (amount.compareTo(new BigDecimal("999999.99")) > 0) {
                throw new BusinessException(ErrorCode.V020);
            }
        }
    }
}