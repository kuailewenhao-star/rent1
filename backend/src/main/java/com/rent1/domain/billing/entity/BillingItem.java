package com.rent1.domain.billing.entity;

import com.rent1.domain.billing.ChargeType;
import com.rent1.domain.billing.ChargeCycle;
import com.rent1.domain.billing.FeeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 计费项值对象 - 单项费用配置
 * 承载单类费用的计费规则（类型、方式、金额/比例、周期、备注）
 *
 * 核心业务规则：
 * 1. 金额范围 0 ~ 999999.99（fixed模式）
 * 2. 比例范围 0 ~ 1.0（ratio模式）
 * 3. 租金/押金为系统固定项，不可删除
 * 4. 同房间同FeeType仅允许1条配置
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingItem {

    /** 费用类型（RENT/DEPOSIT/WATER等9类） */
    private FeeType feeType;

    /** 费用类型名称，便于前端展示 */
    private String feeTypeName;

    /** 计费方式：fixed固定值 / ratio比例分摊 */
    private ChargeType chargeType;

    /** 计费值：fixed模式为金额（元），ratio模式为分摊比例（0~1.0） */
    private BigDecimal chargeValue;

    /** 计费周期：每月/每季度/每半年/每年（押金DEPOSIT为null，一次性） */
    private ChargeCycle chargeCycle;

    /** 备注说明，最长200字符 */
    private String remark;

    /** 是否系统固定项（租金/押金），true表示不可删除 */
    private boolean immutable;

    /**
     * 校验该计费项的基本业务规则
     * 由领域服务在增/改操作前统一调用
     */
    public boolean hasValidChargeValue() {
        if (chargeValue == null) {
            return false;
        }
        if (chargeValue.compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }
        // ratio模式：0 ~ 1.0
        if (chargeType == ChargeType.RATIO) {
            return chargeValue.compareTo(BigDecimal.ONE) <= 0;
        }
        // fixed模式：0 ~ 999999.99
        return chargeValue.compareTo(new BigDecimal("999999.99")) <= 0;
    }

    /**
     * 根据费用类型自动初始化 feeTypeName 和 immutable 标签
     */
    public void normalizeByFeeType() {
        if (this.feeType != null) {
            this.feeTypeName = this.feeType.getName();
            this.immutable = this.feeType.isSystemFixed();
        }
    }
}
