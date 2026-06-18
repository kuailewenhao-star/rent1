package com.rent1.domain.billing.entity;

import com.rent1.domain.billing.FeeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 计费规则聚合根 - 房间计费规则管理
 *
 * 核心业务规则：
 * 1. 空置房间可自由配置费用项（增删改）
 * 2. 同房间同FeeType仅允许1条配置（防重复）
 * 3. 租金RENT/押金DEPOSIT为系统自带固定项，不可删除
 * 4. 合约创建后 isLocked=true，所有编辑操作被拦截（计费锁机规则R-001）
 * 5. 锁定后所有变更必须走"退租→重签"流程
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingRules {

    /** 计费规则ID */
    private String rulesId;

    /** 关联房间ID（UNIQUE，每个房间仅对应1套计费规则） */
    private String roomId;

    /** 费用项列表（JSON存储） */
    @Builder.Default
    private List<BillingItem> items = new ArrayList<>();

    /** 是否已锁定：合约创建后自动锁定，true时禁止一切编辑操作 */
    private boolean locked;

    /** 锁定时间 */
    private LocalDateTime lockedAt;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /**
     * 初始化新房间计费规则 - 自动添加租金、押金两项系统固定费用
     */
    public void initialize(String roomId) {
        this.rulesId = UUID.randomUUID().toString().replace("-", "");
        this.roomId = roomId;
        this.locked = false;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
    }

    /**
     * 判断是否已锁定 - 所有编辑操作前的第一道拦截
     */
    public boolean isLocked() {
        return this.locked;
    }

    /**
     * 锁机 - 合约创建瞬间调用，锁定后所有编辑入口均校验此字段
     * 发布 BillingRulesLockedEvent 的前置条件
     */
    public void lock() {
        this.locked = true;
        this.lockedAt = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 检查指定费用类型是否已存在（同房间同FeeType唯一性校验）
     */
    public boolean containsFeeType(FeeType feeType) {
        if (feeType == null || items == null) {
            return false;
        }
        return items.stream().anyMatch(item -> feeType.equals(item.getFeeType()));
    }

    /**
     * 获取指定费用类型的计费项
     */
    public Optional<BillingItem> findItemByFeeType(FeeType feeType) {
        if (feeType == null || items == null) {
            return Optional.empty();
        }
        return items.stream()
                .filter(item -> feeType.equals(item.getFeeType()))
                .findFirst();
    }

    /**
     * 新增计费项（前置：房间空置且未锁定 + 同feeType不重复）
     */
    public void addItem(BillingItem item) {
        if (item == null) {
            return;
        }
        item.normalizeByFeeType();
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        this.items.add(item);
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 更新指定费用类型的计费项配置
     * 注意：feeType本身不可变，仅允许更新金额/周期/方式/备注
     */
    public void updateItem(FeeType feeType, BillingItem updated) {
        if (feeType == null || updated == null || items == null) {
            return;
        }
        for (int i = 0; i < items.size(); i++) {
            BillingItem existing = items.get(i);
            if (feeType.equals(existing.getFeeType())) {
                // 保留 feeType/feeTypeName/immutable 不变
                existing.setChargeType(updated.getChargeType());
                existing.setChargeValue(updated.getChargeValue());
                existing.setChargeCycle(updated.getChargeCycle());
                existing.setRemark(updated.getRemark());
                this.updateTime = LocalDateTime.now();
                return;
            }
        }
    }

    /**
     * 删除指定费用类型的计费项（租金/押金不可删除）
     */
    public void removeItem(FeeType feeType) {
        if (feeType == null || items == null) {
            return;
        }
        this.items = items.stream()
                .filter(item -> !feeType.equals(item.getFeeType()))
                .collect(Collectors.toList());
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 统计费用项数量
     */
    public int itemCount() {
        return items == null ? 0 : items.size();
    }
}
