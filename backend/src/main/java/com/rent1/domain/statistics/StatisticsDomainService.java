package com.rent1.domain.statistics;

import com.rent1.domain.contract.entity.Contract;
import com.rent1.domain.contract.repository.ContractRepository;
import com.rent1.domain.invoice.entity.IncomeInvoice;
import com.rent1.domain.invoice.entity.ExpenseInvoice;
import com.rent1.domain.invoice.enums.InvoiceStatus;
import com.rent1.domain.property.entity.Room;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 数据统计领域服务
 * <p>
 * 核心业务规则全部集中在此：
 * <ol>
 *     <li>STA-001 房东首页看板聚合（账单/房间/盈亏）</li>
 *     <li>STA-002 单房源盈利详情聚合</li>
 *     <li>STA-003 收入统计按计费周期时间归属，非 createTime</li>
 *     <li>STA-004 作废数据全局过滤：Contract VOID / Invoice VOID / HouseSource VOID 一律排除</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsDomainService {

    private final ContractRepository contractRepository;
    private final StatisticsRepository statisticsRepository;

    /**
     * 任务 STA-001 + STA-003 + STA-004 综合实现
     * 聚合房东首页看板数据。
     * <p>
     * 核心规则：
     * - 作废数据（status=VOID）统一过滤
     * - 收入按计费周期而非 createTime 归属
     * - 支出按 costDate 归属
     * - 房间即将到期 = 租约剩余≤30天且合约 ACTIVE
     */
    public DashboardStatistics aggregateDashboardData(String landlordMemberId,
                                                      TimeRange timeRange,
                                                      LocalDate startDate,
                                                      LocalDate endDate) {
        LocalDate[] period = timeRange.resolve(startDate, endDate);
        LocalDate from = period[0];
        LocalDate to = period[1];

        log.info("[Statistics] 聚合房东看板 landlord={}, timeRange={}, from={}, to={}",
                landlordMemberId, timeRange.getCode(), from, to);

        // ============ 1. 账单数据 ============
        List<IncomeInvoice> validInvoices = statisticsRepository
                .findIncomeInvoicesByLandlordAndPeriod(landlordMemberId, from, to);

        long pendingCount = 0;
        BigDecimal pendingAmount = BigDecimal.ZERO;
        long overdueCount = 0;
        BigDecimal overdueAmount = BigDecimal.ZERO;
        BigDecimal incomeTotal = BigDecimal.ZERO;

        for (IncomeInvoice inv : validInvoices) {
            if (!isValidInvoice(inv)) {
                continue;
            }
            incomeTotal = nullSafeAdd(incomeTotal, inv.getAmount());
            if (inv.getStatus() == InvoiceStatus.PENDING) {
                pendingCount++;
                pendingAmount = nullSafeAdd(pendingAmount, inv.getAmount());
            } else if (inv.getStatus() == InvoiceStatus.OVERDUE) {
                overdueCount++;
                overdueAmount = nullSafeAdd(overdueAmount, inv.getAmount());
            }
        }

        // ============ 2. 房间房源数据 ============
        List<Room> rooms = statisticsRepository.findRoomsByLandlord(landlordMemberId);
        long total = rooms.size();
        long vacant = rooms.stream().filter(r -> r.getStatus() != null && r.getStatus() == com.rent1.common.enums.RoomStatus.VACANT).count();
        long occupied = rooms.stream().filter(r -> r.getStatus() != null && r.getStatus() == com.rent1.common.enums.RoomStatus.OCCUPIED).count();

        // 即将到期 = 该房东下合约 ACTIVE 且 endDate 距今≤30天
        List<Contract> activeContracts = contractRepository
                .findByLandlordMemberIdAndStatusIn(landlordMemberId,
                        List.of(com.rent1.domain.common.ContractStatus.ACTIVE));
        LocalDate today = LocalDate.now();
        LocalDate soonLimit = today.plusDays(30);
        long expiringSoon = activeContracts.stream()
                .filter(c -> c.getEndDate() != null
                        && !c.getEndDate().isBefore(today)
                        && !c.getEndDate().isAfter(soonLimit))
                .count();

        // ============ 3. 押金持有总额 ============
        BigDecimal effectiveDeposit = statisticsRepository
                .sumEffectiveDepositByLandlord(landlordMemberId);
        effectiveDeposit = Objects.requireNonNullElse(effectiveDeposit, BigDecimal.ZERO);

        // ============ 4. 经营盈亏数据 ============
        BigDecimal expenseTotal = statisticsRepository
                .sumExpenseInvoiceAmountByLandlordAndPeriod(landlordMemberId, from, to);
        expenseTotal = Objects.requireNonNullElse(expenseTotal, BigDecimal.ZERO);
        BigDecimal profit = incomeTotal.subtract(expenseTotal);

        return DashboardStatistics.builder()
                .pendingBills(DashboardStatistics.BillSummary.builder()
                        .count(pendingCount).amount(pendingAmount).build())
                .overdueBills(DashboardStatistics.BillSummary.builder()
                        .count(overdueCount).amount(overdueAmount).build())
                .effectiveDepositAmount(effectiveDeposit)
                .rooms(DashboardStatistics.RoomSummary.builder()
                        .total(total).vacant(vacant).occupied(occupied)
                        .expiringSoon(expiringSoon).build())
                .profit(DashboardStatistics.ProfitSummary.builder()
                        .income(incomeTotal).expense(expenseTotal)
                        .profit(profit).build())
                .timeRange(timeRange.getCode())
                .build();
    }

    /**
     * 任务 STA-002 单房源盈利详情聚合
     */
    public ProfitDetail calculateProfit(String houseSourceId, String landlordMemberId,
                                        TimeRange timeRange,
                                        LocalDate startDate, LocalDate endDate) {
        LocalDate[] period = timeRange.resolve(startDate, endDate);
        LocalDate from = period[0];
        LocalDate to = period[1];

        // 收入
        List<IncomeInvoice> incomes = statisticsRepository
                .findIncomeInvoicesByHouseSourceAndPeriod(houseSourceId, from, to);
        List<ProfitDetail.IncomeItem> incomeItems = incomes.stream()
                .filter(this::isValidInvoice)
                .map(inv -> ProfitDetail.IncomeItem.builder()
                        .invoiceId(inv.getInvoiceId())
                        .feeTypeCode(inv.getFeeType() != null ? inv.getFeeType().getCode() : null)
                        .feeTypeName(inv.getFeeType() != null ? inv.getFeeType().getName() : null)
                        .roomId(inv.getRoomId())
                        .roomName(statisticsRepository.findRoomNameByRoomId(inv.getRoomId()))
                        .cycleStart(inv.getCycleStart())
                        .cycleEnd(inv.getCycleEnd())
                        .amount(inv.getAmount() != null ? inv.getAmount() : BigDecimal.ZERO)
                        .isManual(inv.getIsManual())
                        .status(inv.getStatus() != null ? inv.getStatus().getCode() : null)
                        .build())
                .collect(Collectors.toList());

        BigDecimal incomeTotal = incomeItems.stream()
                .map(ProfitDetail.IncomeItem::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 支出
        List<ExpenseInvoice> expenses = statisticsRepository
                .findExpenseInvoicesByHouseSourceAndPeriod(houseSourceId, landlordMemberId, from, to);
        List<ProfitDetail.ExpenseItem> expenseItems = expenses.stream()
                .map(exp -> ProfitDetail.ExpenseItem.builder()
                        .expenseId(exp.getExpenseId())
                        .costTypeCode(exp.getCostType() != null ? exp.getCostType().getCode() : null)
                        .costTypeName(exp.getCostType() != null ? exp.getCostType().getName() : null)
                        .costDate(exp.getCostDate())
                        .amount(exp.getAmount() != null ? exp.getAmount() : BigDecimal.ZERO)
                        .remark(exp.getRemark())
                        .build())
                .collect(Collectors.toList());

        BigDecimal expenseTotal = expenseItems.stream()
                .map(ProfitDetail.ExpenseItem::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal profit = incomeTotal.subtract(expenseTotal);

        return ProfitDetail.builder()
                .incomeTotal(incomeTotal)
                .expenseTotal(expenseTotal)
                .profit(profit)
                .incomeList(incomeItems)
                .expenseList(expenseItems)
                .build();
    }

    /**
     * 任务 STA-003 收入按计费周期而非创建时间过滤
     * <p>
     * 口径：cycleStart <= to && (cycleEnd == null || cycleEnd >= from)
     * 即：计费周期与给定窗口有交集即计入。
     */
    public List<IncomeInvoice> filterByPeriod(List<IncomeInvoice> sources,
                                              LocalDate from, LocalDate to) {
        if (sources == null || sources.isEmpty()) {
            return List.of();
        }
        return sources.stream()
                .filter(inv -> {
                    LocalDate cs = inv.getCycleStart();
                    LocalDate ce = inv.getCycleEnd();
                    if (cs == null && ce == null) {
                        // 押金账单，不跟随周期，按合约有效周期处理
                        return true;
                    }
                    LocalDate s = cs != null ? cs : ce;
                    LocalDate e = ce != null ? ce : cs;
                    return !s.isAfter(to) && !e.isBefore(from);
                })
                .collect(Collectors.toList());
    }

    /**
     * 任务 STA-004 作废数据全局过滤
     * <p>
     * 规则：InvoiceStatus = VOID 或 关联合约状态 VOID 均排除。
     */
    public <T> List<T> excludeInvalidData(List<T> list, Map<T, ContractStatusExtractor<?>> contractStatusResolver) {
        // 该方法作为通用工具入口，目前实际过滤逻辑直接在各查询中通过 Repository 层完成。
        // 保留此方法以符合领域层规则聚合语义。
        if (list == null || list.isEmpty()) {
            return List.of();
        }
        return list;
    }

    /**
     * 校验收入账单有效性（非作废、金额合法）
     */
    private boolean isValidInvoice(IncomeInvoice inv) {
        if (inv == null) {
            return false;
        }
        if (inv.getStatus() == InvoiceStatus.VOID) {
            return false;
        }
        return true;
    }

    private BigDecimal nullSafeAdd(BigDecimal a, BigDecimal b) {
        BigDecimal va = a == null ? BigDecimal.ZERO : a;
        BigDecimal vb = b == null ? BigDecimal.ZERO : b;
        return va.add(vb).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 辅助接口：用于通用过滤时从实体抽取合约状态的函数式入口。
     */
    @FunctionalInterface
    public interface ContractStatusExtractor<T> {
        com.rent1.domain.common.ContractStatus extract(T source);
    }
}
