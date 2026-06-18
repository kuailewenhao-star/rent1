package com.rent1.domain.property.valueobject;

import lombok.Value;
import java.math.BigDecimal;

/**
 * 金额值对象
 * 
 * 用于封装金额计算、盈亏核算等场景
 * 保证金额不可变，避免计算错误
 */
@Value
public class Money {
    
    /**
     * 金额数值
     */
    BigDecimal amount;
    
    /**
     * 货币单位（默认CNY）
     */
    String currency;
    
    /**
     * 创建人民币金额
     */
    public static Money ofCNY(BigDecimal amount) {
        return new Money(amount, "CNY");
    }
    
    /**
     * 创建人民币金额（从字符串）
     */
    public static Money ofCNY(String amount) {
        return new Money(new BigDecimal(amount), "CNY");
    }
    
    /**
     * 创建零金额
     */
    public static Money zero() {
        return new Money(BigDecimal.ZERO, "CNY");
    }
    
    /**
     * 加法
     */
    public Money add(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("Currency mismatch");
        }
        return new Money(amount.add(other.amount), currency);
    }
    
    /**
     * 减法
     */
    public Money subtract(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("Currency mismatch");
        }
        return new Money(amount.subtract(other.amount), currency);
    }
    
    /**
     * 乘法（按比例）
     */
    public Money multiply(double ratio) {
        return new Money(amount.multiply(BigDecimal.valueOf(ratio)), currency);
    }
    
    /**
     * 判断是否为负数
     */
    public boolean isNegative() {
        return amount.compareTo(BigDecimal.ZERO) < 0;
    }
    
    /**
     * 判断是否为零
     */
    public boolean isZero() {
        return amount.compareTo(BigDecimal.ZERO) == 0;
    }
    
    /**
     * 判断是否为正数
     */
    public boolean isPositive() {
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * 比较大小
     */
    public boolean greaterThan(Money other) {
        return amount.compareTo(other.amount) > 0;
    }
    
    /**
     * 比较大小
     */
    public boolean lessThan(Money other) {
        return amount.compareTo(other.amount) < 0;
    }
}