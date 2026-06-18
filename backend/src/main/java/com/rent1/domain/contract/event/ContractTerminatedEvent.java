package com.rent1.domain.contract.event;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 合约终止领域事件
 * 触发时机：合约到期 / 提前解约 / 作废
 * 订阅方：账单域截断后续账单、押金域结算押金、房源域复位房间状态、消息域发送通知
 */
@Data
@NoArgsConstructor
public class ContractTerminatedEvent {

    /** 合约ID */
    private String contractId;

    /** 终止类型：EXPIRED到期 / EARLY_TERMINATED提前解约 / VOID作废 */
    private String terminationType;

    /** 租客会员ID */
    private String tenantMemberId;

    /** 房间ID */
    private String roomId;

    /** 房源ID */
    private String houseSourceId;

    /** 房东会员ID */
    private String landlordMemberId;

    /** 事件发生时间 */
    private LocalDateTime occurredOn;

    public ContractTerminatedEvent(String contractId, String terminationType,
                                   String tenantMemberId, String roomId,
                                   String houseSourceId, String landlordMemberId) {
        this.contractId = contractId;
        this.terminationType = terminationType;
        this.tenantMemberId = tenantMemberId;
        this.roomId = roomId;
        this.houseSourceId = houseSourceId;
        this.landlordMemberId = landlordMemberId;
        this.occurredOn = LocalDateTime.now();
    }

    /** 终止类型常量 */
    public static final String TYPE_EXPIRED = "EXPIRED";
    public static final String TYPE_EARLY_TERMINATED = "EARLY_TERMINATED";
    public static final String TYPE_VOID = "VOID";
}
