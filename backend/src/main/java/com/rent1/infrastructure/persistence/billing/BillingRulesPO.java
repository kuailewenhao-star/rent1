package com.rent1.infrastructure.persistence.billing;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * billing_rules 表持久化对象
 * items_json 字段以 JSON 字符串形式存储整个费用项列表
 */
@Data
@TableName("billing_rules")
public class BillingRulesPO {

    @TableId
    private String rulesId;

    @TableField("room_id")
    private String roomId;

    /** JSON格式的费用项列表 */
    @TableField("items_json")
    private String itemsJson;

    @TableField("is_locked")
    private Boolean isLocked;

    @TableField("locked_at")
    private LocalDateTime lockedAt;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
