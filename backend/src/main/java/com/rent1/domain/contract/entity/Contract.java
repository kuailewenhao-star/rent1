package com.rent1.domain.contract.entity;

import com.rent1.domain.common.ContractStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 合约实体 - 租赁合约全生命周期管理
 * 核心业务规则：
 * 1. 合约绑定租客、房间、计费规则
 * 2. 合约状态流转：履约中 → 已到期完结 / 提前解约 / 作废
 * 3. 计费规则锁定：合约生效后永久锁定关联计费规则JSON
 * 4. 退租解约闭环：账单截断、押金结算、房间状态复位
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contract {

    /** 合约ID */
    private String contractId;

    /** 租客会员ID */
    private String tenantMemberId;

    /** 关联房间ID */
    private String roomId;

    /** 关联房源ID（冗余字段，便于查询） */
    private String houseSourceId;

    /** 房东会员ID */
    private String landlordMemberId;

    /** 关联计费规则快照ID */
    private String billingRulesSnapshotId;

    /** 合约开始日期 */
    private LocalDate startDate;

    /** 合约结束日期 */
    private LocalDate endDate;

    /** 合约状态 */
    private ContractStatus status;

    /** 纸质合约扫描件URL */
    private String paperContractUrl;

    /** 作废原因 */
    private String voidReason;

    /** 终止类型（提前解约时记录） */
    private String terminationType;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /**
     * 创建合约 - 设置初始状态
     */
    public void initialize() {
        this.status = ContractStatus.ACTIVE;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 校验合约是否可终止
     */
    public boolean canTerminate() {
        return this.status == ContractStatus.ACTIVE;
    }

    /**
     * 校验合约是否可作废
     * 作废条件：合约无任何账单产生、无任何履约记录
     */
    public boolean canVoid() {
        return this.status == ContractStatus.ACTIVE;
    }

    /**
     * 校验合约是否已完结
     */
    public boolean isCompleted() {
        return this.status == ContractStatus.EXPIRED
            || this.status == ContractStatus.TERMINATED_EARLY
            || this.status == ContractStatus.VOID;
    }

    /**
     * 获取合约剩余天数
     */
    public long getRemainingDays() {
        if (this.endDate == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), this.endDate);
    }
}
