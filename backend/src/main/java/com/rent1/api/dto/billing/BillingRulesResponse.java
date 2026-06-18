package com.rent1.api.dto.billing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 房间计费规则整体响应DTO
 * 包含：费用项列表、锁定状态标识
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingRulesResponse {

    /** 计费规则ID */
    private String rulesId;

    /** 关联房间ID */
    private String roomId;

    /** 是否已锁定（签约后锁定） */
    private boolean locked;

    /** 锁定时间 */
    private LocalDateTime lockedAt;

    /** 费用项列表 */
    private List<BillingItemResponse> items;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
