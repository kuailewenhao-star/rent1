package com.rent1.api.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 微信登录请求
 * MEM-001/MEM-002
 */
public class WechatLoginRequest {

    /** 微信授权code */
    @NotBlank(message = "code不能为空")
    private String code;

    /** 会员类型：LANDLORD-房东 / TENANT-租客 */
    @NotBlank(message = "memberType不能为空")
    @Pattern(regexp = "LANDLORD|TENANT", message = "memberType必须是LANDLORD或TENANT")
    private String memberType;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMemberType() {
        return memberType;
    }

    public void setMemberType(String memberType) {
        this.memberType = memberType;
    }
}
