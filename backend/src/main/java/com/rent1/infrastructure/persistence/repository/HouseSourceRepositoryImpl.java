package com.rent1.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rent1.common.enums.HouseSourceStatus;
import com.rent1.common.enums.HouseSourceType;
import com.rent1.domain.property.entity.HouseSource;
import com.rent1.domain.property.repository.HouseSourceRepository;
import com.rent1.infrastructure.persistence.mapper.HouseSourceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 房源仓储实现
 * 
 * 职责：房源数据持久化操作实现
 * 
 * 注意：
 * - 实现类放在基础设施层
 * - 使用MyBatis-Plus进行数据库操作
 * - 所有查询方法必须按房东ID过滤（数据权限隔离）
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class HouseSourceRepositoryImpl implements HouseSourceRepository {
    
    private final HouseSourceMapper houseSourceMapper;
    
    @Override
    public void save(HouseSource houseSource) {
        houseSourceMapper.insert(houseSource);
    }
    
    @Override
    public void update(HouseSource houseSource) {
        houseSourceMapper.updateById(houseSource);
    }
    
    @Override
    public HouseSource findById(String houseSourceId) {
        return houseSourceMapper.selectById(houseSourceId);
    }

    @Override
    public HouseSource findByIdDirect(String houseSourceId) {
        return houseSourceMapper.selectById(houseSourceId);
    }
    
    @Override
    public List<HouseSource> findByLandlordWithFilters(String landlordMemberId,
                                                        HouseSourceStatus status,
                                                        HouseSourceType type,
                                                        String city) {
        LambdaQueryWrapper<HouseSource> wrapper = new LambdaQueryWrapper<>();
        
        // 核心规则：按房东ID过滤（数据权限隔离）
        wrapper.eq(HouseSource::getLandlordMemberId, landlordMemberId);
        
        // 状态筛选
        if (status != null) {
            wrapper.eq(HouseSource::getStatus, status);
        }
        
        // 类型筛选
        if (type != null) {
            wrapper.eq(HouseSource::getType, type);
        }
        
        // 城市筛选
        if (city != null && !city.isEmpty()) {
            wrapper.eq(HouseSource::getCity, city);
        }
        
        // 排序：创建时间倒序
        wrapper.orderByDesc(HouseSource::getCreateTime);
        
        return houseSourceMapper.selectList(wrapper);
    }
    
    @Override
    public List<HouseSource> findByLandlordId(String landlordMemberId) {
        LambdaQueryWrapper<HouseSource> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HouseSource::getLandlordMemberId, landlordMemberId);
        wrapper.orderByDesc(HouseSource::getCreateTime);
        return houseSourceMapper.selectList(wrapper);
    }
    
    @Override
    public int countByLandlordId(String landlordMemberId) {
        LambdaQueryWrapper<HouseSource> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HouseSource::getLandlordMemberId, landlordMemberId);
        return Math.toIntExact(houseSourceMapper.selectCount(wrapper));
    }
    
    @Override
    public int countByLandlordIdAndStatus(String landlordMemberId, HouseSourceStatus status) {
        LambdaQueryWrapper<HouseSource> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HouseSource::getLandlordMemberId, landlordMemberId);
        wrapper.eq(HouseSource::getStatus, status);
        return Math.toIntExact(houseSourceMapper.selectCount(wrapper));
    }
}