package com.rent1.api.dto.statistics;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DashboardResponse {
    private BillSummaryResponse pendingBills;
    private BillSummaryResponse overdueBills;
    private BigDecimal effectiveDepositAmount;
    private RoomSummaryResponse rooms;
    private ProfitSummaryResponse profit;
    private String timeRange;
}
