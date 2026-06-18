package com.rent1.domain.property.repository;

import com.rent1.domain.property.entity.HouseSource;
import com.rent1.common.enums.HouseSourceStatus;
import com.rent1.common.enums.HouseSourceType;

import java.util.List;

/**
 * 房源仓储接口
 * 
 * 职责：房源数据持久化操作
 * 
 * 注意：
 * - 仓储接口定义在领域层，实现在基础设施层
 * - 所有查询方法必须按房东ID过滤（数据权限隔离）
 */
public interface HouseSourceRepository {
    
    /**
     * 保存房源
     * 
     * @param houseSource 房源实体
     */
    void save(HouseSource houseSource);
    
    /**
     * 更新房源
     * 
     * @param houseSource 房源实体
     */
    void update(HouseSource houseSource);
    
    /**
     * 根据ID查询房源
     * 
     * @param houseSourceId 房源ID
     * @return 房源实体
     */
    HouseSource findById(String houseSourceId);
    
    /**
     * 根据房东ID查询房源列表（带筛选条件）
     * 
     * @param landlordMemberId 房东会员ID
     * @param status 状态筛选（可选）
     * @param type 类型筛选（可选）
     * @param city 城市筛选（可选）
     * @return 房源列表
     */
    List<HouseSource> findByLandlordWithFilters(String landlordMemberId,
                                                  HouseSourceStatus status,
                                                  HouseSourceType type,
                                                  String city);
    
    /**
     * 根据房东ID查询房源列表
     * 
     * @param landlordMemberId 房东会员ID
     * @return 房源列表
     */
    List<HouseSource> findByLandlordId(String landlordMemberId);
    
    /**
     * 统计房东房源数量
     * 
     * @param landlordMemberId 房东会员ID
     * @return 房源数量
     */
    int countByLandlordId(String landlordMemberId);
    
    /**
     * 统计房东指定状态的房源数量
     * 
     * @param landlordMemberId 房东会员ID
     * @param status 房源状态
     * @return 房源数量
     */
    int countByLandlordIdAndStatus(String landlordMemberId, HouseSourceStatus status);

    /**
     * 根据ID查询房源（非Optional版本）
     */
    HouseSource findByIdDirect(String houseSourceId);
}