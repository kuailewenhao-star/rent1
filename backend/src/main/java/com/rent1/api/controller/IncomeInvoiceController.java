package com.rent1.api.controller;

import com.rent1.application.service.IncomeInvoiceAppService;
import com.rent1.domain.common.BusinessException;
import com.rent1.domain.contract.entity.Contract;
import com.rent1.domain.contract.repository.ContractRepository;
import com.rent1.domain.invoice.entity.IncomeInvoice;
import com.rent1.domain.invoice.repository.IncomeInvoiceRepository;
import com.rent1.infrastructure.security.DataScopeContext;
import com.rent1.common.response.ApiResponse;
import com.rent1.api.dto.invoice.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 收入账单控制器 - 房东端API入口
 * 接口清单：
 * 1. GET /api/income-invoices - 账单列表查询
 * 2. POST /api/income-invoices/manual - 手动录入杂费
 * 3. POST /api/income-invoices/shared - 公摊费用分摊
 * 4. PUT /api/income-invoices/{id}/pay - 账单核销
 * 5. GET /api/income-invoices/{id} - 账单详情查询
 *
 * 职责：请求接收、参数校验、权限拦截、响应封装
 * 禁止行为：禁止执行业务计算、禁止执行业务判断
 */
@Slf4j
@RestController
@RequestMapping("/income-invoices")
@RequiredArgsConstructor
public class IncomeInvoiceController {

    private final IncomeInvoiceAppService incomeInvoiceAppService;
    private final ContractRepository contractRepository;
    private final IncomeInvoiceRepository incomeInvoiceRepository;

    /**
     * 手动录入杂费账单（房间维度）
     * 接口：POST /api/income-invoices/manual
     */
    @PostMapping("/manual")
    public ApiResponse<IncomeInvoiceResponse> manualCreate(@Valid @RequestBody ManualInvoiceRequest request) {
        log.info("手动录入杂费账单: feeType={}, amount={}, roomId={}",
                 request.getFeeType(), request.getAmount(), request.getRoomId());

        String landlordMemberId = DataScopeContext.getMemberId();

        if (!DataScopeContext.isLandlord()) {
            return ApiResponse.error("P001", "租客无权限创建收入账单");
        }

        // 查询房间关联的有效合约
        Contract contract = contractRepository.findActiveByRoomId(request.getRoomId())
                .orElseThrow(() -> new BusinessException("I101", "房间未关联有效合约"));

        // 校验房东权限
        if (!landlordMemberId.equals(contract.getLandlordMemberId())) {
            return ApiResponse.error("P001", "无权限操作该房间");
        }

        IncomeInvoice invoice = incomeInvoiceAppService.manualCreate(
                contract, request.getFeeType(), request.getAmount(),
                request.getBillMonth(), request.getRemark());

        return ApiResponse.success(toIncomeResponse(invoice));
    }

    /**
     * 房源维度公摊费用录入与自动分摊
     * 接口：POST /api/income-invoices/shared
     */
    @PostMapping("/shared")
    public ApiResponse<SharedInvoiceResponse> createShared(@Valid @RequestBody SharedInvoiceRequest request) {
        log.info("公摊费用录入: feeType={}, totalAmount={}, houseSourceId={}",
                 request.getFeeType(), request.getTotalAmount(), request.getHouseSourceId());

        String landlordMemberId = DataScopeContext.getMemberId();

        if (!DataScopeContext.isLandlord()) {
            return ApiResponse.error("P001", "租客无权限创建收入账单");
        }

        // 查询房东在该房源下的在租合约
        List<Contract> contracts = contractRepository.findByLandlordMemberId(landlordMemberId)
                .stream()
                .filter(c -> request.getHouseSourceId().equals(c.getHouseSourceId()))
                .filter(c -> c.getStatus() != null)
                .collect(Collectors.toList());

        if (contracts.isEmpty()) {
            return ApiResponse.error("I103", "该房源下无在租合约");
        }

        List<IncomeInvoice> invoices = incomeInvoiceAppService.createSharedInvoice(
                contracts, request.getFeeType(), request.getTotalAmount(), request.getBillMonth());

        SharedInvoiceResponse response = new SharedInvoiceResponse();
        response.setSplitCount(invoices.size());

        return ApiResponse.success(response);
    }

