package com.rent1.api.controller;

import com.rent1.application.deposit.DepositAppService;
import com.rent1.api.deposit.dto.*;
import com.rent1.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import com.rent1.infrastructure.security.DataScopeContext;
import java.util.List;

/**
 * 押金管理控制器
 *
 * 任务范围：
 * - DEP-001: 押金结算（全额退还）
 * - DEP-002: 押金结算（部分扣费）
 * - DEP-003: 房东查看当前有效持有押金总额
 * - DEP-004: 租客查看当前有效押金
 *
 * 接口权限说明：
 * - 全额退还/部分扣费：仅房东可操作
 * - 房东押金查询：仅房东可访问
 * - 租客押金查询：仅租客可访问
 */
@Slf4j
@RestController
@RequestMapping("/deposits")
@RequiredArgsConstructor
public class DepositController {

    private final DepositAppService depositAppService;

    // ==================== DEP-001: 押金全额退还 ====================

    /**
     * 押金全额退还
     *
     * 接口路径: POST /api/deposits/{depositId}/refund
     * 所属模块: 押金域
     * 鉴权要求: 房东角色
     *
     * @param depositId 押金记录ID
     * @return 退还结果
     */
    @PostMapping("/{depositId}/refund")
    public ApiResponse<DepositSettlementResponse> fullRefund(
            @PathVariable String depositId) {

        log.info("押金全额退还请求 depositId={}", depositId);

        String memberId = DataScopeContext.getMemberId();
        String memberType = DataScopeContext.getMemberType();

        // 仅房东可操作
        if (!"LANDLORD".equals(memberType)) {
            return ApiResponse.error("P001", "无权限操作");
        }

        DepositAppService.DepositSettlementResponse result = depositAppService.fullRefund(
            depositId, memberId, memberType);

        DepositSettlementResponse response = new DepositSettlementResponse();
        response.setRecordId(result.getRecordId());
        response.setOriginalAmount(result.getOriginalAmount());
        response.setDeductionAmount(result.getDeductionAmount());
        response.setActualRefundAmount(result.getActualRefundAmount());
        response.setDeductionReason(result.getDeductionReason());
        response.setStatus(result.getStatus());
        response.setRefundTime(result.getRefundTime());
        response.setSettlementType(result.getSettlementType());

        return ApiResponse.success(response);
    }

    // ==================== DEP-002: 押金部分扣费退还 ====================

    /**
     * 押金部分扣费后退还
     *
     * 接口路径: POST /api/deposits/{depositId}/partial-refund
     * 所属模块: 押金域
     * 鉴权要求: 房东角色
     *
     * @param depositId 押金记录ID
     * @param request 扣费信息
     * @return 退还结果
     */
    @PostMapping("/{depositId}/partial-refund")
    public ApiResponse<DepositSettlementResponse> partialRefund(
            @PathVariable String depositId,
            @Valid @RequestBody PartialRefundRequest request) {

        log.info("押金部分扣费退还请求 depositId={}, deductionAmount={}",
            depositId, request.getDeductionAmount());

        String memberId = DataScopeContext.getMemberId();
        String memberType = DataScopeContext.getMemberType();

        // 仅房东可操作
        if (!"LANDLORD".equals(memberType)) {
            return ApiResponse.error("P001", "无权限操作");
        }

        DepositAppService.DepositSettlementResponse result = depositAppService.partialRefund(
            depositId,
            request.getDeductionAmount(),
            request.getDeductionReason(),
            memberId,
            memberType);

        DepositSettlementResponse response = new DepositSettlementResponse();
        response.setRecordId(result.getRecordId());
        response.setOriginalAmount(result.getOriginalAmount());
        response.setDeductionAmount(result.getDeductionAmount());
        response.setActualRefundAmount(result.getActualRefundAmount());
        response.setDeductionReason(result.getDeductionReason());
        response.setStatus(result.getStatus());
        response.setRefundTime(result.getRefundTime());
        response.setSettlementType(result.getSettlementType());

        return ApiResponse.success(response);
    }

    // ==================== DEP-003: 房东查看押金 ====================

