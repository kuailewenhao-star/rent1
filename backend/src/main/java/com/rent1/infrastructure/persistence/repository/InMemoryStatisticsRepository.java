package com.rent1.infrastructure.persistence.repository;

import com.rent1.domain.deposit.DepositRepository;
import com.rent1.domain.invoice.entity.ExpenseInvoice;
import com.rent1.domain.invoice.entity.IncomeInvoice;
import com.rent1.domain.invoice.repository.IncomeInvoiceRepository;
import com.rent1.domain.invoice.repository.ExpenseInvoiceRepository;
import com.rent1.domain.property.entity.Room;
import com.rent1.domain.property.repository.RoomRepository;
import com.rent1.domain.statistics.StatisticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 数据统计仓储实现（内存实现，简化版）。
 * <p>
 * 核心规则：作废数据（status=VOID）不参与任何统计；收入按计费周期归属。
 */
@Repository
@RequiredArgsConstructor
public class InMemoryStatisticsRepository implements StatisticsRepository {

    private final IncomeInvoiceRepository incomeInvoiceRepository;
    private final ExpenseInvoiceRepository expenseInvoiceRepository;
    private final RoomRepository roomRepository;
    private final DepositRepository depositRepository;

    @Override
    public List<IncomeInvoice> findIncomeInvoicesByLandlordAndPeriod(String landlordMemberId,
                                                                     LocalDate from, LocalDate to) {
        return incomeInvoiceRepository
                .findByLandlordMemberIdAndCycleIntersect(landlordMemberId, from, to);
    }

    @Override
    public List<Room> findRoomsByLandlord(String landlordMemberId) {
        return roomRepository.findByLandlordId(landlordMemberId);
    }

    @Override
    public String findRoomNameByRoomId(String roomId) {
        if (roomId == null) {
            return null;
        }
        Room room = roomRepository.findByIdDirect(roomId);
        return room == null ? null : room.getRoomName();
    }

    @Override
    public BigDecimal sumExpenseInvoiceAmountByLandlordAndPeriod(String landlordMemberId,
                                                                  LocalDate from, LocalDate to) {
        return expenseInvoiceRepository.sumAmountByLandlordMemberIdAndCostDateBetween(
                landlordMemberId, from, to);
    }

    @Override
    public BigDecimal sumEffectiveDepositByLandlord(String landlordMemberId) {
        BigDecimal depositTotal = depositRepository.sumValidDepositByLandlord(landlordMemberId);
        return Objects.requireNonNullElseGet(depositTotal, () -> BigDecimal.ZERO);
    }

    @Override
    public List<IncomeInvoice> findIncomeInvoicesByHouseSourceAndPeriod(String houseSourceId,
                                                                        LocalDate from, LocalDate to) {
        return incomeInvoiceRepository.findByHouseSourceId(houseSourceId).stream()
                .filter(inv -> matchesPeriod(inv, from, to))
                .collect(Collectors.toList());
    }

    @Override
    public List<ExpenseInvoice> findExpenseInvoicesByHouseSourceAndPeriod(String houseSourceId,
                                                                          String landlordMemberId,
                                                                          LocalDate from, LocalDate to) {
        return expenseInvoiceRepository.findByHouseSourceIdAndCostDateBetween(
                houseSourceId, landlordMemberId, from, to);
    }

    /**
     * 周期匹配：账单计费周期与 [from, to] 有交集即匹配。
     */
    private boolean matchesPeriod(IncomeInvoice inv, LocalDate from, LocalDate to) {
        LocalDate start = inv.getCycleStart();
        LocalDate end = inv.getCycleEnd();
        if (start == null && end == null) {
            // 押金账单，无条件包含
            return true;
        }
        LocalDate s = start != null ? start : end;
        LocalDate e = end != null ? end : start;
        return !s.isAfter(to) && !e.isBefore(from);
    }
}
