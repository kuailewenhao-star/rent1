package com.rent1.domain.statistics;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 单房源盈利详情聚合根
 * <p>
 * 核心口径：
 * - 收入明细：该房源下所有房间的非作废收入账单（按计费周期筛选）
 * - 支出明细：该房源下所有非作废支出账单（按支出发生日期筛选）
 * - 利润 = 收入总额 - 支出总额（允许为负，体现亏损）
 */
@Data
@Builder
public class ProfitDetail {

    /** 收入总额 */
    private BigDecimal incomeTotal;
    /** 支出总额 */
    private BigDecimal expenseTotal;
    /** 利润 = 收入 - 支出 */
    private BigDecimal profit;
    /** 收入账单明细 */
    private List<IncomeItem> incomeList;
    /** 支出账单明细 */
    private List<ExpenseItem> expenseList;

    @Data
    @Builder
    public static class IncomeItem {
        private String invoiceId;
        /** 费用类型（RENT/WATER/...） */
        private String feeTypeCode;
        /** 费用类型中文名（租金/水费/...） */
        private String feeTypeName;
        /** 关联房间ID */
        private String roomId;
        /** 关联房间名称（冗余，便于前端展示） */
        private String roomName;
        /** 计费周期起始 */
        private LocalDate cycleStart;
        /** 计费周期结束 */
        private LocalDate cycleEnd;
        /** 金额 */
        private BigDecimal amount;
        /** 是否手动录入 */
        private Boolean isManual;
        /** 当前账单状态（PENDING/PAID/OVERDUE/DEPOSIT_RECEIVED） */
        private String status;
    }

    @Data
    @Builder
    public static class ExpenseItem {
        private String expenseId;
        /** 支出费用类型（HOUSE_RENT/WATER/...） */
        private String costTypeCode;
        /** 支出费用类型中文名 */
        private String costTypeName;
        /** 支出发生日期 */
        private LocalDate costDate;
        /** 金额 */
        private BigDecimal amount;
        /** 备注 */
        private String remark;
    }
}