    /**
     * 房东查看当前有效持有押金总额
     *
     * 接口路径: GET /api/landlord/deposits/total
     * 所属模块: 押金域
     * 鉴权要求: 房东角色
     *
     * @return 有效押金总额及条数
     */
    @GetMapping("/landlord/total")
    public ApiResponse<LandlordDepositSummaryResponse> getLandlordValidDeposit() {

        log.info("查询房东有效押金总额");

        String memberId = DataScopeContext.getMemberId();
        String memberType = DataScopeContext.getMemberType();
        // 仅房东可操作
        if (!"LANDLORD".equals(memberType)) {
            return ApiResponse.error("P001", "无权限访问");
        }

        DepositAppService.LandlordDepositSummaryResponse result = depositAppService.getLandlordValidDeposit(memberId);

        LandlordDepositSummaryResponse response = new LandlordDepositSummaryResponse();
        response.setTotalValidDeposit(result.getTotalValidDeposit());
        response.setCount(result.getCount());

        return ApiResponse.success(response);
    }

    /**
     * 房东查看押金明细列表
     *
     * 接口路径: GET /api/landlord/deposits
     * 所属模块: 押金域
     * 鉴权要求: 房东角色
     *
     * @return 押金记录列表
     */
    @GetMapping("/landlord/list")
    public ApiResponse<List<DepositRecordResponse>> getLandlordDepositList() {

        log.info("查询房东押金列表");

        String memberId = DataScopeContext.getMemberId();
        String memberType = DataScopeContext.getMemberType();
        // 仅房东可操作
        if (!"LANDLORD".equals(memberType)) {
            return ApiResponse.error("P001", "无权限访问");
        }

        List<DepositRecordResponse> result = depositAppService.getLandlordDepositList(memberId)
            .stream()
            .map(this::toResponse)
            .collect(java.util.stream.Collectors.toList());

        return ApiResponse.success(result);
    }

    // ==================== DEP-004: 租客查看押金 ====================

    /**
     * 租客查看本人当前有效押金
     *
     * 接口路径: GET /api/tenant/deposit
     * 所属模块: 押金域
     * 鉴权要求: 租客角色
     *
     * @return 有效押金金额
     */
    @GetMapping("/tenant/deposit")
    public ApiResponse<TenantDepositResponse> getTenantValidDeposit() {

        log.info("查询租客有效押金");

        String memberId = DataScopeContext.getMemberId();
        String memberType = DataScopeContext.getMemberType();
        // 仅租客可操作
        if (!"TENANT".equals(memberType)) {
            return ApiResponse.error("P001", "无权限访问");
        }

        DepositAppService.TenantDepositResponse result = depositAppService.getTenantValidDeposit(memberId);

        TenantDepositResponse response = new TenantDepositResponse();
        response.setValidDeposit(result.getValidDeposit());

        return ApiResponse.success(response);
    }

    /**
     * 租客查看押金历史记录
     *
     * 接口路径: GET /api/tenant/deposits
     * 所属模块: 押金域
     * 鉴权要求: 租客角色
     *
     * @return 押金记录列表
     */
    @GetMapping("/tenant/list")
    public ApiResponse<List<DepositRecordResponse>> getTenantDepositList() {

        log.info("查询租客押金列表");

        String memberId = DataScopeContext.getMemberId();
        String memberType = DataScopeContext.getMemberType();
        // 仅租客可操作
        if (!"TENANT".equals(memberType)) {
            return ApiResponse.error("P001", "无权限访问");
        }

        List<DepositRecordResponse> result = depositAppService.getTenantDepositList(memberId)
            .stream()
            .map(this::toResponse)
            .collect(java.util.stream.Collectors.toList());

        return ApiResponse.success(result);
    }

    // ==================== 私有辅助方法 ====================

    private DepositRecordResponse toResponse(DepositAppService.DepositRecordDTO dto) {
        DepositRecordResponse response = new DepositRecordResponse();
        response.setRecordId(dto.getRecordId());
        response.setContractId(dto.getContractId());
        response.setRoomId(dto.getRoomId());
        response.setHouseSourceId(dto.getHouseSourceId());
        response.setOriginalAmount(dto.getOriginalAmount());
        response.setDeductionAmount(dto.getDeductionAmount());
        response.setActualRefundAmount(dto.getActualRefundAmount());
        response.setDeductionReason(dto.getDeductionReason());
        response.setStatus(dto.getStatus());
        response.setStatusName(dto.getStatusName());
        response.setRefundTime(dto.getRefundTime());
        response.setCreateTime(dto.getCreateTime());
        return response;
    }
}
