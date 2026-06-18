package com.rent1.domain.statistics;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 房东首页看板聚合根（Statistics 领域核心聚合根）
 * <p>
 * 承载三大数据板块的业务规则与计算逻辑：
 * 1. 待支付 / 逾期账单统计（数量、金额、是否需要关注）
 * 2. 房间房源状态统计（总房间数、空置数、已出租数、即将到期数）
 * 3. 经营盈亏统计（收入总额、支出总额、利润 = 收入 - 支出）
 * <p>
 * 核心业务规则（R-006 作废数据不参与统计）：
 * - Contract VOID / Invoice VOID / HouseSource VOID 全部过滤
 * - 收入按计费周期 cycleStart/cycleEnd 归属，不按 createTime
 * - 支出按 costDate 归属
 */
@Data
@Builder
public class DashboardStatistics {

    /** 待支付账单统计 */
    private BillSummary pendingBills;

    /** 逾期账单统计 */
    private BillSummary overdueBills;

    /** 当前持有有效押金总额（仅履约中合约押金） */
    private BigDecimal effectiveDepositAmount;

    /** 房间房源状态统计 */
    private RoomSummary rooms;

    /** 经营盈亏统计 */
    private ProfitSummary profit;

    /** 时间范围（用于前端展示当前统计口径） */
    private String timeRange;

    /**
     * 账单子聚合 - 统一表达"数量 + 金额"
     */
    @Data
    @Builder
    public static class BillSummary {
        /** 账单数量 */
        private long count;
        /** 总金额 */
        private BigDecimal amount;

        public static BillSummary zero() {
            return BillSummary.builder().count(0L).amount(BigDecimal.ZERO).build();
        }
    }

    /**
     * 房间/房源子聚合
     */
    @Data
    @Builder
    public static class RoomSummary {
        /** 总房间数（按房东下所有有效房源统计） */
        private long total;
        /** 空置中房间数（status=VACANT 且所属房源未停用） */
        private long vacant;
        /** 已出租房间数（status=OCCUPIED） */
        private long occupied;
        /** 即将到期房间数（租约剩余≤30天的活跃合约房间数） */
        private long expiringSoon;
    }

    /**
     * 经营盈亏子聚合
     */
    @Data
    @Builder
    public static class ProfitSummary {
        /** 收入总额（全部有效非作废收入账单，按计费周期筛选） */
        private BigDecimal income;
        /** 支出总额（房东手动录入支出账单，按 costDate 筛选） */
        private BigDecimal expense;
        /** 利润 = 收入 - 支出 */
        private BigDecimal profit;

        public static ProfitSummary zero() {
            return ProfitSummary.builder()
                    .income(BigDecimal.ZERO)
                    .expense(BigDecimal.ZERO)
                    .profit(BigDecimal.ZERO).build();
        }
    }
}
