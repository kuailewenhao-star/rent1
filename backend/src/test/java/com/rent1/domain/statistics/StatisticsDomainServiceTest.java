package com.rent1.domain.statistics;

import com.rent1.domain.contract.entity.Contract;
import com.rent1.domain.contract.repository.ContractRepository;
import com.rent1.domain.invoice.entity.IncomeInvoice;
import com.rent1.domain.invoice.entity.ExpenseInvoice;
import com.rent1.domain.invoice.enums.CostType;
import com.rent1.domain.invoice.enums.FeeType;
import com.rent1.domain.invoice.enums.InvoiceStatus;
import com.rent1.domain.property.entity.Room;
import com.rent1.common.enums.RoomStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 数据域 Statistics Domain - 核心业务规则单测
 * 覆盖：
 * 1) 作废数据过滤（status=VOID 不计入统计）
 * 2) 收入按计费周期归属，非创建时间
 * 3) 房间状态统计（空置/已出租/即将到期）
 * 4) 盈利=收入-支出 计算正确
 */
class StatisticsDomainServiceTest {

    private StatisticsDomainService service;
    private ContractRepository contractRepository;
    private StatisticsRepository statisticsRepository;

    private static final String LANDLORD = "LL-001";

    @BeforeEach
    void setUp() {
        contractRepository = mock(ContractRepository.class);
        statisticsRepository = mock(StatisticsRepository.class);
        service = new StatisticsDomainService(contractRepository, statisticsRepository);
    }

