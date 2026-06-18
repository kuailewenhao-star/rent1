package com.rent1.domain.property.service;

import com.rent1.common.enums.ErrorCode;
import com.rent1.common.enums.HouseSourceStatus;
import com.rent1.common.enums.HouseSourceType;
import com.rent1.domain.common.BusinessException;
import com.rent1.domain.property.entity.HouseSource;
import com.rent1.domain.property.entity.Room;
import com.rent1.domain.property.event.HouseSourceCreatedEvent;
import com.rent1.domain.property.repository.HouseSourceRepository;
import com.rent1.domain.property.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 房源领域服务
 * 
 * 职责边界：房源（整套）与房间（最小出租单元）的双层架构管理
 * 
 * 核心业务规则：
 * 1. 房源层级不存储图片、租金，所有出租属性全部下沉至房间层级
 * 2. 停用/终止房源仅留存历史数据，不再参与平台出租率统计
 * 3. V1.0不支持房源物理删除，仅做状态管控
 * 4. 合租房源最少保留2间房间
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PropertyDomainService {
    
    private final HouseSourceRepository houseSourceRepository;
    private final RoomRepository roomRepository;
    
    /**
     * 创建房源并自动生成默认房间
     * 
     * 核心业务规则：
     * - 整租房源：自动生成1间房间，room_name="整套"
     * - 合租房源：自动生成2间房间，room_name="房间1"/"房间2"
     * - 房源名称无唯一性约束
     * - 承租结束时间不能早于开始时间
     * 
     * @param houseSource 房源实体
     * @return 创建的房间列表
     */
    @Transactional(rollbackFor = Exception.class)
    public List<Room> createHouseSource(HouseSource houseSource) {
        // 1. 校验承租时间
        validateLeasePeriod(houseSource.getLeaseStart(), houseSource.getLeaseEnd());
        
        // 2. 校验地址完整性（省/市/区三级）
        validateAddress(houseSource);
        
        // 3. 保存房源
        houseSource.setStatus(HouseSourceStatus.NORMAL);
        houseSourceRepository.save(houseSource);
        
        // 4. 创建默认房间
        List<Room> rooms = houseSource.createDefaultRooms();
        roomRepository.saveAll(rooms);
        
        log.info("房源创建成功，房源ID：{}，类型：{}，生成房间数：{}", 
                houseSource.getHouseSourceId(), houseSource.getType(), rooms.size());
        
        return rooms;
    }
    
    /**
     * 更新房源基础信息
     * 
     * 核心业务规则：
     * - 仅允许编辑：名称、地址、总户型、承租时间
     * - 房源类型（整租/合租）创建后不可修改
     * - 停用房源不可编辑
     * - 校验房源归属当前房东
     * 
     * @param houseSourceId 房源ID
     * @param memberId 当前用户ID
     * @param name 房源名称
     * @param province 省份
     * @param city 城市
     * @param district 区县
     * @param address 详细地址
     * @param totalRooms 总户型
     * @param leaseStart 承租开始时间
     * @param leaseEnd 承租结束时间
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateHouseSource(String houseSourceId, String memberId,
                                  String name, String province, String city,
                                  String district, String address, Integer totalRooms,
                                  LocalDate leaseStart, LocalDate leaseEnd) {
        // 1. 查询房源
        HouseSource houseSource = houseSourceRepository.findById(houseSourceId);
        if (houseSource == null) {
            throw new BusinessException(ErrorCode.P001);
        }
        
        // 2. 校验归属权限
        if (!houseSource.belongsTo(memberId)) {
            throw new BusinessException(ErrorCode.P001);
        }
        
        // 3. 校验是否可编辑（停用房源不可编辑）
        if (!houseSource.canEdit()) {
            throw new BusinessException(ErrorCode.P002);
        }
        
        // 4. 校验承租时间
        validateLeasePeriod(leaseStart, leaseEnd);
        
        // 5. 校验地址完整性
        if (province == null || city == null || district == null) {
            throw new BusinessException(ErrorCode.V010);
        }
        
        // 6. 更新基础信息（房源类型不可修改）
        houseSource.updateBasicInfo(name, province, city, district, address, 
                                   totalRooms, leaseStart, leaseEnd);
        houseSourceRepository.update(houseSource);
        
        log.info("房源更新成功，房源ID：{}", houseSourceId);
    }
    
    /**
     * 停用房源
     * 
     * 核心业务规则：
     * - 将status改为LEASE_EXPIRED或TERMINATED
     * - 下属房间不可新增出租、新建账单
     * - V1.0不支持物理删除，仅状态管控
     * - 校验房源归属当前房东
     * 
     * @param houseSourceId 房源ID
     * @param memberId 当前用户ID
     * @param newStatus 新状态
     * @param reason 原因（可选）
     */
    @Transactional(rollbackFor = Exception.class)
    public void deactivateHouseSource(String houseSourceId, String memberId,
                                       HouseSourceStatus newStatus, String reason) {
        // 1. 查询房源
        HouseSource houseSource = houseSourceRepository.findById(houseSourceId);
        if (houseSource == null) {
            throw new BusinessException(ErrorCode.P001);
        }
        
        // 2. 校验归属权限
        if (!houseSource.belongsTo(memberId)) {
            throw new BusinessException(ErrorCode.P001);
        }
        
        // 3. 校验状态是否可停用
        if (houseSource.getStatus().isDeactivated()) {
            throw new BusinessException(ErrorCode.P010);
        }
        
        // 4. 变更状态
        houseSource.changeStatus(newStatus, reason);
        houseSourceRepository.update(houseSource);
        
        log.info("房源停用成功，房源ID：{}，新状态：{}", houseSourceId, newStatus);
    }
    
    /**
     * 查询房东房源列表
     * 
     * 核心业务规则：
     * - 按landlord_member_id过滤（仅看自有）
     * - 支持status/type/city筛选
     * - 聚合展示总户数、空置数、已租数
     * 
     * @param memberId 房东会员ID
     * @param status 状态筛选（可选）
     * @param type 类型筛选（可选）
     * @param city 城市筛选（可选）
     * @return 房源列表
     */
    public List<HouseSource> queryHouseSources(String memberId, 
                                               HouseSourceStatus status,
                                               HouseSourceType type,
                                               String city) {
        return houseSourceRepository.findByLandlordWithFilters(memberId, status, type, city);
    }
    
    /**
     * 查询房源详情
     * 
     * @param houseSourceId 房源ID
     * @param memberId 当前用户ID
     * @return 房源实体
     */
    public HouseSource getHouseSourceDetail(String houseSourceId, String memberId) {
        HouseSource houseSource = houseSourceRepository.findById(houseSourceId);
        if (houseSource == null) {
            throw new BusinessException(ErrorCode.P001);
        }
        
        // 校验归属权限
        if (!houseSource.belongsTo(memberId)) {
            throw new BusinessException(ErrorCode.P001);
        }
        
        return houseSource;
    }
    
    /**
     * 校验承租时间
     * 核心业务规则：结束时间不能早于开始时间
     */
    private void validateLeasePeriod(LocalDate leaseStart, LocalDate leaseEnd) {
        if (leaseStart != null && leaseEnd != null && leaseEnd.isBefore(leaseStart)) {
            throw new BusinessException(ErrorCode.V011);
        }
    }
    
    /**
     * 校验地址完整性
     * 核心业务规则：省/市/区三级结构化选择
     */
    private void validateAddress(HouseSource houseSource) {
        if (houseSource.getProvince() == null || 
            houseSource.getCity() == null || 
            houseSource.getDistrict() == null) {
            throw new BusinessException(ErrorCode.V010);
        }
    }
}