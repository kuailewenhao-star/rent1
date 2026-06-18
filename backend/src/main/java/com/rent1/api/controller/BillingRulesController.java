package com.rent1.api.controller;

import com.rent1.application.service.BillingRulesAppService;
import com.rent1.common.response.ApiResponse;
import com.rent1.api.dto.billing.AddBillingItemRequest;
import com.rent1.api.dto.billing.BillingRulesResponse;
import com.rent1.api.dto.billing.UpdateBillingItemRequest;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 计费规则 Controller - 接入层
 *
 * 仅负责：
 * 1. 参数接收与基础校验（@Valid）
 * 2. 权限拦截（由全局拦截器统一处理）
 * 3. 调用应用层服务完成业务流程
 * 4. 统一响应封装
 *
 * 禁止在此层编写任何业务规则、业务判断。
 */
@RestController
@RequestMapping("/rooms/{roomId}/billing-rules")
public class BillingRulesController {

    private final BillingRulesAppService appService;

    @Autowired
    public BillingRulesController(BillingRulesAppService appService) {
        this.appService = appService;
    }

    /**
     * 查询房间计费规则
     * GET /api/rooms/{roomId}/billing-rules?contractId=xxx
     *
     * 已签约房间通过 contractId 参数获取锁定快照；未签约则返回当前配置。
     */
    @GetMapping
    public ApiResponse<BillingRulesResponse> getBillingRules(
            @PathVariable String roomId,
            @RequestParam(required = false) String contractId) {
        return ApiResponse.success(appService.getRoomBillingRules(roomId, contractId));
    }

    /**
     * 新增费用项配置
     * POST /api/rooms/{roomId}/billing-rules
     */
    @PostMapping
    public ApiResponse<BillingRulesResponse> addBillingItem(
            @PathVariable String roomId,
            @Valid @RequestBody AddBillingItemRequest request) {
        return ApiResponse.success(appService.addBillingItem(roomId, request));
    }

    /**
     * 编辑指定 feeType 的计费项配置
     * PUT /api/rooms/{roomId}/billing-rules/{feeType}
     */
    @PutMapping("/{feeType}")
    public ApiResponse<BillingRulesResponse> updateBillingItem(
            @PathVariable String roomId,
            @PathVariable String feeType,
            @Valid @RequestBody UpdateBillingItemRequest request) {
        return ApiResponse.success(appService.updateBillingItem(roomId, feeType, request));
    }

    /**
     * 删除指定 feeType 的计费项配置
     * DELETE /api/rooms/{roomId}/billing-rules/{feeType}
     * 租金RENT和押金DEPOSIT为系统固定项，不可删除
     */
    @DeleteMapping("/{feeType}")
    public ApiResponse<BillingRulesResponse> deleteBillingItem(
            @PathVariable String roomId,
            @PathVariable String feeType) {
        return ApiResponse.success(appService.deleteBillingItem(roomId, feeType));
    }
}
