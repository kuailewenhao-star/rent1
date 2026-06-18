package com.rent1.api.contract.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 房东创建合约请求DTO
 */
@Data
public class CreateContractRequest {

    /** 房间ID */
    @NotBlank(message = "房间ID不能为空")
    private String roomId;

    /** 租客手机号 */
    @NotBlank(message = "租客手机号不能为空")
    private String tenantPhone;

    /** 租客真实姓名（可选） */
    private String realName;

    /** 合约开始日期 */
    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;

    /** 合约结束日期 */
    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;

    /** 计费规则JSON */
    @NotBlank(message = "计费规则不能为空")
    private String billingRules;

    /** 纸质合约URL（可选） */
    private String paperContractUrl;
}
