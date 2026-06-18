package com.rent1.api.dto.statistics;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class ProfitDetailResponse {

    private BigDecimal incomeTotal;
    private BigDecimal expenseTotal;
    private BigDecimal profit;
    private List<IncomeItem> incomeList;
    private List<ExpenseItem> expenseList;

    @Data
    @Builder
    public static class IncomeItem {
        private String invoiceId;
        private String feeTypeCode;
        private String feeTypeName;
        private String roomId;
        private String roomName;
        private LocalDate cycleStart;
        private LocalDate cycleEnd;
        private BigDecimal amount;
        private Boolean isManual;
        private String status;
    }

    @Data
    @Builder
    public static class ExpenseItem {
        private String expenseId;
        private String costTypeCode;
        private String costTypeName;
        private LocalDate costDate;
        private BigDecimal amount;
        private String remark;
    }
}
