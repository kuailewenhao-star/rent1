package com.rent1.domain.invoice.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;

/**
 * 计费规则项值对象 - 单费用项标准字段结构
 * 系统每一条收入费用项统一包含5个固定核心字段
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingRuleItem {

    /** 收入费用类型枚举值 */
    private String feeType;

    /** 计费方式：fixed / ratio */
    private String chargeType;

    /** 计费数值：固定值为具体金额、比例为小数比例（例：0.4=40%） */
    private BigDecimal chargeValue;

    /** 计费周期：MONTHLY/QUARTERLY/HALF_YEARLY/YEARLY（押金为空） */
    private String chargeCycle;

    /** 费用备注 */
    private String remark;

    /**
     * 获取费用类型名称
     */
    public String getFeeTypeName() {
        if ("RENT".equals(feeType)) return "租金";
        if ("DEPOSIT".equals(feeType)) return "押金";
        if ("WATER".equals(feeType)) return "水费";
        if ("ELECTRIC".equals(feeType)) return "电费";
        if ("GAS".equals(feeType)) return "燃气费";
        if ("BROADBAND".equals(feeType)) return "宽带费";
        if ("PROPERTY".equals(feeType)) return "物业费";
        if ("TRASH".equals(feeType)) return "垃圾清运费";
        if ("OTHER".equals(feeType)) return "其他杂费";
        return feeType;
    }

    /**
     * 校验是否为固定项（租金、押金不可删除）
     */
    public boolean isImmutable() {
        return "RENT".equals(feeType) || "DEPOSIT".equals(feeType);
    }

    /**
     * 校验是否为押金（押金无周期）
     */
    public boolean isDeposit() {
        return "DEPOSIT".equals(feeType);
    }

    /**
     * 校验金额范围
     */
    public boolean isValidAmount() {
        if (chargeValue == null) return false;
        if ("fixed".equals(chargeType)) {
            return chargeValue.compareTo(BigDecimal.ZERO) >= 0 
                && chargeValue.compareTo(new BigDecimal("999999.99")) <= 0;
        } else if ("ratio".equals(chargeType)) {
            return chargeValue.compareTo(BigDecimal.ZERO) >= 0 
                && chargeValue.compareTo(BigDecimal.ONE) <= 0;
        }
        return false;
    }
}