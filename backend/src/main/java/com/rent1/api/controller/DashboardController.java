package com.rent1.api.controller;

import com.rent1.application.service.StatisticsAppService;
import com.rent1.domain.common.BusinessException;
import com.rent1.common.enums.ErrorCode;
import com.rent1.common.response.ApiResponse;
import com.rent1.api.dto.statistics.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 房东首页看板/统计数据接口
 * 权限：仅LANDLORD / ADMIN 角色可访问。
 */
@Slf4j
@RestController
@RequestMapping("/landlord")
@RequiredArgsConstructor
public class DashboardController {

    private static final String ATTR_MEMBER_ID = "memberId";

    private final StatisticsAppService statisticsAppService;

    /**
     * 房东首页看板数据聚合
     *
     * @param timeRange 时间范围枚举：TODAY / THIS_MONTH / THIS_QUARTER / THIS_YEAR / CUSTOM
     * @param startDate 自定义起始日期（timeRange=CUSTOM时必填）
     * @param endDate   自定义结束日期
     */
    @GetMapping("/dashboard")
    public ApiResponse<DashboardResponse> getDashboard(
            @RequestParam(required = false, defaultValue = "THIS_MONTH") String timeRange,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletRequest request) {

        String landlordMemberId = (String) request.getAttribute(ATTR_MEMBER_ID);
        if (landlordMemberId == null || landlordMemberId.isBlank()) {
            throw new BusinessException(ErrorCode.A001);
        }

        var result = statisticsAppService.getLandlordDashboard(landlordMemberId, timeRange, startDate, endDate);
        return ApiResponse.success(toResponse(result));
    }

    /**
     * 单房源盈利详情
     */
    @GetMapping("/house-sources/{houseSourceId}/profit-detail")
    public ApiResponse<ProfitDetailResponse> getProfitDetail(
            @PathVariable String houseSourceId,
            @RequestParam(required = false, defaultValue = "THIS_MONTH") String timeRange,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletRequest request) {

        String landlordMemberId = (String) request.getAttribute(ATTR_MEMBER_ID);
        if (landlordMemberId == null || landlordMemberId.isBlank()) {
            throw new BusinessException(ErrorCode.A001);
        }

        var detail = statisticsAppService.getHouseSourceProfitDetail(
                landlordMemberId, houseSourceId, timeRange, startDate, endDate);
        return ApiResponse.success(toResponse(detail));
    }

    private DashboardResponse toResponse(com.rent1.domain.statistics.DashboardStatistics s) {
        var pending = BillSummaryResponse.builder()
                .count(s.getPendingBills().getCount())
                .amount(s.getPendingBills().getAmount() == null ? BigDecimal.ZERO : s.getPendingBills().getAmount())
                .build();
        var overdue = BillSummaryResponse.builder()
                .count(s.getOverdueBills().getCount())
                .amount(s.getOverdueBills().getAmount() == null ? BigDecimal.ZERO : s.getOverdueBills().getAmount())
                .build();
        var rooms = RoomSummaryResponse.builder()
                .total(s.getRooms().getTotal())
                .vacant(s.getRooms().getVacant())
                .occupied(s.getRooms().getOccupied())
                .expiringSoon(s.getRooms().getExpiringSoon())
                .build();
        var profit = ProfitSummaryResponse.builder()
                .income(s.getProfit().getIncome() == null ? BigDecimal.ZERO : s.getProfit().getIncome())
                .expense(s.getProfit().getExpense() == null ? BigDecimal.ZERO : s.getProfit().getExpense())
                .profit(s.getProfit().getProfit() == null ? BigDecimal.ZERO : s.getProfit().getProfit())
                .build();
        return DashboardResponse.builder()
                .pendingBills(pending)
                .overdueBills(overdue)
                .effectiveDepositAmount(s.getEffectiveDepositAmount())
                .rooms(rooms)
                .profit(profit)
                .timeRange(s.getTimeRange())
                .build();
    }

    private ProfitDetailResponse toResponse(com.rent1.domain.statistics.ProfitDetail d) {
        List<ProfitDetailResponse.IncomeItem> incomes = d.getIncomeList().stream()
                .map(i -> ProfitDetailResponse.IncomeItem.builder()
                        .invoiceId(i.getInvoiceId())
                        .feeTypeCode(i.getFeeTypeCode())
                        .feeTypeName(i.getFeeTypeName())
                        .roomId(i.getRoomId())
                        .roomName(i.getRoomName())
                        .cycleStart(i.getCycleStart())
                        .cycleEnd(i.getCycleEnd())
                        .amount(i.getAmount())
                        .isManual(i.getIsManual())
                        .status(i.getStatus())
                        .build())
                .collect(Collectors.toList());
        List<ProfitDetailResponse.ExpenseItem> expenses = d.getExpenseList().stream()
                .map(e -> ProfitDetailResponse.ExpenseItem.builder()
                        .expenseId(e.getExpenseId())
                        .costTypeCode(e.getCostTypeCode())
                        .costTypeName(e.getCostTypeName())
                        .costDate(e.getCostDate())
                        .amount(e.getAmount())
                        .remark(e.getRemark())
                        .build())
                .collect(Collectors.toList());
        return ProfitDetailResponse.builder()
                .incomeTotal(d.getIncomeTotal())
                .expenseTotal(d.getExpenseTotal())
                .profit(d.getProfit())
                .incomeList(incomes)
                .expenseList(expenses)
                .build();
    }
}
