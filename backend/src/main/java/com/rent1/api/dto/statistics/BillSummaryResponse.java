package com.rent1.api.dto.statistics;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class BillSummaryResponse {
    private long count;
    private BigDecimal amount;
}
