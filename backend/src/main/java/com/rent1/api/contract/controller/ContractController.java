package com.rent1.api.contract.controller;

import com.rent1.application.contract.service.ContractAppService;
import com.rent1.domain.common.BusinessException;
import com.rent1.domain.common.ContractStatus;
import com.rent1.domain.common.TerminationType;
import com.rent1.api.contract.dto.*;
import com.rent1.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerInterceptor;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 合约管理控制器
 *
 * API接口清单（对应任务拆解文档）：
 * - POST /api/contracts 房东创建合约 (CON-001)
 * - POST /api/contracts/self-checkin 租客自助入驻 (CON-002)
 * - GET /api/contracts 合约列表查询 (CON-020)
 * - GET /api/contracts/{id} 合约详情查询 (CON-021)
 * - PUT /api/contracts/{id}/terminate 提前解约 (CON-011)
 * - PUT /api/contracts/{id}/void 作废合约 (CON-012)
 */
@Slf4j
@RestController
@RequestMapping("/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractAppService contractAppService;

    // ==================== CON-001: 房东手动创建租客合约 ====================

    /**
     * 房东手动创建租客合约
     *
     * @param request 创建请求
     * @return 创建结果
     */
    @PostMapping
    public ApiResponse<CreateContractResponse> createContract(
            @Valid @RequestBody CreateContractRequest request) {
        log.info("创建合约请求 roomId={}, tenantPhone={}",
            request.getRoomId(), request.getTenantPhone());

        try {
            // 获取当前房东ID（简化处理）
            String landlordMemberId = getCurrentMemberId();

            ContractAppService.ContractCreateResult result = contractAppService.createContractByLandlord(
                landlordMemberId,
                request.getRoomId(),
                request.getTenantPhone(),
                request.getRealName(),
                request.getStartDate(),
                request.getEndDate(),
                request.getBillingRules(),
                request.getPaperContractUrl()
            );

            CreateContractResponse response = CreateContractResponse.builder()
                .contractId(result.getContractId())
                .status(result.getStatus().name())
                .roomId(result.getRoomId())
                .tenantMemberId(result.getTenantMemberId())
                .billingRulesSnapshotId(result.getBillingRulesSnapshotId())
                .firstBillIds(result.getFirstBillIds())
                .build();

            return ApiResponse.success(response);
        } catch (BusinessException e) {
            log.error("创建合约业务异常 code={}, msg={}", e.getCode(), e.getMessage());
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("创建合约系统异常", e);
            return ApiResponse.error("SYS_ERROR", "系统异常，请稍后重试");
        }
    }

    // ==================== CON-002: 租客自助入驻 ====================

    /**
     * 生成入驻邀请码
     *
     * @param roomId 房间ID
     * @return 邀请码
     */
    @PostMapping("/rooms/{roomId}/invite")
    public ApiResponse<InviteCodeResponse> generateInviteCode(@PathVariable String roomId) {
        log.info("生成邀请码 roomId={}", roomId);

        try {
            String landlordMemberId = getCurrentMemberId();

            ContractAppService.InviteCodeResult result = contractAppService.generateInviteCode(
                roomId, landlordMemberId);

            InviteCodeResponse response = InviteCodeResponse.builder()
                .inviteCode(result.getInviteCode())
                .expireTime(result.getExpireTime())
                .build();

            return ApiResponse.success(response);
        } catch (BusinessException e) {
            log.error("生成邀请码业务异常", e);
            return ApiResponse.error(e.getCode(), e.getMessage());
        }
    }

    /**
     * 租客自助入驻确认
     *
     * @param request 入驻请求
     * @return 入驻结果
     */
    @PostMapping("/self-checkin")
    public ApiResponse<CreateContractResponse> selfCheckin(
            @Valid @RequestBody SelfCheckinRequest request) {
        log.info("租客自助入驻 inviteCode={}", request.getInviteCode());

        try {
            ContractAppService.ContractCreateResult result = contractAppService.selfCheckin(
                request.getInviteCode(),
                request.getTenantPhone(),
                request.getRealName(),
                request.getIdCard(),
                request.getConfirmed()
            );

            CreateContractResponse response = CreateContractResponse.builder()
                .contractId(result.getContractId())
                .status(result.getStatus().name())
                .roomId(result.getRoomId())
                .tenantMemberId(result.getTenantMemberId())
                .billingRulesSnapshotId(result.getBillingRulesSnapshotId())
                .firstBillIds(result.getFirstBillIds())
                .build();

            return ApiResponse.success(response);
        } catch (BusinessException e) {
            log.error("自助入驻业务异常", e);
            return ApiResponse.error(e.getCode(), e.getMessage());
        }
    }

    // ==================== CON-020: 合约列表查询 ====================

    /**
     * 合约列表查询（房东/租客双视角）
     *
     * @param status 状态筛选（可选）
     * @param roomId 房间ID筛选（可选）
     * @param tenantName 租客姓名筛选（可选）
     * @param page 页码
     * @param pageSize 每页大小
     * @return 合约列表
     */
    @GetMapping
    public ApiResponse<List<ContractListItemResponse>> queryContracts(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String roomId,
            @RequestParam(required = false) String tenantName,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        log.info("查询合约列表 status={}, roomId={}", status, roomId);

        try {
            String memberId = getCurrentMemberId();
            String memberType = getCurrentMemberType();

            ContractStatus contractStatus = null;
            if (status != null && !status.isEmpty()) {
                try {
                    contractStatus = ContractStatus.valueOf(status);
                } catch (IllegalArgumentException e) {
                    return ApiResponse.error("V001", "无效的状态枚举值");
                }
            }

            List<ContractAppService.ContractListItem> items = contractAppService.queryContracts(
                memberId, memberType, contractStatus, roomId, tenantName);

            List<ContractListItemResponse> response = items.stream()
                .map(item -> ContractListItemResponse.builder()
                    .contractId(item.getContractId())
                    .roomId(item.getRoomId())
                    .tenantMemberId(item.getTenantMemberId())
                    .tenantName(item.getTenantNameMasked())
                    .startDate(item.getStartDate())
                    .endDate(item.getEndDate())
                    .status(item.getStatus().name())
                    .statusDesc(item.getStatus().getDescription())
                    .remainingDays(item.getRemainingDays())
                    .build())
                .collect(Collectors.toList());

            return ApiResponse.success(response);
        } catch (BusinessException e) {
            log.error("查询合约列表业务异常", e);
            return ApiResponse.error(e.getCode(), e.getMessage());
        }
    }

    // ==================== CON-021: 合约详情查询 ====================

    /**
     * 合约详情查询
     *
     * @param contractId 合约ID
     * @return 合约详情
     */
    @GetMapping("/{contractId}")
    public ApiResponse<ContractDetailResponse> getContractDetail(@PathVariable String contractId) {
        log.info("查询合约详情 contractId={}", contractId);

        try {
            String memberId = getCurrentMemberId();
            String memberType = getCurrentMemberType();

            ContractAppService.ContractDetail detail = contractAppService.getContractDetail(
                contractId, memberId, memberType);

            ContractDetailResponse response = ContractDetailResponse.builder()
                .contractId(detail.getContractId())
                .roomId(detail.getRoomId())
                .houseSourceId(detail.getHouseSourceId())
                .tenantMemberId(detail.getTenantMemberId())
                .tenantName(detail.getTenantNameMasked())
                .tenantPhone(detail.getTenantPhoneMasked())
                .startDate(detail.getStartDate())
                .endDate(detail.getEndDate())
                .status(detail.getStatus().name())
                .statusDesc(detail.getStatus().getDescription())
                .paperContractUrl(detail.getPaperContractUrl())
                .billingRules(detail.getBillingRulesJson())
                .remainingDays(detail.getRemainingDays())
                .createTime(detail.getCreateTime())
                .build();

            return ApiResponse.success(response);
        } catch (BusinessException e) {
            log.error("查询合约详情业务异常", e);
            return ApiResponse.error(e.getCode(), e.getMessage());
        }
    }

    // ==================== CON-011: 手动提前解约 ====================

    /**
     * 手动提前解约
     *
     * @param contractId 合约ID
     * @param request 解约请求
     * @return 解约结果
     */
    @PutMapping("/{contractId}/terminate")
    public ApiResponse<TerminationResponse> terminateContract(
            @PathVariable String contractId,
            @Valid @RequestBody TerminateContractRequest request) {
        log.info("提前解约 contractId={}, type={}", contractId, request.getTerminationType());

        try {
            TerminationType terminationType;
            try {
                terminationType = TerminationType.valueOf(request.getTerminationType());
            } catch (IllegalArgumentException e) {
                return ApiResponse.error("V001", "无效的终止类型");
            }

            ContractAppService.TerminationResult result = contractAppService.earlyTerminate(
                contractId,
                terminationType,
                request.getRefundAmount(),
                request.getDeductionAmount(),
                request.getDeductionRemark()
            );

            TerminationResponse response = TerminationResponse.builder()
                .contractId(result.getContractId())
                .terminationType(result.getTerminationType())
                .refundAmount(result.getRefundAmount() != null ?
                    result.getRefundAmount().toString() : null)
                .deductionAmount(result.getDeductionAmount() != null ?
                    result.getDeductionAmount().toString() : null)
                .build();

            return ApiResponse.success(response);
        } catch (BusinessException e) {
            log.error("提前解约业务异常", e);
            return ApiResponse.error(e.getCode(), e.getMessage());
        }
    }

    // ==================== CON-012: 合约作废 ====================

    /**
     * 合约作废（新建未履约）
     *
     * @param contractId 合约ID
     * @param request 作废请求
     * @return 作废结果
     */
    @PutMapping("/{contractId}/void")
    public ApiResponse<TerminationResponse> voidContract(
            @PathVariable String contractId,
            @RequestBody(required = false) VoidContractRequest request) {
        log.info("合约作废 contractId={}", contractId);

        try {
            String reason = (request != null) ? request.getReason() : null;

            ContractAppService.TerminationResult result = contractAppService.voidContract(
                contractId, reason);

            TerminationResponse response = TerminationResponse.builder()
                .contractId(result.getContractId())
                .terminationType(result.getTerminationType())
                .build();

            return ApiResponse.success(response);
        } catch (BusinessException e) {
            log.error("合约作废业务异常", e);
            return ApiResponse.error(e.getCode(), e.getMessage());
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 获取当前会员ID
     * 优先从 RequestContextHolder 读取 AuthInterceptor 设置的属性，
     * 若无请求上下文则 fallback 到 DataScopeContext ThreadLocal。
     */
    private String getCurrentMemberId() {
        try {
            Object attrs = org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
            if (attrs instanceof ServletRequestAttributes) {
                String memberId = (String) ((ServletRequestAttributes) attrs)
                        .getRequest().getAttribute(com.rent1.api.interceptor.AuthInterceptor.ATTR_MEMBER_ID);
                if (memberId != null) return memberId;
            }
        } catch (Exception ignored) { /* 无请求上下文 */ }
        return com.rent1.infrastructure.security.DataScopeContext.getMemberId();
    }

    /**
     * 获取当前会员类型
     */
    private String getCurrentMemberType() {
        try {
            Object attrs = org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
            if (attrs instanceof ServletRequestAttributes) {
                String memberType = (String) ((ServletRequestAttributes) attrs)
                        .getRequest().getAttribute(com.rent1.api.interceptor.AuthInterceptor.ATTR_MEMBER_TYPE);
                if (memberType != null) return memberType;
            }
        } catch (Exception ignored) { /* 无请求上下文 */ }
        return com.rent1.infrastructure.security.DataScopeContext.getMemberType();
    }
}
