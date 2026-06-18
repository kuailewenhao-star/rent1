package com.rent1.domain.statistics;

import lombok.Getter;

import java.time.LocalDate;

/**
 * 统计时间范围枚举
 * 统一数据统计的时间口径，前端只需传枚举值，领域层统一解析为起止日期。
 * <p>
 * 核心口径：
 * - TODAY / THIS_MONTH / THIS_QUARTER / THIS_YEAR 基于自然日/月/季/年
 * - CUSTOM 由调用方传入自定义 startDate / endDate
 * - 过期日 / 周期口径由计费规则独立约束（详见 StatisticsDomainService.filterByPeriod）
 */
@Getter
public enum TimeRange {

    /** 今日 */
    TODAY("TODAY", "今日"),
    /** 本月 */
    THIS_MONTH("THIS_MONTH", "本月"),
    /** 本季度 */
    THIS_QUARTER("THIS_QUARTER", "本季度"),
    /** 本年度 */
    THIS_YEAR("THIS_YEAR", "本年度"),
    /** 自定义 */
    CUSTOM("CUSTOM", "自定义");

    private final String code;
    private final String name;

    TimeRange(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 解析时间范围为【起始日期, 结束日期】闭区间。
     * 自定义模式下若未传起止日期，则默认返回本月。
     */
    public LocalDate[] resolve(LocalDate customStart, LocalDate customEnd) {
        LocalDate today = LocalDate.now();
        switch (this) {
            case TODAY:
                return new LocalDate[]{today, today};
            case THIS_MONTH:
                return new LocalDate[]{today.withDayOfMonth(1),
                        today.withDayOfMonth(today.lengthOfMonth())};
            case THIS_QUARTER:
                int quarterMonthStart = ((today.getMonthValue() - 1) / 3) * 3 + 1;
                LocalDate qStart = today.withMonth(quarterMonthStart).withDayOfMonth(1);
                LocalDate qEnd = qStart.plusMonths(3).minusDays(1);
                return new LocalDate[]{qStart, qEnd};
            case THIS_YEAR:
                return new LocalDate[]{today.withDayOfYear(1),
                        today.withDayOfYear(today.lengthOfYear())};
            case CUSTOM:
                if (customStart == null || customEnd == null) {
                    return new LocalDate[]{today.withDayOfMonth(1),
                            today.withDayOfMonth(today.lengthOfMonth())};
                }
                if (customStart.isAfter(customEnd)) {
                    return new LocalDate[]{customEnd, customStart};
                }
                return new LocalDate[]{customStart, customEnd};
            default:
                return new LocalDate[]{today.withDayOfMonth(1),
                        today.withDayOfMonth(today.lengthOfMonth())};
        }
    }

    public static TimeRange fromCode(String code) {
        if (code == null || code.isBlank()) {
            return THIS_MONTH;
        }
        for (TimeRange value : values()) {
            if (value.getCode().equalsIgnoreCase(code)) {
                return value;
            }
        }
        return THIS_MONTH;
    }
}
