package com.rent1.domain.property.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.rent1.common.enums.RoomStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 房间实体
 * 
 * 房间是系统最小出租、签约、计费、统计单元，
 * 所有租客、合约、账单数据均关联房间ID。
 * 
 * 核心业务规则：
 * 1. 房间状态仅设置两种物理真实状态：空置中/已出租
 * 2. 即将到期为前端计算标签（≤30天），不修改底层状态
 * 3. 房间状态仅允许空置↔已出租两种流转
 * 4. 已出租房间不可编辑房间信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("room")
public class Room {
    
    /**
     * 房间ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String roomId;
    
    /**
     * 所属房源ID
     */
    private String houseSourceId;
    
    /**
     * 房间名称（≤20字符）
     */
    private String roomName;
    
    /**
     * 房间面积（平方米）
     */
    private BigDecimal area;
    
    /**
     * 月租金额
     * 金额边界：0~999999.99
     */
    private BigDecimal monthlyRent;
    
    /**
     * 押金金额
     * 金额边界：0~999999.99
     */
    private BigDecimal deposit;
    
    /**
     * 房间图片URL数组（JSON格式）
     * 核心业务规则：单房间最多9张图片
     */
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private List<String> images;
    
    /**
     * 房间状态：VACANT空置中/OCCUPIED已出租
     */
    private RoomStatus status;
    
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
     * 占用房间（合约创建成功时调用）
     * 核心业务规则：
     * - 仅空置中房间可出租
     * - 状态变更：空置中 → 已出租
     */
    public void occupy() {
        // 核心规则：仅空置中房间可出租
        if (!status.canOccupy()) {
            throw new IllegalStateException("房间当前不可出租");
        }
        this.status = RoomStatus.OCCUPIED;
    }
    
    /**
     * 释放房间（合约到期/解约时调用）
     * 核心业务规则：
     * - 状态变更：已出租 → 空置中
     */
    public void vacate() {
        this.status = RoomStatus.VACANT;
    }
    
    /**
     * 更新房间信息
     * 核心业务规则：
     * - 仅空置状态允许编辑
     * - 金额边界：月租0~999999.99，押金0~999999.99
     */
    public void updateInfo(String roomName, BigDecimal area, 
                          BigDecimal monthlyRent, BigDecimal deposit) {
        // 核心规则：仅空置状态允许编辑
        if (!status.canEdit()) {
            throw new IllegalStateException("房间已出租，不可编辑");
        }
        
        // 金额边界校验
        validateAmount(monthlyRent, "月租");
        validateAmount(deposit, "押金");
        
        this.roomName = roomName;
        this.area = area;
        this.monthlyRent = monthlyRent;
        this.deposit = deposit;
    }
    
    /**
     * 上传图片
     * 核心业务规则：
     * - 单房间最多9张图片
     * - 图片格式：jpg/png，大小≤5MB
     */
    public void addImage(String imageUrl) {
        if (images == null) {
            images = new ArrayList<>();
        }
        
        // 核心规则：单房间最多9张图片
        if (images.size() >= 9) {
            throw new IllegalStateException("单房间最多上传9张图片");
        }
        
        images.add(imageUrl);
    }
    
    /**
     * 删除图片
     */
    public void removeImage(String imageUrl) {
        if (images != null) {
            images.remove(imageUrl);
        }
    }
    
    /**
     * 判断是否为空置状态
     */
    public boolean isVacant() {
        return status == RoomStatus.VACANT;
    }
    
    /**
     * 判断是否为已出租状态
     */
    public boolean isOccupied() {
        return status == RoomStatus.OCCUPIED;
    }
    
    /**
     * 判断是否可以编辑
     */
    public boolean canEdit() {
        return status.canEdit();
    }
    
    /**
     * 判断是否可以出租
     */
    public boolean canOccupy() {
        return status.canOccupy();
    }
    
    /**
     * 获取图片数量
     */
    public int getImageCount() {
        return images == null ? 0 : images.size();
    }
    
    // ========== 私有方法 ==========
    
    /**
     * 金额边界校验
     */
    private void validateAmount(BigDecimal amount, String fieldName) {
        if (amount != null) {
            if (amount.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException(fieldName + "不能为负数");
            }
            if (amount.compareTo(new BigDecimal("999999.99")) > 0) {
                throw new IllegalArgumentException(fieldName + "超出允许范围");
            }
        }
    }
}