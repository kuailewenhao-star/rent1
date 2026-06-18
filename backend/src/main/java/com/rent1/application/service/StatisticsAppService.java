package com.rent1.application.service;

import com.rent1.domain.statistics.DashboardStatistics;
import com.rent1.domain.statistics.ProfitDetail;
import com.rent1.domain.statistics.StatisticsDomainService;
import com.rent1.domain.statistics.TimeRange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * 数据统计应用服务（Application Service）
 * 职责：跨领域调度、事务控制、参数组装、给接入层提供清晰接口。
 * <p>
 * 核心业务规则由 StatisticsDomainService 定义。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsAppService {

    private final StatisticsDomainService statisticsDomainService;

    /**
     * 获取房东首页看板数据
     *
     * @param landlordMemberId 房东会员ID
     * @param timeRange        时间范围枚举（TODAY/THIS_MONTH/THIS_QUARTER/THIS_YEAR/CUSTOM）
     * @param startDate        自定义起始日期（timeRange=CUSTOM时必填）
     * @param endDate          自定义结束日期
     * @return 看板聚合数据
     */
    public DashboardStatistics getLandlordDashboard(String landlordMemberId,
                                                     String timeRange,
                                                     LocalDate startDate,
                                                     LocalDate endDate) {
        TimeRange range = TimeRange.fromCode(timeRange);
        return statisticsDomainService.aggregateDashboardData(
                landlordMemberId, range, startDate, endDate);
    }

    /**
     * 获取单房源盈利详情
     */
    public ProfitDetail getHouseSourceProfitDetail(String landlordMemberId,
                                                    String houseSourceId,
                                                    String timeRange,
                                                    LocalDate startDate,
                                                    LocalDate endDate) {
        TimeRange range = TimeRange.fromCode(timeRange);
        return statisticsDomainService.calculateProfit(
                houseSourceId, landlordMemberId, range, startDate, endDate);
    }
}