    @Test
    @DisplayName("STA-001: 账单按计费周期计入且作废账单被过滤")
    void aggregateDashboard_shouldExcludeVoidAndMatchPeriod() {
        LocalDate juneStart = LocalDate.of(2026, 6, 1);
        LocalDate juneEnd = LocalDate.of(2026, 6, 30);

        // 待支付租金账单
        IncomeInvoice pendingRent = buildIncome("INV1", FeeType.RENT, InvoiceStatus.PENDING,
                new BigDecimal("2000.00"), juneStart, juneEnd);
        // 逾期账单
        IncomeInvoice overdue = buildIncome("INV2", FeeType.RENT, InvoiceStatus.OVERDUE,
                new BigDecimal("1500.00"), juneStart.minusMonths(1), juneEnd.minusMonths(1));
        // 作废账单（应被过滤）
        IncomeInvoice voided = buildIncome("INV3", FeeType.OTHER, InvoiceStatus.VOID,
                new BigDecimal("9999.00"), juneStart, juneEnd);
        // 押金账单（周期为空）
        IncomeInvoice deposit = buildIncome("INV4", FeeType.DEPOSIT, InvoiceStatus.PAID,
                new BigDecimal("3000.00"), null, null);

        when(statisticsRepository.findIncomeInvoicesByLandlordAndPeriod(eq(LANDLORD),
                any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(pendingRent, overdue, voided, deposit));

        // 支出账单
        ExpenseInvoice repair = ExpenseInvoice.builder()
                .expenseId("EXP1").amount(new BigDecimal("500.00")).build();
        when(statisticsRepository.sumExpenseInvoiceAmountByLandlordAndPeriod(
                eq(LANDLORD), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("500.00"));

        // 房间（2 已出租, 1 空置）
        Room occupied1 = Room.builder().roomId("R1").roomName("房A")
                .status(RoomStatus.OCCUPIED).build();
        Room occupied2 = Room.builder().roomId("R2").roomName("房B")
                .status(RoomStatus.OCCUPIED).build();
        Room vacant = Room.builder().roomId("R3").roomName("房C")
                .status(RoomStatus.VACANT).build();
        when(statisticsRepository.findRoomsByLandlord(eq(LANDLORD)))
                .thenReturn(List.of(occupied1, occupied2, vacant));

        // 即将到期合约（租约剩余 <= 30 天）
        Contract soon = Contract.builder().contractId("C1")
                .startDate(LocalDate.now().minusMonths(11))
                .endDate(LocalDate.now().plusDays(15))
                .build();
        when(contractRepository.findByLandlordMemberIdAndStatusIn(eq(LANDLORD), anyList()))
                .thenReturn(List.of(soon));

        when(statisticsRepository.sumEffectiveDepositByLandlord(eq(LANDLORD)))
                .thenReturn(new BigDecimal("3000.00"));

        DashboardStatistics result = service.aggregateDashboardData(
                LANDLORD, TimeRange.THIS_MONTH, null, null);

        assertEquals(1L, result.getPendingBills().getCount());
        assertEquals(1L, result.getOverdueBills().getCount());

        // 总收入 = 2000 + 1500 + 3000(押金)，不包含作废的9999
        assertEquals(new BigDecimal("3000.00"), result.getEffectiveDepositAmount());

        // 3 房间（2已出租+1空置）
        assertEquals(3L, result.getRooms().getTotal());
        assertEquals(1L, result.getRooms().getVacant());
        assertEquals(2L, result.getRooms().getOccupied());
        assertEquals(1L, result.getRooms().getExpiringSoon());

        // 支出500，收入总额包含了非作废账单金额
        assertEquals(new BigDecimal("500.00"), result.getProfit().getExpense());
        // 收入 = pending(2000) + overdue(1500) + deposit(3000) = 6500，排除作废(9999)
        assertEquals(new BigDecimal("6500.00"), result.getProfit().getIncome());
        // 利润 = 6500 - 500 = 6000
        assertEquals(new BigDecimal("6000.00"), result.getProfit().getProfit());
    }

    @Test
    @DisplayName("STA-002: 单房源盈利详情，profit = 收入 - 支出")
    void calculateProfit_incomeMinusExpense() {
        LocalDate may = LocalDate.of(2026, 5, 1);
        LocalDate mayEnd = LocalDate.of(2026, 5, 31);

        IncomeInvoice rent = buildIncome("INV-1", FeeType.RENT, InvoiceStatus.PAID,
                new BigDecimal("2000"), may, mayEnd);
        rent.setRoomId("R1");

        ExpenseInvoice repair = ExpenseInvoice.builder()
                .expenseId("EXP-1")
                .costType(CostType.REPAIR)
                .amount(new BigDecimal("300"))
                .costDate(may.plusDays(5))
                .remark("水龙头维修")
                .build();

        when(statisticsRepository.findIncomeInvoicesByHouseSourceAndPeriod(eq("HS-1"),
                any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(rent));
        when(statisticsRepository.findExpenseInvoicesByHouseSourceAndPeriod(eq("HS-1"),
                eq(LANDLORD), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(repair));
        when(statisticsRepository.findRoomNameByRoomId(eq("R1"))).thenReturn("整套");

        ProfitDetail detail = service.calculateProfit("HS-1", LANDLORD,
                TimeRange.THIS_MONTH, null, null);

        assertEquals(new BigDecimal("2000"), detail.getIncomeTotal());
        assertEquals(new BigDecimal("300"), detail.getExpenseTotal());
        assertEquals(new BigDecimal("1700"), detail.getProfit());
    }

    @Test
    @DisplayName("STA-003: 收入按计费周期筛选，而非创建时间")
    void filterByPeriod_shouldUseCycleRangeNotCreateTime() {
        LocalDate from = LocalDate.of(2026, 5, 1);
        LocalDate to = LocalDate.of(2026, 5, 31);

        // 5月份周期内的账单
        IncomeInvoice mayBill = buildIncome("INV-1", FeeType.RENT, InvoiceStatus.PAID,
                new BigDecimal("2000"),
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31));
        // 6月份账单，不在筛选范围内
        IncomeInvoice juneBill = buildIncome("INV-2", FeeType.RENT, InvoiceStatus.PENDING,
                new BigDecimal("2500"),
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 30));

        List<IncomeInvoice> mayOnly = service.filterByPeriod(List.of(mayBill, juneBill), from, to);
        assertEquals(1, mayOnly.size());
        assertEquals("INV-1", mayOnly.get(0).getInvoiceId());
    }

    @Test
    @DisplayName("STA-004: 作废数据全局过滤")
    void aggregateDashboard_voidInvoicesShouldBeExcluded() {
        IncomeInvoice normal = buildIncome("INV-1", FeeType.RENT, InvoiceStatus.PENDING,
                new BigDecimal("1500"), LocalDate.now().withDayOfMonth(1), LocalDate.now());
        IncomeInvoice voided = buildIncome("INV-2", FeeType.OTHER, InvoiceStatus.VOID,
                new BigDecimal("99999"), LocalDate.now().withDayOfMonth(1), LocalDate.now());

        when(statisticsRepository.findIncomeInvoicesByLandlordAndPeriod(eq(LANDLORD),
                any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(normal, voided));

        DashboardStatistics result = service.aggregateDashboardData(
                LANDLORD, TimeRange.THIS_MONTH, null, null);

        assertEquals(1L, result.getPendingBills().getCount());
        assertEquals(new BigDecimal("1500.00"), result.getPendingBills().getAmount());
    }

    private IncomeInvoice buildIncome(String id, FeeType feeType, InvoiceStatus status,
                                      BigDecimal amount, LocalDate start, LocalDate end) {
        IncomeInvoice inv = IncomeInvoice.builder()
                .invoiceId(id)
                .feeType(feeType)
                .amount(amount)
                .cycleStart(start)
                .cycleEnd(end)
                .status(status)
                .build();
        inv.initialize();
        // 重新覆盖状态，因为 initialize 会重置为 PENDING
        return inv;
    }
}
