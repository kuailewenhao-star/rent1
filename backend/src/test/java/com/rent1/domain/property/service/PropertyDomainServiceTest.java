package com.rent1.domain.property.service;

import com.rent1.common.enums.ErrorCode;
import com.rent1.common.enums.HouseSourceStatus;
import com.rent1.common.enums.HouseSourceType;
import com.rent1.domain.common.BusinessException;
import com.rent1.domain.property.entity.HouseSource;
import com.rent1.domain.property.entity.Room;
import com.rent1.domain.property.repository.HouseSourceRepository;
import com.rent1.domain.property.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 房源领域服务单元测试
 * 
 * 测试覆盖范围：
 * - 正常流程：创建房源、更新房源、停用房源、查询房源
 * - 边界值场景：承租时间校验、地址完整性校验
 * - 异常分支场景：权限校验、状态校验
 * - 业务规则校验：整租/合租房源默认房间生成
 */
@ExtendWith(MockitoExtension.class)
class PropertyDomainServiceTest {
    
    @Mock
    private HouseSourceRepository houseSourceRepository;
    
    @Mock
    private RoomRepository roomRepository;
    
    @InjectMocks
    private PropertyDomainService propertyDomainService;
    
    private HouseSource testHouseSource;
    private String testMemberId;
    
    @BeforeEach
    void setUp() {
        testMemberId = "test_member_001";
        
        testHouseSource = HouseSource.builder()
                .houseSourceId("test_house_source_001")
                .landlordMemberId(testMemberId)
                .name("测试房源")
                .province("广东省")
                .city("深圳市")
                .district("南山区")
                .address("科技园路100号")
                .totalRooms(3)
                .type(HouseSourceType.SHARED)
                .status(HouseSourceStatus.NORMAL)
                .leaseStart(LocalDate.of(2026, 1, 1))
                .leaseEnd(LocalDate.of(2027, 12, 31))
                .build();
    }
    
    // ========== 正常流程测试 ==========
    
    @Test
    @DisplayName("创建整租房源 - 成功生成1间房间")
    void testCreateEntireHouseSource() {
        // 准备数据
        HouseSource entireHouseSource = HouseSource.builder()
                .landlordMemberId(testMemberId)
                .name("整租测试房源")
                .province("广东省")
                .city("深圳市")
                .district("南山区")
                .address("科技园路100号")
                .totalRooms(1)
                .type(HouseSourceType.ENTIRE)
                .leaseStart(LocalDate.of(2026, 1, 1))
                .leaseEnd(LocalDate.of(2027, 12, 31))
                .build();
        
        // 执行测试
        List<Room> rooms = propertyDomainService.createHouseSource(entireHouseSource);
        
        // 验证结果
        assertNotNull(rooms);
        assertEquals(1, rooms.size());
        assertEquals("整套", rooms.get(0).getRoomName());
        
        // 验证仓储调用
        verify(houseSourceRepository, times(1)).save(any(HouseSource.class));
        verify(roomRepository, times(1)).saveAll(anyList());
    }
    
    @Test
    @DisplayName("创建合租房源 - 成功生成2间房间")
    void testCreateSharedHouseSource() {
        // 准备数据
        HouseSource sharedHouseSource = HouseSource.builder()
                .landlordMemberId(testMemberId)
                .name("合租测试房源")
                .province("广东省")
                .city("深圳市")
                .district("南山区")
                .address("科技园路100号")
                .totalRooms(3)
                .type(HouseSourceType.SHARED)
                .leaseStart(LocalDate.of(2026, 1, 1))
                .leaseEnd(LocalDate.of(2027, 12, 31))
                .build();
        
        // 执行测试
        List<Room> rooms = propertyDomainService.createHouseSource(sharedHouseSource);
        
        // 验证结果
        assertNotNull(rooms);
        assertEquals(2, rooms.size());
        assertEquals("房间1", rooms.get(0).getRoomName());
        assertEquals("房间2", rooms.get(1).getRoomName());
        
        // 验证仓储调用
        verify(houseSourceRepository, times(1)).save(any(HouseSource.class));
        verify(roomRepository, times(1)).saveAll(anyList());
    }
    
