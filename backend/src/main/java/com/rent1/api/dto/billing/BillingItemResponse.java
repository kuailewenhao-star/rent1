package com.rent1.api.dto.billing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 单项计费规则响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingItemResponse {

    /** 费用类型 */
    private String feeType;

    /** 费用类型中文名，便于前端展示 */
    private String feeTypeName;

    /** 计费方式：fixed/ratio */
    private String chargeType;

    /** 金额或比例值 */
    private BigDecimal chargeValue;

    /** 计费周期 */
    private String chargeCycle;

    /** 备注 */
    private String remark;

    /** 是否系统固定项（租金/押金不可删除） */
    private boolean immutable;
}
