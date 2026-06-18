package com.rent1.api.contract.dto;

import lombok.Data;
import lombok.Builder;
import java.time.LocalDate;

/**
 * 合约列表项响应DTO
 */
@Data
@Builder
public class ContractListItemResponse {

    /** 合约ID */
    private String contractId;

    /** 房间ID */
    private String roomId;

    /** 租客会员ID */
    private String tenantMemberId;

    /** 租客姓名（脱敏） */
    private String tenantName;

    /** 合约开始日期 */
    private LocalDate startDate;

    /** 合约结束日期 */
    private LocalDate endDate;

    /** 合约状态 */
    private String status;

    /** 状态描述 */
    private String statusDesc;

    /** 剩余天数 */
    private long remainingDays;
}
