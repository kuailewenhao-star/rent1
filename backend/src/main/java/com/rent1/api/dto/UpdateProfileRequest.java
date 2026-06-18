package com.rent1.api.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 更新个人信息请求
 */
public class UpdateProfileRequest {

    @Size(max = 20, message = "姓名最长20字符")
    private String realName;

    @Pattern(regexp = "^[0-9]{17}[0-9Xx]$|^[0-9]{15}$", message = "身份证号格式不正确")
    private String idCard;

    private String avatar;

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