    /**
     * 账单核销（已支付状态变更）
     * 接口：PUT /api/income-invoices/{invoiceId}/pay
     */
    @PutMapping("/{invoiceId}/pay")
    public ApiResponse<Void> verifyPayment(@PathVariable String invoiceId,
                                           @RequestBody InvoicePayRequest request) {
        log.info("账单核销: invoiceId={}", invoiceId);

        IncomeInvoice invoice = incomeInvoiceRepository.findByIdDirect(invoiceId);
        if (invoice == null) {
            return ApiResponse.error("I104", "账单不存在");
        }

        String landlordMemberId = DataScopeContext.getMemberId();
        if (!landlordMemberId.equals(invoice.getLandlordMemberId())) {
            return ApiResponse.error("P001", "无权限核销该账单");
        }

        if (!invoice.canPay()) {
            return ApiResponse.error("I102", "账单状态不允许核销");
        }

        incomeInvoiceAppService.verifyPayment(invoice, request.getPaidTime());

        return ApiResponse.success(null);
    }

    /**
     * 账单列表查询
     * 接口：GET /api/income-invoices
     */
    @GetMapping
    public ApiResponse<List<IncomeInvoiceResponse>> queryList(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String roomId,
            @RequestParam(required = false) String feeType,
            @RequestParam(required = false) String billMonth,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        log.info("查询收入账单列表: status={}, roomId={}, feeType={}, billMonth={}",
                 status, roomId, feeType, billMonth);

        String memberId = DataScopeContext.getMemberId();
        List<IncomeInvoice> invoices;

        if (DataScopeContext.isTenant()) {
            // 租客视角：仅能查看本人账单
            invoices = incomeInvoiceAppService.queryByTenant(
                    memberId, status, feeType, billMonth, page, pageSize);
        } else if (DataScopeContext.isLandlord()) {
            // 房东视角
            invoices = incomeInvoiceAppService.queryByLandlord(
                    memberId, status, roomId, feeType, billMonth, page, pageSize);
        } else {
            return ApiResponse.error("P001", "无权限查看账单");
        }

        List<IncomeInvoiceResponse> responseList = invoices.stream()
                .map(this::toIncomeResponse)
                .collect(Collectors.toList());

        return ApiResponse.success(responseList);
    }

    /**
     * 租客账单详情查询
     * 接口：GET /api/income-invoices/{invoiceId} （详情共用，权限校验在内部）
     */
    @GetMapping("/{invoiceId}")
    public ApiResponse<IncomeInvoiceResponse> getDetail(@PathVariable String invoiceId) {
        log.info("查询账单详情: invoiceId={}", invoiceId);

        IncomeInvoice invoice = incomeInvoiceRepository.findByIdDirect(invoiceId);
        if (invoice == null) {
            return ApiResponse.error("I104", "账单不存在");
        }

        String memberId = DataScopeContext.getMemberId();
        boolean isLandlord = DataScopeContext.isLandlord();
        boolean isTenant = DataScopeContext.isTenant();

        // 房东校验：必须是自己的账单
        if (isLandlord && !memberId.equals(invoice.getLandlordMemberId())) {
            return ApiResponse.error("P001", "无权限查看该账单");
        }
        // 租客校验：必须是自己的账单
        if (isTenant && !memberId.equals(invoice.getTenantMemberId())) {
            return ApiResponse.error("P001", "无权限查看该账单");
        }

        return ApiResponse.success(toIncomeResponse(invoice));
    }

    private IncomeInvoiceResponse toIncomeResponse(IncomeInvoice invoice) {
        IncomeInvoiceResponse response = new IncomeInvoiceResponse();
        response.setInvoiceId(invoice.getInvoiceId());
        response.setContractId(invoice.getContractId());
        response.setFeeType(invoice.getFeeType() != null ? invoice.getFeeType().getCode() : null);
        response.setFeeTypeName(invoice.getFeeType() != null ? invoice.getFeeType().getName() : null);
        response.setAmount(invoice.getAmount());
        response.setCycleStart(invoice.getCycleStart());
        response.setCycleEnd(invoice.getCycleEnd());
        response.setCycleDescription(invoice.getCycleDescription());
        response.setDueDate(invoice.getDueDate());
        response.setPaidTime(invoice.getPaidTime());
        response.setStatus(invoice.getStatus() != null ? invoice.getStatus().name() : null);
        response.setIsManual(invoice.getIsManual());
        response.setRemark(invoice.getRemark());
        response.setCreateTime(invoice.getCreateTime());
        return response;
    }
}
