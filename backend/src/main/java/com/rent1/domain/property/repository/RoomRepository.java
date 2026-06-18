package com.rent1.domain.property.repository;

import com.rent1.domain.property.entity.Room;
import com.rent1.common.enums.RoomStatus;

import java.math.BigDecimal;
import java.util.List;

/**
 * 房间仓储接口
 * 
 * 职责：房间数据持久化操作
 * 
 * 注意：
 * - 仓储接口定义在领域层，实现在基础设施层
 * - 所有查询方法必须按房东ID过滤（数据权限隔离）
 */
public interface RoomRepository {
    
    /**
     * 保存房间
     * 
     * @param room 房间实体
     */
    void save(Room room);
    
    /**
     * 批量保存房间
     * 
     * @param rooms 房间列表
     */
    void saveAll(List<Room> rooms);
    
    /**
     * 更新房间
     * 
     * @param room 房间实体
     */
    void update(Room room);
    
    /**
     * 根据ID查询房间
     * 
     * @param roomId 房间ID
     * @return 房间实体
     */
    Room findById(String roomId);
    
    /**
     * 根据房源ID查询房间列表
     * 
     * @param houseSourceId 房源ID
     * @return 房间列表
     */
    List<Room> findByHouseSourceId(String houseSourceId);
    
    /**
     * 根据房东ID查询房间列表（带筛选条件）
     * 
     * @param landlordMemberId 房东会员ID
     * @param status 状态筛选（可选）
     * @param houseSourceId 房源筛选（可选）
     * @param minRent 最小租金（可选）
     * @param maxRent 最大租金（可选）
     * @return 房间列表
     */
    List<Room> findByLandlordWithFilters(String landlordMemberId,
                                          RoomStatus status,
                                          String houseSourceId,
                                          BigDecimal minRent,
                                          BigDecimal maxRent);
    
    /**
     * 根据房东ID查询房间列表
     * 
     * @param landlordMemberId 房东会员ID
     * @return 房间列表
     */
    List<Room> findByLandlordId(String landlordMemberId);
    
    /**
     * 统计房源下的房间数量
     * 
     * @param houseSourceId 房源ID
     * @return 房间数量
     */
    int countByHouseSourceId(String houseSourceId);
    
    /**
     * 统计房源下指定状态的房间数量
     * 
     * @param houseSourceId 房源ID
     * @param status 房间状态
     * @return 房间数量
     */
    int countByHouseSourceIdAndStatus(String houseSourceId, RoomStatus status);
    
    /**
     * 统计房东房间数量
     * 
     * @param landlordMemberId 房东会员ID
     * @return 房间数量
     */
    int countByLandlordId(String landlordMemberId);
    
    /**
     * 统计房东指定状态的房间数量
     * 
     * @param landlordMemberId 房东会员ID
     * @param status 房间状态
     * @return 房间数量
     */
    int countByLandlordIdAndStatus(String landlordMemberId, RoomStatus status);

    /**
     * 根据ID查询房间（非Optional版本）
     */
    Room findByIdDirect(String roomId);
}