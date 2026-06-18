package com.rent1.domain.property.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.rent1.common.enums.HouseSourceType;
import com.rent1.common.enums.HouseSourceStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 房源实体（聚合根）
 * 
 * 房源为整套房屋管理载体，记录房东收房成本与房屋基础信息，
 * 仅用于成本核算与数据归类，不对外出租、不单独计费。
 * 
 * 核心业务规则：
 * 1. 房源层级不存储图片、租金，所有出租属性全部下沉至房间层级
 * 2. 停用/终止房源仅留存历史数据、可回溯查看，不再参与平台出租率、房间存量统计
 * 3. V1.0不支持房源物理删除，仅做状态管控
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("house_source")
public class HouseSource {
    
    /**
     * 房源ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String houseSourceId;
    
    /**
     * 房东会员ID（归属）
     */
    private String landlordMemberId;
    
    /**
     * 房源名称（≤50字符）
     */
    private String name;
    
    /**
     * 省份
     */
    private String province;
    
    /**
     * 城市
     */
    private String city;
    
    /**
     * 区县
     */
    private String district;
    
    /**
     * 详细地址
     */
    private String address;
    
    /**
     * 总户型数
     */
    private Integer totalRooms;
    
    /**
     * 房源类型：ENTIRE整租/SHARED合租
     */
    private HouseSourceType type;
    
    /**
     * 房源业务状态：NORMAL正常/LEASE_EXPIRED到期停用/TERMINATED终止经营/VOID作废
     */
    private HouseSourceStatus status;
    
    /**
     * 承租起始时间（与大房东的合约）
     */
    private LocalDate leaseStart;
    
    /**
     * 承租结束时间（与大房东的合约）
     */
    private LocalDate leaseEnd;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /**
     * 逻辑删除标记（0未删除，1已删除）
     */
    @TableLogic
    private Integer deleted;
    
    // ========== 领域行为方法（核心业务规则下沉到实体） ==========
    
    /**
     * 创建默认房间
     * 核心业务规则：
     * - 整租房源：自动生成1间房间，room_name="整套"
     * - 合租房源：自动生成2间房间，room_name="房间1"/"房间2"
     */
    public List<Room> createDefaultRooms() {
        List<Room> rooms = new ArrayList<>();
        
        if (type == HouseSourceType.ENTIRE) {
            // 整租房源：生成1间"整套"房间
            Room entireRoom = Room.builder()
                    .houseSourceId(this.houseSourceId)
                    .roomName("整套")
                    .status(com.rent1.common.enums.RoomStatus.VACANT)
                    .build();
            rooms.add(entireRoom);
        } else if (type == HouseSourceType.SHARED) {
            // 合租房源：生成2间默认房间
            Room room1 = Room.builder()
                    .houseSourceId(this.houseSourceId)
                    .roomName("房间1")
                    .status(com.rent1.common.enums.RoomStatus.VACANT)
                    .build();
            Room room2 = Room.builder()
                    .houseSourceId(this.houseSourceId)
                    .roomName("房间2")
                    .status(com.rent1.common.enums.RoomStatus.VACANT)
                    .build();
            rooms.add(room1);
            rooms.add(room2);
        }
        
        return rooms;
    }
    
    /**
     * 更新基础信息
     * 核心业务规则：
     * - 仅允许编辑：名称、地址、总户型、承租时间
     * - 房源类型（整租/合租）创建后不可修改
     * - 停用房源不可编辑
     */
    public void updateBasicInfo(String name, String province, String city, 
                                String district, String address, Integer totalRooms,
                                LocalDate leaseStart, LocalDate leaseEnd) {
        // 校验：停用房源不可编辑
        if (status.isDeactivated()) {
            throw new IllegalStateException("停用房源不可编辑");
        }
        
        this.name = name;
        this.province = province;
        this.city = city;
        this.district = district;
        this.address = address;
        this.totalRooms = totalRooms;
        this.leaseStart = leaseStart;
        this.leaseEnd = leaseEnd;
    }
    
    /**
     * 变更状态（停用房源）
     * 核心业务规则：
     * - 将status改为LEASE_EXPIRED或TERMINATED
     * - 下属房间不可新增出租、新建账单
     * - V1.0不支持物理删除，仅状态管控
     */
    public void changeStatus(HouseSourceStatus newStatus, String reason) {
        // 校验：已停用房源再次停用
        if (status.isDeactivated() && newStatus.isDeactivated()) {
            throw new IllegalStateException("房源已处于停用状态");
        }
        
        this.status = newStatus;
    }
    
    /**
     * 判断是否为合租房源
     */
    public boolean isShared() {
        return type == HouseSourceType.SHARED;
    }
    
    /**
     * 判断是否为整租房源
     */
    public boolean isEntire() {
        return type == HouseSourceType.ENTIRE;
    }
    
    /**
     * 判断是否可以编辑
     * 核心业务规则：停用房源不可编辑
     */
    public boolean canEdit() {
        return !status.isDeactivated();
    }
    
    /**
     * 判断是否归属指定房东
     */
    public boolean belongsTo(String memberId) {
        return this.landlordMemberId.equals(memberId);
    }
}