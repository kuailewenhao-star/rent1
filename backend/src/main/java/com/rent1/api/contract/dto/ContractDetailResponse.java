package com.rent1.api.contract.dto;

import lombok.Data;
import lombok.Builder;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 合约详情响应DTO
 */
@Data
@Builder
public class ContractDetailResponse {

    /** 合约ID */
    private String contractId;

    /** 房间ID */
    private String roomId;

    /** 房源ID */
    private String houseSourceId;

    /** 租客会员ID */
    private String tenantMemberId;

    /** 租客姓名（脱敏） */
    private String tenantName;

    /** 租客手机号（脱敏） */
    private String tenantPhone;

    /** 合约开始日期 */
    private LocalDate startDate;

    /** 合约结束日期 */
    private LocalDate endDate;

    /** 合约状态 */
    private String status;

    /** 状态描述 */
    private String statusDesc;

    /** 纸质合约URL */
    private String paperContractUrl;

    /** 计费规则JSON */
    private String billingRules;

    /** 剩余天数 */
    private long remainingDays;

    /** 创建时间 */
    private LocalDateTime createTime;
}
