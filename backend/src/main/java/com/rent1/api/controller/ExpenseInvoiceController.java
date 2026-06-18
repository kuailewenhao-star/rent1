package com.rent1.api.controller;

import com.rent1.application.service.ExpenseInvoiceAppService;
import com.rent1.domain.common.BusinessException;
import com.rent1.domain.invoice.entity.ExpenseInvoice;
import com.rent1.domain.invoice.repository.ExpenseInvoiceRepository;
import com.rent1.infrastructure.security.DataScopeContext;
import com.rent1.common.response.ApiResponse;
import com.rent1.api.dto.invoice.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 支出账单控制器 - 房东端API入口
 * 接口清单：
 * 1. POST /api/expense-invoices - 新增支出账单
 * 2. PUT /api/expense-invoices/{id} - 编辑支出账单
 * 3. DELETE /api/expense-invoices/{id} - 删除支出账单
 * 4. GET /api/expense-invoices - 支出账单列表查询
 * 5. GET /api/expense-invoices/{id} - 支出账单详情查询
 *
 * 职责：请求接收、参数校验、权限拦截、响应封装
 * 禁止行为：禁止执行业务计算、禁止执行业务判断
 *
 * 权限规则：租客完全不可见支出账单，仅房东可视可操作
 */
@Slf4j
@RestController
@RequestMapping("/expense-invoices")
@RequiredArgsConstructor
public class ExpenseInvoiceController {

    private final ExpenseInvoiceAppService expenseInvoiceAppService;
    private final ExpenseInvoiceRepository expenseInvoiceRepository;

    /**
     * 新增支出账单
     * 接口：POST /api/expense-invoices
     */
    @PostMapping
    public ApiResponse<ExpenseInvoiceResponse> create(@Valid @RequestBody ExpenseInvoiceRequest request) {
        log.info("新增支出账单: costType={}, amount={}, houseSourceId={}",
                 request.getCostType(), request.getAmount(), request.getHouseSourceId());

        String landlordMemberId = DataScopeContext.getMemberId();

        if (!DataScopeContext.isLandlord()) {
            return ApiResponse.error("P001", "租客无权限操作支出账单");
        }

        ExpenseInvoice expense = expenseInvoiceAppService.create(
                request.getHouseSourceId(), landlordMemberId,
                request.getCostType(), request.getAmount(),
                request.getCostDate(), request.getRemark());

        return ApiResponse.success(toExpenseResponse(expense));
    }

    /**
     * 编辑支出账单
     * 接口：PUT /api/expense-invoices/{expenseId}
     */
    @PutMapping("/{expenseId}")
    public ApiResponse<ExpenseInvoiceResponse> update(@PathVariable String expenseId,
                                                       @Valid @RequestBody ExpenseInvoiceRequest request) {
        log.info("编辑支出账单: expenseId={}", expenseId);

        ExpenseInvoice expense = expenseInvoiceRepository.findById(expenseId)
                .orElseThrow(() -> new BusinessException("E004", "支出账单不存在"));

        String landlordMemberId = DataScopeContext.getMemberId();
        expenseInvoiceAppService.update(expense, landlordMemberId,
                request.getAmount(), request.getCostDate(),
                request.getCostType(), request.getRemark());

        return ApiResponse.success(toExpenseResponse(expense));
    }

    /**
     * 删除支出账单
     * 接口：DELETE /api/expense-invoices/{expenseId}
     */
    @DeleteMapping("/{expenseId}")
    public ApiResponse<Void> delete(@PathVariable String expenseId) {
        log.info("删除支出账单: expenseId={}", expenseId);

        ExpenseInvoice expense = expenseInvoiceRepository.findById(expenseId)
                .orElseThrow(() -> new BusinessException("E004", "支出账单不存在"));

        String landlordMemberId = DataScopeContext.getMemberId();
        expenseInvoiceAppService.delete(expense, landlordMemberId);

        return ApiResponse.success(null);
    }

    /**
     * 支出账单列表查询
     * 接口：GET /api/expense-invoices
     */
    @GetMapping
    public ApiResponse<List<ExpenseInvoiceResponse>> queryList(
            @RequestParam(required = false) String houseSourceId,
            @RequestParam(required = false) String costType,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        log.info("查询支出账单列表: houseSourceId={}, costType={}, startDate={}, endDate={}",
                 houseSourceId, costType, startDate, endDate);

        if (!DataScopeContext.isLandlord()) {
            return ApiResponse.error("P001", "租客无权限查看支出账单");
        }

        String landlordMemberId = DataScopeContext.getMemberId();
        List<ExpenseInvoice> expenses = expenseInvoiceAppService.query(
                landlordMemberId, houseSourceId, costType, startDate, endDate);

        List<ExpenseInvoiceResponse> responseList = expenses.stream()
                .map(this::toExpenseResponse)
                .collect(Collectors.toList());

        return ApiResponse.success(responseList);
    }

    /**
     * 支出账单详情查询
     * 接口：GET /api/expense-invoices/{expenseId}
     */
    @GetMapping("/{expenseId}")
    public ApiResponse<ExpenseInvoiceResponse> getDetail(@PathVariable String expenseId) {
        log.info("查询支出账单详情: expenseId={}", expenseId);

        ExpenseInvoice expense = expenseInvoiceRepository.findById(expenseId)
                .orElseThrow(() -> new BusinessException("E004", "支出账单不存在"));

        if (!expense.getLandlordMemberId().equals(DataScopeContext.getMemberId())) {
            return ApiResponse.error("P001", "无权限查看该支出账单");
        }

        return ApiResponse.success(toExpenseResponse(expense));
    }

    private ExpenseInvoiceResponse toExpenseResponse(ExpenseInvoice expense) {
        ExpenseInvoiceResponse response = new ExpenseInvoiceResponse();
        response.setExpenseId(expense.getExpenseId());
        response.setCostType(expense.getCostType() != null ? expense.getCostType().getCode() : null);
        response.setCostTypeName(expense.getCostType() != null ? expense.getCostType().getName() : null);
        response.setAmount(expense.getAmount());
        response.setCostDate(expense.getCostDate());
        response.setRemark(expense.getRemark());
        response.setCreateTime(expense.getCreateTime());
        response.setUpdateTime(expense.getUpdateTime());
        return response;
    }
}