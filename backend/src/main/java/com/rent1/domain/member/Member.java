package com.rent1.domain.member;

import com.baomidou.mybatisplus.annotation.*;
import com.rent1.domain.common.MemberStatus;
import com.rent1.domain.common.MemberType;
import com.rent1.infrastructure.security.typehandler.SensitiveStringTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 会员实体
 * 统一账号主体架构，支持房东/租客/管理员三类角色
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("member")
public class Member {

    @TableId(type = IdType.ASSIGN_ID)
    private String memberId;

    /** 项目ID（数据隔离） */
    private String projectId;

    /** 主体ID（同用户多角色关联） */
    private String subjectId;

    /** 小程序用户ID(openid) */
    private String userId;

    /** 角色类型 */
    private MemberType memberType;

    /** 手机号（加密存储） - 通过 TypeHandler 在读写时自动加解密 */
    @TableField(typeHandler = SensitiveStringTypeHandler.class)
    private String phoneEncrypted;

    /** 真实姓名（加密存储） - 通过 TypeHandler 在读写时自动加解密 */
    @TableField(typeHandler = SensitiveStringTypeHandler.class)
    private String realNameEncrypted;

    /** 身份证号（加密存储） - 通过 TypeHandler 在读写时自动加解密 */
    @TableField(typeHandler = SensitiveStringTypeHandler.class)
    private String idCardEncrypted;

    /** 头像URL */
    private String avatar;

    /** 账号状态 */
    private MemberStatus status;

    /** 密码哈希（仅管理员使用） */
    private String passwordHash;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 判断账号是否可用
     */
    public boolean isActive() {
        return this.status == MemberStatus.ACTIVE;
    }

    /**
     * 判断是否为管理员
     */
    public boolean isAdmin() {
        return this.memberType == MemberType.ADMIN;
    }

    /**
     * 判断是否为房东
     */
    public boolean isLandlord() {
        return this.memberType == MemberType.LANDLORD;
    }

    /**
     * 判断是否为租客
     */
    public boolean isTenant() {
        return this.memberType == MemberType.TENANT;
    }
}