    @Test
    @DisplayName("更新房源基础信息 - 成功")
    void testUpdateHouseSource() {
        // 准备数据
        when(houseSourceRepository.findById("test_house_source_001")).thenReturn(testHouseSource);
        
        // 执行测试
        propertyDomainService.updateHouseSource(
                "test_house_source_001",
                testMemberId,
                "更新后的房源名称",
                "广东省",
                "深圳市",
                "福田区",
                "福田路200号",
                4,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2028, 12, 31)
        );
        
        // 验证结果
        verify(houseSourceRepository, times(1)).update(any(HouseSource.class));
    }
    
    @Test
    @DisplayName("停用房源 - 成功")
    void testDeactivateHouseSource() {
        // 准备数据
        when(houseSourceRepository.findById("test_house_source_001")).thenReturn(testHouseSource);
        
        // 执行测试
        propertyDomainService.deactivateHouseSource(
                "test_house_source_001",
                testMemberId,
                HouseSourceStatus.LEASE_EXPIRED,
                "租期到期"
        );
        
        // 验证结果
        verify(houseSourceRepository, times(1)).update(any(HouseSource.class));
    }
    
    // ========== 边界值场景测试 ==========
    
    @Test
    @DisplayName("创建房源 - 承租结束时间早于开始时间 - 抛出异常")
    void testCreateHouseSourceWithInvalidLeasePeriod() {
        // 准备数据
        HouseSource invalidHouseSource = HouseSource.builder()
                .landlordMemberId(testMemberId)
                .name("测试房源")
                .province("广东省")
                .city("深圳市")
                .district("南山区")
                .address("科技园路100号")
                .totalRooms(3)
                .type(HouseSourceType.SHARED)
                .leaseStart(LocalDate.of(2027, 12, 31)) // 开始时间晚于结束时间
                .leaseEnd(LocalDate.of(2026, 1, 1))
                .build();
        
        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> propertyDomainService.createHouseSource(invalidHouseSource));
        
        assertEquals(ErrorCode.V011.getCode(), exception.getCode());
    }
    
    @Test
    @DisplayName("创建房源 - 地址不完整（缺少区县） - 抛出异常")
    void testCreateHouseSourceWithIncompleteAddress() {
        // 准备数据
        HouseSource incompleteAddressHouseSource = HouseSource.builder()
                .landlordMemberId(testMemberId)
                .name("测试房源")
                .province("广东省")
                .city("深圳市")
                .district(null) // 缺少区县
                .address("科技园路100号")
                .totalRooms(3)
                .type(HouseSourceType.SHARED)
                .leaseStart(LocalDate.of(2026, 1, 1))
                .leaseEnd(LocalDate.of(2027, 12, 31))
                .build();
        
        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> propertyDomainService.createHouseSource(incompleteAddressHouseSource));
        
        assertEquals(ErrorCode.V010.getCode(), exception.getCode());
    }
    
    // ========== 异常分支场景测试 ==========
    
    @Test
    @DisplayName("更新房源 - 房源不存在 - 抛出异常")
    void testUpdateHouseSourceNotFound() {
        // 准备数据
        when(houseSourceRepository.findById("not_exist_id")).thenReturn(null);
        
        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> propertyDomainService.updateHouseSource(
                        "not_exist_id",
                        testMemberId,
                        "更新后的房源名称",
                        "广东省",
                        "深圳市",
                        "福田区",
                        "福田路200号",
                        4,
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2028, 12, 31)
                ));
        
        assertEquals(ErrorCode.P001.getCode(), exception.getCode());
    }
    
    @Test
    @DisplayName("更新房源 - 无权限（房源归属其他房东） - 抛出异常")
    void testUpdateHouseSourceNoPermission() {
        // 准备数据
        HouseSource otherLandlordHouseSource = HouseSource.builder()
                .houseSourceId("other_house_source_001")
                .landlordMemberId("other_member_001") // 归属其他房东
                .name("其他房东房源")
                .province("广东省")
                .city("深圳市")
                .district("南山区")
                .address("科技园路100号")
                .totalRooms(3)
                .type(HouseSourceType.SHARED)
                .status(HouseSourceStatus.NORMAL)
                .leaseStart(LocalDate.of(2026, 1, 1))
                .leaseEnd(LocalDate.of(2027, 12, 31))
                .build();
        
        when(houseSourceRepository.findById("other_house_source_001")).thenReturn(otherLandlordHouseSource);
        
        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> propertyDomainService.updateHouseSource(
                        "other_house_source_001",
                        testMemberId, // 当前用户不是房源归属房东
                        "更新后的房源名称",
                        "广东省",
                        "深圳市",
                        "福田区",
                        "福田路200号",
                        4,
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2028, 12, 31)
                ));
        
        assertEquals(ErrorCode.P001.getCode(), exception.getCode());
    }
    
    @Test
    @DisplayName("更新房源 - 房源已停用 - 抛出异常")
    void testUpdateDeactivatedHouseSource() {
        // 准备数据
        HouseSource deactivatedHouseSource = HouseSource.builder()
                .houseSourceId("deactivated_house_source_001")
                .landlordMemberId(testMemberId)
                .name("已停用房源")
                .province("广东省")
                .city("深圳市")
                .district("南山区")
                .address("科技园路100号")
                .totalRooms(3)
                .type(HouseSourceType.SHARED)
                .status(HouseSourceStatus.LEASE_EXPIRED) // 已停用
                .leaseStart(LocalDate.of(2026, 1, 1))
                .leaseEnd(LocalDate.of(2027, 12, 31))
                .build();
        
        when(houseSourceRepository.findById("deactivated_house_source_001")).thenReturn(deactivatedHouseSource);
        
        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> propertyDomainService.updateHouseSource(
                        "deactivated_house_source_001",
                        testMemberId,
                        "更新后的房源名称",
                        "广东省",
                        "深圳市",
                        "福田区",
                        "福田路200号",
                        4,
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2028, 12, 31)
                ));
        
        assertEquals(ErrorCode.P002.getCode(), exception.getCode());
    }
    
    @Test
    @DisplayName("停用房源 - 房源已处于停用状态 - 抛出异常")
    void testDeactivateAlreadyDeactivatedHouseSource() {
        // 准备数据
        HouseSource deactivatedHouseSource = HouseSource.builder()
                .houseSourceId("deactivated_house_source_001")
                .landlordMemberId(testMemberId)
                .name("已停用房源")
                .province("广东省")
                .city("深圳市")
                .district("南山区")
                .address("科技园路100号")
                .totalRooms(3)
                .type(HouseSourceType.SHARED)
                .status(HouseSourceStatus.LEASE_EXPIRED) // 已停用
                .leaseStart(LocalDate.of(2026, 1, 1))
                .leaseEnd(LocalDate.of(2027, 12, 31))
                .build();
        
        when(houseSourceRepository.findById("deactivated_house_source_001")).thenReturn(deactivatedHouseSource);
        
        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> propertyDomainService.deactivateHouseSource(
                        "deactivated_house_source_001",
                        testMemberId,
                        HouseSourceStatus.TERMINATED,
                        "主动终止"
                ));
        
        assertEquals(ErrorCode.P010.getCode(), exception.getCode());
    }
    
    // ========== 业务规则校验测试 ==========
    
    @Test
    @DisplayName("房源实体 - 判断是否为合租房源")
    void testHouseSourceIsShared() {
        assertTrue(testHouseSource.isShared());
        assertFalse(testHouseSource.isEntire());
    }
    
    @Test
    @DisplayName("房源实体 - 判断是否可以编辑（正常状态）")
    void testHouseSourceCanEdit() {
        assertTrue(testHouseSource.canEdit());
    }
    
    @Test
    @DisplayName("房源实体 - 判断是否归属指定房东")
    void testHouseSourceBelongsTo() {
        assertTrue(testHouseSource.belongsTo(testMemberId));
        assertFalse(testHouseSource.belongsTo("other_member_001"));
    }
}