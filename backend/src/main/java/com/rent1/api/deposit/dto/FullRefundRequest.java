package com.rent1.api.deposit.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 全额退还押金请求
 * DEP-001: 押金结算（全额退还）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FullRefundRequest extends DepositSettlementRequest {
    // 仅需要 depositId，全额退还不需额外参数
}
