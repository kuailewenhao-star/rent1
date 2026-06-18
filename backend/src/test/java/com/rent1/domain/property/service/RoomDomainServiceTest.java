package com.rent1.domain.property.service;

import com.rent1.common.enums.ErrorCode;
import com.rent1.common.enums.HouseSourceType;
import com.rent1.common.enums.RoomStatus;
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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 房间领域服务单元测试
 * 
 * 测试覆盖范围：
 * - 正常流程：创建房间、更新房间、上传图片、查询房间
 * - 边界值场景：金额边界校验、房间名称长度校验、图片数量校验
 * - 异常分支场景：整租房源禁止新增房间、已出租房间禁止编辑
 * - 权限校验场景：房源归属校验
 */
@ExtendWith(MockitoExtension.class)
class RoomDomainServiceTest {
    
    @Mock
    private HouseSourceRepository houseSourceRepository;
    
    @Mock
    private RoomRepository roomRepository;
    
    @Mock
    private RoomStatusDomainService roomStatusDomainService;
    
    @InjectMocks
    private RoomDomainService roomDomainService;
    
    private HouseSource testSharedHouseSource;
    private HouseSource testEntireHouseSource;
    private Room testVacantRoom;
    private Room testOccupiedRoom;
    private String testMemberId;
    
    @BeforeEach
    void setUp() {
        testMemberId = "test_member_001";
        
        // 合租房源
        testSharedHouseSource = HouseSource.builder()
                .houseSourceId("shared_house_source_001")
                .landlordMemberId(testMemberId)
                .name("合租测试房源")
                .type(HouseSourceType.SHARED)
                .build();
        
        // 整租房源
        testEntireHouseSource = HouseSource.builder()
                .houseSourceId("entire_house_source_001")
                .landlordMemberId(testMemberId)
                .name("整租测试房源")
                .type(HouseSourceType.ENTIRE)
                .build();
        
        // 空置房间
        testVacantRoom = Room.builder()
                .roomId("vacant_room_001")
                .houseSourceId("shared_house_source_001")
                .roomName("房间1")
                .status(RoomStatus.VACANT)
                .monthlyRent(new BigDecimal("2000.00"))
                .deposit(new BigDecimal("2000.00"))
                .build();
        
        // 已出租房间
        testOccupiedRoom = Room.builder()
                .roomId("occupied_room_001")
                .houseSourceId("shared_house_source_001")
                .roomName("房间2")
                .status(RoomStatus.OCCUPIED)
                .monthlyRent(new BigDecimal("2500.00"))
                .deposit(new BigDecimal("2500.00"))
                .build();
    }
    
    // ========== 正常流程测试 ==========
    
    @Test
    @DisplayName("创建房间 - 合租房源成功创建")
    void testCreateRoomForSharedHouseSource() {
        // 准备数据
        when(houseSourceRepository.findById("shared_house_source_001")).thenReturn(testSharedHouseSource);
        
        // 执行测试
        Room room = roomDomainService.createRoom(
                "shared_house_source_001",
                testMemberId,
                "新增房间",
                new BigDecimal("30.00"),
                new BigDecimal("3000.00"),
                new BigDecimal("3000.00")
        );
        
        // 验证结果
        assertNotNull(room);
        assertEquals("新增房间", room.getRoomName());
        assertEquals(RoomStatus.VACANT, room.getStatus());
        
        // 验证仓储调用
        verify(roomRepository, times(1)).save(any(Room.class));
    }
    
    @Test
    @DisplayName("更新房间 - 空置状态成功更新")
    void testUpdateVacantRoom() {
        // 准备数据
        when(roomRepository.findById("vacant_room_001")).thenReturn(testVacantRoom);
        when(houseSourceRepository.findById("shared_house_source_001")).thenReturn(testSharedHouseSource);
        
        // 执行测试
        roomDomainService.updateRoom(
                "vacant_room_001",
                testMemberId,
                "更新后的房间名",
                new BigDecimal("35.00"),
                new BigDecimal("3500.00"),
                new BigDecimal("3500.00")
        );
        
        // 验证仓储调用
        verify(roomRepository, times(1)).update(any(Room.class));
    }
    
    @Test
    @DisplayName("上传房间图片 - 成功")
    void testUploadRoomImage() {
        // 准备数据
        when(roomRepository.findById("vacant_room_001")).thenReturn(testVacantRoom);
        when(houseSourceRepository.findById("shared_house_source_001")).thenReturn(testSharedHouseSource);
        
        // 执行测试
        roomDomainService.uploadRoomImage("vacant_room_001", testMemberId, "http://example.com/image1.jpg");
        
        // 验证仓储调用
        verify(roomRepository, times(1)).update(any(Room.class));
    }
    
    // ========== 边界值场景测试 ==========
    
    @Test
    @DisplayName("创建房间 - 房间名称超过20字符 - 抛出异常")
    void testCreateRoomWithLongName() {
        // 准备数据
        when(houseSourceRepository.findById("shared_house_source_001")).thenReturn(testSharedHouseSource);
        
        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> roomDomainService.createRoom(
                        "shared_house_source_001",
                        testMemberId,
                        "房间名称超过二十个字符限制测试", // 超过20字符
                        new BigDecimal("30.00"),
                        new BigDecimal("3000.00"),
                        new BigDecimal("3000.00")
                ));
        
