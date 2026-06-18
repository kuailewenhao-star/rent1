package com.rent1.api.dto.billing;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 新增计费规则项 - 请求DTO
 * 对应接口 POST /api/rooms/{roomId}/billing-rules
 */
@Data
public class AddBillingItemRequest {

    /**
     * 费用类型枚举：RENT/DEPOSIT/WATER/ELECTRIC/GAS/BROADBAND/PROPERTY/TRASH/OTHER
     */
    @NotBlank(message = "费用类型不能为空")
    private String feeType;

    /**
     * 计费方式：fixed固定值 / ratio比例分摊
     */
    @NotBlank(message = "计费方式不能为空")
    private String chargeType;

    /**
     * 金额值：fixed模式为金额(0~999999.99)，ratio模式为分摊比例(0~1.0)
     */
    @NotNull(message = "计费值不能为空")
    @PositiveOrZero(message = "计费值必须≥0")
    private BigDecimal chargeValue;

    /**
     * 计费周期：MONTHLY每月/QUARTERLY每三月/HALF_YEARLY每半年/YEARLY每年
     * 押金DEPOSIT无周期，此字段可为空
     */
    private String chargeCycle;

    /**
     * 备注
     */
    @Size(max = 200, message = "备注最多200字符")
    private String remark;
}
