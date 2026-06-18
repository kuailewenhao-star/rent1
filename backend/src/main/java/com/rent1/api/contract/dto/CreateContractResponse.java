package com.rent1.api.contract.dto;

import lombok.Data;
import lombok.Builder;
import com.rent1.domain.common.ContractStatus;
import java.time.LocalDate;
import java.util.List;

/**
 * 合约创建响应DTO
 */
@Data
@Builder
public class CreateContractResponse {

    /** 合约ID */
    private String contractId;

    /** 合约状态 */
    private String status;

    /** 房间ID */
    private String roomId;

    /** 租客会员ID */
    private String tenantMemberId;

    /** 计费规则快照ID */
    private String billingRulesSnapshotId;

    /** 首期账单ID列表 */
    private List<String> firstBillIds;
}
