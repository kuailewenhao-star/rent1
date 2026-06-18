package com.rent1.api.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 身份切换请求
 * MEM-003
 */
public class SwitchMemberTypeRequest {

    /** 目标会员类型：LANDLORD-房东 / TENANT-租客 */
    @NotBlank(message = "targetMemberType不能为空")
    @Pattern(regexp = "LANDLORD|TENANT", message = "targetMemberType必须是LANDLORD或TENANT")
    private String targetMemberType;

    public String getTargetMemberType() {
        return targetMemberType;
    }

    public void setTargetMemberType(String targetMemberType) {
        this.targetMemberType = targetMemberType;
    }
}
