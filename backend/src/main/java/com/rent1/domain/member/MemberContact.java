package com.rent1.domain.member;

import com.baomidou.mybatisplus.annotation.*;
import com.rent1.domain.common.ContactRelationship;
import com.rent1.infrastructure.security.typehandler.SensitiveStringTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 会员联系人实体
 * 用于紧急联系人绑定
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("member_contact")
public class MemberContact {

    @TableId(type = IdType.ASSIGN_ID)
    private String contactId;

    /** 关联会员ID */
    private String memberId;

    /** 联系人姓名（加密存储） - 通过 TypeHandler 在读写时自动加解密 */
    @TableField(typeHandler = SensitiveStringTypeHandler.class)
    private String nameEncrypted;

    /** 联系人手机号（加密存储） - 通过 TypeHandler 在读写时自动加解密 */
    @TableField(typeHandler = SensitiveStringTypeHandler.class)
    private String phoneEncrypted;

    /** 与会员关系 */
    private ContactRelationship relationship;

    /** 是否默认联系人 */
    private Boolean isDefault;

    /** 是否紧急联系人 */
    private Boolean isEmergency;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 判断是否为紧急联系人
     */
    public boolean isEmergencyContact() {
        return Boolean.TRUE.equals(this.isEmergency);
    }

    /**
     * 判断是否为默认联系人
     */
    public boolean isDefaultContact() {
        return Boolean.TRUE.equals(this.isDefault);
    }
}