        assertEquals(ErrorCode.B002.getCode(), exception.getCode());
    }
    
    @Test
    @DisplayName("创建房间 - 月租金额超过边界 - 抛出异常")
    void testCreateRoomWithExceedingRent() {
        // 准备数据
        when(houseSourceRepository.findById("shared_house_source_001")).thenReturn(testSharedHouseSource);
        
        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> roomDomainService.createRoom(
                        "shared_house_source_001",
                        testMemberId,
                        "新增房间",
                        new BigDecimal("30.00"),
                        new BigDecimal("9999999.99"), // 超过边界
                        new BigDecimal("3000.00")
                ));
        
        assertEquals(ErrorCode.V020.getCode(), exception.getCode());
    }
    
    @Test
    @DisplayName("上传房间图片 - 超过9张限制 - 抛出异常")
    void testUploadRoomImageExceedingLimit() {
        // 准备数据：已有9张图片的房间
        Room roomWith9Images = Room.builder()
                .roomId("room_with_9_images")
                .houseSourceId("shared_house_source_001")
                .roomName("房间1")
                .status(RoomStatus.VACANT)
                .images(Arrays.asList(
                        "img1.jpg", "img2.jpg", "img3.jpg", "img4.jpg", "img5.jpg",
                        "img6.jpg", "img7.jpg", "img8.jpg", "img9.jpg"
                ))
                .build();
        
        when(roomRepository.findById("room_with_9_images")).thenReturn(roomWith9Images);
        when(houseSourceRepository.findById("shared_house_source_001")).thenReturn(testSharedHouseSource);
        
        // 执行测试并验证异常
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
                () -> roomDomainService.uploadRoomImage("room_with_9_images", testMemberId, "img10.jpg"));
        
        assertTrue(exception.getMessage().contains("9张"));
    }
    
    // ========== 异常分支场景测试 ==========
    
    @Test
    @DisplayName("创建房间 - 整租房源禁止新增 - 抛出异常")
    void testCreateRoomForEntireHouseSource() {
        // 准备数据
        when(houseSourceRepository.findById("entire_house_source_001")).thenReturn(testEntireHouseSource);
        
        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> roomDomainService.createRoom(
                        "entire_house_source_001",
                        testMemberId,
                        "新增房间",
                        new BigDecimal("30.00"),
                        new BigDecimal("3000.00"),
                        new BigDecimal("3000.00")
                ));
        
        assertEquals(ErrorCode.B001.getCode(), exception.getCode());
    }
    
    @Test
    @DisplayName("更新房间 - 已出租状态禁止编辑 - 抛出异常")
    void testUpdateOccupiedRoom() {
        // 准备数据
        when(roomRepository.findById("occupied_room_001")).thenReturn(testOccupiedRoom);
        when(houseSourceRepository.findById("shared_house_source_001")).thenReturn(testSharedHouseSource);
        
        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> roomDomainService.updateRoom(
                        "occupied_room_001",
                        testMemberId,
                        "更新后的房间名",
                        new BigDecimal("35.00"),
                        new BigDecimal("3500.00"),
                        new BigDecimal("3500.00")
                ));
        
        assertEquals(ErrorCode.L001.getCode(), exception.getCode());
    }
    
    @Test
    @DisplayName("创建房间 - 无权限（房源归属其他房东） - 抛出异常")
    void testCreateRoomNoPermission() {
        // 准备数据
        HouseSource otherLandlordHouseSource = HouseSource.builder()
                .houseSourceId("other_house_source_001")
                .landlordMemberId("other_member_001") // 归属其他房东
                .type(HouseSourceType.SHARED)
                .build();
        
        when(houseSourceRepository.findById("other_house_source_001")).thenReturn(otherLandlordHouseSource);
        
        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, 
                () -> roomDomainService.createRoom(
                        "other_house_source_001",
                        testMemberId, // 当前用户不是房源归属房东
                        "新增房间",
                        new BigDecimal("30.00"),
                        new BigDecimal("3000.00"),
                        new BigDecimal("3000.00")
                ));
        
        assertEquals(ErrorCode.P001.getCode(), exception.getCode());
    }
    
    // ========== 业务规则校验测试 ==========
    
    @Test
    @DisplayName("房间实体 - 判断是否为空置状态")
    void testRoomIsVacant() {
        assertTrue(testVacantRoom.isVacant());
        assertFalse(testVacantRoom.isOccupied());
    }
    
    @Test
    @DisplayName("房间实体 - 判断是否为已出租状态")
    void testRoomIsOccupied() {
        assertTrue(testOccupiedRoom.isOccupied());
        assertFalse(testOccupiedRoom.isVacant());
    }
    
    @Test
    @DisplayName("房间实体 - 判断是否可以编辑")
    void testRoomCanEdit() {
        assertTrue(testVacantRoom.canEdit());
        assertFalse(testOccupiedRoom.canEdit());
    }
    
    @Test
    @DisplayName("房间实体 - 判断是否可以出租")
    void testRoomCanOccupy() {
        assertTrue(testVacantRoom.canOccupy());
        assertFalse(testOccupiedRoom.canOccupy());
    }
    
    @Test
    @DisplayName("房间实体 - 占用房间（状态变更）")
    void testRoomOccupy() {
        // 执行测试
        testVacantRoom.occupy();
        
        // 验证结果
        assertEquals(RoomStatus.OCCUPIED, testVacantRoom.getStatus());
    }
    
    @Test
    @DisplayName("房间实体 - 占用已出租房间 - 抛出异常")
    void testRoomOccupyAlreadyOccupied() {
        // 执行测试并验证异常
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
                () -> testOccupiedRoom.occupy());
        
        assertTrue(exception.getMessage().contains("不可出租"));
    }
    
    @Test
    @DisplayName("房间实体 - 释放房间（状态变更）")
    void testRoomVacate() {
        // 执行测试
        testOccupiedRoom.vacate();
        
        // 验证结果
        assertEquals(RoomStatus.VACANT, testOccupiedRoom.getStatus());
    }
}