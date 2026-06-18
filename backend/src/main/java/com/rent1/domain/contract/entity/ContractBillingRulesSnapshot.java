package com.rent1.domain.contract.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 合约计费规则快照（内存实现）
 * 注意：此为简化内存版本，实际持久化使用 domain.billing.entity.BillingRulesSnapshot
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractBillingRulesSnapshot {

    /** 快照ID */
    private String snapshotId;

    /** 关联合约ID（唯一） */
    private String contractId;

    /** 锁定时的计费规则完整JSON */
    private String rulesJson;

    /** 锁定时间 */
    private LocalDateTime lockedAt;

    /** 创建时间 */
    private LocalDateTime createTime;
}
