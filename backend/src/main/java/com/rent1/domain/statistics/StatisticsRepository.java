package com.rent1.domain.statistics;

import com.rent1.domain.invoice.entity.ExpenseInvoice;
import com.rent1.domain.invoice.entity.IncomeInvoice;
import com.rent1.domain.property.entity.Room;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 数据统计仓储接口
 * 专门服务于 StatisticsDomainService，用于聚合多表统计查询。
 * 遵循"全局作废数据过滤"规则，默认所有查询均排除 status=VOID。
 */
public interface StatisticsRepository {

    /**
     * 查询指定房东在计费周期范围内的收入账单（排除作废）。
     * 按计费周期 [cycleStart, cycleEnd] 与 [from, to] 有交集的账单，
     * 对应 STA-003 收入按周期归属的核心口径。
     */
    List<IncomeInvoice> findIncomeInvoicesByLandlordAndPeriod(String landlordMemberId,
                                                               LocalDate from, LocalDate to);

    /**
     * 查询指定房东所属所有房间（排除作废房源下的房间）。
     */
    List<Room> findRoomsByLandlord(String landlordMemberId);

    /**
     * 按房间ID获取房间名称，用于盈利详情列表。
     */
    String findRoomNameByRoomId(String roomId);

    /**
     * 查询指定房东在支出日期范围内的支出总额。
     */
    BigDecimal sumExpenseInvoiceAmountByLandlordAndPeriod(String landlordMemberId,
                                                          LocalDate from, LocalDate to);

    /**
     * 查询指定房东当前持有有效押金总额（履约中合约的押金账单/记录）。
     */
    BigDecimal sumEffectiveDepositByLandlord(String landlordMemberId);

    /**
     * 按房源查询收入账单明细。
     */
    List<IncomeInvoice> findIncomeInvoicesByHouseSourceAndPeriod(String houseSourceId,
                                                                 LocalDate from, LocalDate to);

    /**
     * 按房源+房东查询支出账单明细。
     */
    List<ExpenseInvoice> findExpenseInvoicesByHouseSourceAndPeriod(String houseSourceId,
                                                                    String landlordMemberId,
                                                                    LocalDate from, LocalDate to);
}
