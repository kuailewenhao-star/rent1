package com.rent1.domain.contract.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 房间入驻邀请码实体
 * 用于房东分享入驻邀请 + 租客自助确认场景
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomInvite {

    /** 邀请ID */
    private String inviteId;

    /** 关联房间ID */
    private String roomId;

    /** 邀请码 */
    private String inviteCode;

    /** 邀请方房东会员ID */
    private String landlordMemberId;

    /** 过期时间（24小时） */
    private LocalDateTime expireTime;

    /** 状态：ACTIVE可用 / USED已使用 / EXPIRED已过期 */
    private String status;

    /** 使用的租客ID */
    private String usedByTenantId;

    /** 创建时间 */
    private LocalDateTime createTime;

    /**
     * 校验邀请码是否有效
     */
    public boolean isValid() {
        if (!"ACTIVE".equals(this.status)) {
            return false;
        }
        return LocalDateTime.now().isBefore(this.expireTime);
    }
}
