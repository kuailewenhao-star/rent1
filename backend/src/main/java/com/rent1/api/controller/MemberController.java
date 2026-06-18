package com.rent1.api.controller;

import com.rent1.application.service.MemberAppService;
import com.rent1.api.dto.*;
import com.rent1.common.response.ApiResponse;
import com.rent1.api.interceptor.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

/**
 * 会员控制器
 * MEM-030: 个人信息查询与编辑
 * MEM-020: 联系人管理
 */
@Slf4j
@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberAppService memberAppService;

    /**
     * 获取当前会员个人信息
     * GET /api/member/profile
     */
    @GetMapping("/profile")
    public ApiResponse<MemberProfileDTO> getProfile(HttpServletRequest httpRequest) {
        String memberId = (String) httpRequest.getAttribute(AuthInterceptor.ATTR_MEMBER_ID);
        String memberType = (String) httpRequest.getAttribute(AuthInterceptor.ATTR_MEMBER_TYPE);
        
        // 判断是否小程序端（租客/房东）还是后台管理员
        boolean isMiniProgram = "LANDLORD".equals(memberType) || "TENANT".equals(memberType);
        
        MemberProfileDTO profile = memberAppService.getProfile(memberId, isMiniProgram);
        return ApiResponse.success(profile);
    }

    /**
     * 更新个人信息
     * PUT /api/member/profile
     */
    @PutMapping("/profile")
    public ApiResponse<Void> updateProfile(
            HttpServletRequest httpRequest,
            @Valid @RequestBody UpdateProfileRequest request) {
        
        String memberId = (String) httpRequest.getAttribute(AuthInterceptor.ATTR_MEMBER_ID);
        log.info("Update profile: memberId={}", memberId);

        memberAppService.updateProfile(
                memberId,
                request.getRealName(),
                request.getIdCard(),
                request.getAvatar()
        );

        return ApiResponse.success();
    }

    /**
     * 获取联系人列表
     * GET /api/member/contacts
     */
    @GetMapping("/contacts")
    public ApiResponse<List<ContactDTO>> getContacts(HttpServletRequest httpRequest) {
        String memberId = (String) httpRequest.getAttribute(AuthInterceptor.ATTR_MEMBER_ID);
        String memberType = (String) httpRequest.getAttribute(AuthInterceptor.ATTR_MEMBER_ID);
        
        boolean isMiniProgram = "LANDLORD".equals(memberType) || "TENANT".equals(memberType);
        List<ContactDTO> contacts = memberAppService.getContacts(memberId, isMiniProgram);
        return ApiResponse.success(contacts);
    }

    /**
     * 新增联系人
     * POST /api/member/contacts
     */
    @PostMapping("/contacts")
    public ApiResponse<ContactDTO> createContact(
            HttpServletRequest httpRequest,
            @Valid @RequestBody CreateContactRequest request) {
        
        String memberId = (String) httpRequest.getAttribute(AuthInterceptor.ATTR_MEMBER_ID);
        String memberType = (String) httpRequest.getAttribute(AuthInterceptor.ATTR_MEMBER_TYPE);
        
        boolean isMiniProgram = "LANDLORD".equals(memberType) || "TENANT".equals(memberType);
        
        log.info("Create contact: memberId={}, isEmergency={}", memberId, request.getIsEmergency());

        ContactDTO contact = memberAppService.createContact(
                memberId,
                request.getName(),
                request.getPhone(),
                request.getRelationship(),
                request.getIsEmergency(),
                isMiniProgram
        );

        return ApiResponse.success(contact);
    }

    /**
     * 删除联系人
     * DELETE /api/member/contacts/{contactId}
     */
    @DeleteMapping("/contacts/{contactId}")
    public ApiResponse<Void> deleteContact(
            HttpServletRequest httpRequest,
            @PathVariable String contactId) {
        
        String memberId = (String) httpRequest.getAttribute(AuthInterceptor.ATTR_MEMBER_ID);
        log.info("Delete contact: memberId={}, contactId={}", memberId, contactId);

        memberAppService.deleteContact(contactId, memberId);
        return ApiResponse.success();
    }
}
