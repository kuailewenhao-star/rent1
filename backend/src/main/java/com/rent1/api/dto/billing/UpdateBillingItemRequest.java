package com.rent1.api.dto.billing;

import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 编辑计费规则项 - 请求DTO
 * 对应接口 PUT /api/rooms/{roomId}/billing-rules/{feeType}
 * 仅可更新：计费方式、金额、周期、备注（feeType不可变）
 */
@Data
public class UpdateBillingItemRequest {

    /** 计费方式：fixed/ratio */
    private String chargeType;

    /** 计费值 */
    @PositiveOrZero(message = "计费值必须≥0")
    private BigDecimal chargeValue;

    /** 计费周期 */
    private String chargeCycle;

    /** 备注 */
    @Size(max = 200, message = "备注最多200字符")
    private String remark;
}
