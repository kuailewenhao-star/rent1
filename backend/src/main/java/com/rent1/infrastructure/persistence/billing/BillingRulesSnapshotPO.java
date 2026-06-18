package com.rent1.infrastructure.persistence.billing;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * billing_rules_snapshot 表持久化对象
 */
@Data
@TableName("billing_rules_snapshot")
public class BillingRulesSnapshotPO {

    @TableId
    private String snapshotId;

    @TableField("contract_id")
    private String contractId;

    /** JSON格式的锁定时完整计费规则 */
    @TableField("rules_json")
    private String rulesJson;

    @TableField("locked_at")
    private LocalDateTime lockedAt;

    @TableField("create_time")
    private LocalDateTime createTime;
}
