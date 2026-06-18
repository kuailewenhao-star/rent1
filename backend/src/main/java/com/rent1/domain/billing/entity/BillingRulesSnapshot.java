package com.rent1.domain.billing.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 计费规则快照实体 - 合约签约瞬间的计费规则冻结副本
 *
 * 核心设计背景：
 * 1. 合约创建时，将当前房间的 BillingRules.items 完整拷贝到快照
 * 2. 快照永久不可变，历史账单、押金结算都严格基于当时的快照
 * 3. 即使房东后续调整了计费规则（新租客），已签约的历史数据不受影响
 * 4. 账单域 InvoiceDomain 通过 snapshot.rulesJson 生成首期账单和周期性账单
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingRulesSnapshot {

    /** 快照ID */
    private String snapshotId;

    /** 关联合约ID（UNIQUE，一个合约对应1份快照） */
    private String contractId;

    /** 锁定时的完整计费规则JSON副本 */
    @Builder.Default
    private List<BillingItem> rulesJson = new ArrayList<>();

    /** 锁定时间 */
    private LocalDateTime lockedAt;

    /** 创建时间 */
    private LocalDateTime createTime;

    /**
     * 根据当前计费规则创建不可变快照
     */
    public static BillingRulesSnapshot fromBillingRules(BillingRules rules, String contractId) {
        BillingRulesSnapshot snapshot = new BillingRulesSnapshot();
        snapshot.snapshotId = UUID.randomUUID().toString().replace("-", "");
        snapshot.contractId = contractId;
        snapshot.rulesJson = new ArrayList<>(rules.getItems());
        snapshot.lockedAt = rules.getLockedAt() != null ? rules.getLockedAt() : LocalDateTime.now();
        snapshot.createTime = LocalDateTime.now();
        return snapshot;
    }
}
