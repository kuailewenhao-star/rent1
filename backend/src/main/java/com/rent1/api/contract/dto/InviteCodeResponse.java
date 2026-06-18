package com.rent1.api.contract.dto;

import lombok.Data;
import lombok.Builder;
import java.time.LocalDateTime;

/**
 * 邀请码响应DTO
 */
@Data
@Builder
public class InviteCodeResponse {

    /** 邀请码 */
    private String inviteCode;

    /** 过期时间 */
    private LocalDateTime expireTime;
}
