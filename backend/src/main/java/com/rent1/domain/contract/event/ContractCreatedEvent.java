package com.rent1.domain.contract.event;

import com.rent1.domain.common.ContractStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * 合约创建领域事件
 * 触发时机：合约创建成功
 * 订阅方：计费域锁定计费规则、房源域变更房间状态、消息域发送通知
 */
@Data
@NoArgsConstructor
public class ContractCreatedEvent {

    /** 合约ID */
    private String contractId;

    /** 租客会员ID */
    private String tenantMemberId;

    /** 房间ID */
    private String roomId;

    /** 房源ID */
    private String houseSourceId;

    /** 房东会员ID */
    private String landlordMemberId;

    /** 计费规则快照ID */
    private String billingRulesSnapshotId;

    /** 合约开始日期 */
    private LocalDate startDate;

    /** 合约结束日期 */
    private LocalDate endDate;

    /** 合约状态 */
    private ContractStatus status;

    /** 事件发生时间 */
    private LocalDateTime occurredOn;

    public ContractCreatedEvent(String contractId, String tenantMemberId, String roomId,
                                 String houseSourceId, String landlordMemberId,
                                 String billingRulesSnapshotId, LocalDate startDate,
                                 LocalDate endDate, ContractStatus status) {
        this.contractId = contractId;
        this.tenantMemberId = tenantMemberId;
        this.roomId = roomId;
        this.houseSourceId = houseSourceId;
        this.landlordMemberId = landlordMemberId;
        this.billingRulesSnapshotId = billingRulesSnapshotId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.occurredOn = LocalDateTime.now();
    }
}
