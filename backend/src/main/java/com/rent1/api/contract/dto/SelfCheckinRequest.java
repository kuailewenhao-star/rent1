package com.rent1.api.contract.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 租客自助入驻请求DTO
 */
@Data
public class SelfCheckinRequest {

    /** 邀请码 */
    @NotBlank(message = "邀请码不能为空")
    private String inviteCode;

    /** 租客手机号 */
    @NotBlank(message = "租客手机号不能为空")
    private String tenantPhone;

    /** 真实姓名 */
    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    /** 身份证号 */
    @NotBlank(message = "身份证号不能为空")
    private String idCard;

    /** 是否确认入驻 */
    @NotNull(message = "确认状态不能为空")
    private Boolean confirmed;
}
