package com.rent1.domain.property.valueobject;

import lombok.Value;
import java.time.LocalDate;

/**
 * 时间周期值对象
 * 
 * 用于封装租期、计费周期等场景
 * 保证周期不可变，避免时间计算错误
 */
@Value
public class Period {
    
    /**
     * 开始日期
     */
    LocalDate startDate;
    
    /**
     * 结束日期
     */
    LocalDate endDate;
    
    /**
     * 创建周期
     */
    public static Period of(LocalDate startDate, LocalDate endDate) {
        // 校验：结束时间不能早于开始时间
        if (endDate != null && startDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("结束时间不能早于开始时间");
        }
        return new Period(startDate, endDate);
    }
    
    /**
     * 判断是否有效（结束时间不早于开始时间）
     */
    public boolean isValid() {
        if (startDate == null || endDate == null) {
            return false;
        }
        return !endDate.isBefore(startDate);
    }
    
    /**
     * 计算周期天数
     */
    public long days() {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return endDate.toEpochDay() - startDate.toEpochDay() + 1;
    }
    
    /**
     * 计算周期月数（近似）
     */
    public long months() {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return days() / 30;
    }
    
    /**
     * 判断指定日期是否在周期内
     */
    public boolean contains(LocalDate date) {
        if (startDate == null || endDate == null || date == null) {
            return false;
        }
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }
    
    /**
     * 判断是否已过期（结束日期早于当前日期）
     */
    public boolean isExpired(LocalDate currentDate) {
        if (endDate == null) {
            return false;
        }
        return endDate.isBefore(currentDate);
    }
    
    /**
     * 计算剩余天数
     */
    public long remainingDays(LocalDate currentDate) {
        if (endDate == null) {
            return 0;
        }
        if (currentDate.isAfter(endDate)) {
            return 0;
        }
        return endDate.toEpochDay() - currentDate.toEpochDay();
    }
    
    /**
     * 判断是否即将到期（剩余≤30天）
     */
    public boolean isExpiringSoon(LocalDate currentDate) {
        return remainingDays(currentDate) <= 30 && remainingDays(currentDate) > 0;
    }
}