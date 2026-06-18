package com.rent1.application.deposit;

import com.rent1.domain.common.BusinessException;
import com.rent1.domain.deposit.DepositDomainService;
import com.rent1.domain.deposit.DepositRecord;
import com.rent1.domain.deposit.DepositRepository;
import com.rent1.domain.deposit.DepositStatus;
import com.rent1.domain.deposit.event.DepositRefundedEvent;
import com.rent1.domain.invoice.entity.IncomeInvoice;
import com.rent1.domain.invoice.enums.FeeType;
import com.rent1.domain.invoice.enums.InvoiceStatus;
import com.rent1.domain.invoice.repository.IncomeInvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 押金应用服务
 * 负责押金业务流程编排、跨领域调度、事务控制
 * 
 * 任务范围：
 * - DEP-001: 押金结算（全额退还）
 * - DEP-002: 押金结算（部分扣费）
 * - DEP-003: 房东查看当前有效持有押金总额
 * - DEP-004: 租客查看当前有效押金
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DepositAppService {
    
    private final DepositDomainService depositDomainService;
    private final DepositRepository depositRepository;
    private final IncomeInvoiceRepository incomeInvoiceRepository;
    
    // region DEP-001: 押金结算（全额退还）
    
    /**
     * 押金全额退还
     * 
     * 业务规则：
     * 1. 校验押金记录存在且状态为HELD
     * 2. 校验押金账单状态为PAID
     * 3. 执行全额退还
     * 4. 发布DepositRefundedEvent
     * 
     * @param depositId 押金记录ID
     * @param operatorId 操作人ID（房东）
     * @param operatorType 操作人类型
     * @return 退还结果
     */
    @Transactional
    public DepositSettlementResponse fullRefund(String depositId, String operatorId, String operatorType) {
        log.info("押金全额退还开始 depositId={}, operatorId={}, operatorType={}", 
            depositId, operatorId, operatorType);
        
        // 1. 查询押金记录
        DepositRecord depositRecord = depositRepository.findById(depositId)
            .orElseThrow(() -> new BusinessException("D010", "押金记录不存在"));
        
        // 2. 权限校验：仅房东可操作
        if (!depositDomainService.canOperateDeposit(depositRecord, operatorId)) {
            throw new BusinessException("P001", "无权限操作该押金");
        }
        
        // 3. 查询押金账单状态
        boolean depositInvoicePaid = checkDepositInvoicePaid(depositRecord.getInvoiceId());
        
        // 4. 执行全额退还（领域服务）
        BigDecimal refundAmount = depositDomainService.refund(depositRecord, depositInvoicePaid);
        
        // 5. 保存押金记录
        depositRepository.save(depositRecord);
        
        // 6. 发布领域事件
        DepositRefundedEvent event = DepositRefundedEvent.create(
            depositRecord.getRecordId(),
            depositRecord.getContractId(),
            depositRecord.getTenantMemberId(),
            depositRecord.getLandlordMemberId(),
            depositRecord.getOriginalAmount(),
            depositRecord.getDeductionAmount(),
            depositRecord.getActualRefundAmount(),
            depositRecord.getDeductionReason()
        );
        publishDepositRefundedEvent(event);
        
        log.info("押金全额退还成功 depositId={}, refundAmount={}", depositId, refundAmount);
        
        DepositSettlementResponse resp = new DepositSettlementResponse();
        resp.setRecordId(depositRecord.getRecordId());
        resp.setOriginalAmount(depositRecord.getOriginalAmount());
        resp.setDeductionAmount(depositRecord.getDeductionAmount());
        resp.setActualRefundAmount(refundAmount);
        resp.setStatus(depositRecord.getStatus().name());
        resp.setRefundTime(depositRecord.getRefundTime());
        resp.setSettlementType("FULL");
        return resp;
    }
    
    // endregion
    
    // region DEP-002: 押金结算（部分扣费）
    
    /**
     * 押金部分扣费后退还
     * 
     * 业务规则：
     * 1. 校验押金记录存在且状态为HELD
     * 2. 校验押金账单状态为PAID
     * 3. 扣费金额不能超过押金总额
     * 4. 执行部分扣费退还
     * 5. 记录扣费明细台账
     * 6. 发布DepositRefundedEvent
     * 
     * @param depositId 押金记录ID
     * @param deductionAmount 扣费金额
     * @param deductionReason 扣费原因
     * @param operatorId 操作人ID（房东）
     * @param operatorType 操作人类型
     * @return 退还结果
     */
    @Transactional
    public DepositSettlementResponse partialRefund(String depositId, 
                                                BigDecimal deductionAmount, 
                                                String deductionReason,
                                                String operatorId, 
                                                String operatorType) {
        log.info("押金部分扣费退还开始 depositId={}, deductionAmount={}, operatorId={}", 
            depositId, deductionAmount, operatorId);
        
        // 1. 参数校验
        if (deductionAmount == null || deductionAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("V001", "扣费金额不能为负数");
        }
        if (deductionReason == null || deductionReason.trim().isEmpty()) {
            throw new BusinessException("V002", "请输入扣费原因");
        }
        
        // 2. 查询押金记录
        DepositRecord depositRecord = depositRepository.findById(depositId)
            .orElseThrow(() -> new BusinessException("D010", "押金记录不存在"));
        
        // 3. 权限校验：仅房东可操作
        if (!depositDomainService.canOperateDeposit(depositRecord, operatorId)) {
            throw new BusinessException("P001", "无权限操作该押金");
        }
        
        // 4. 查询押金账单状态
        boolean depositInvoicePaid = checkDepositInvoicePaid(depositRecord.getInvoiceId());
        
        // 5. 执行部分扣费退还（领域服务）
        BigDecimal actualRefundAmount = depositDomainService.deduct(
            depositRecord, depositInvoicePaid, deductionAmount, deductionReason);
        
        // 6. 保存押金记录
        depositRepository.save(depositRecord);
        
        // 7. 发布领域事件
        DepositRefundedEvent event = DepositRefundedEvent.create(
            depositRecord.getRecordId(),
            depositRecord.getContractId(),
            depositRecord.getTenantMemberId(),
            depositRecord.getLandlordMemberId(),
            depositRecord.getOriginalAmount(),
            depositRecord.getDeductionAmount(),
            depositRecord.getActualRefundAmount(),
            depositRecord.getDeductionReason()
        );
        publishDepositRefundedEvent(event);
        
        log.info("押金部分扣费退还成功 depositId={}, deductionAmount={}, actualRefundAmount={}", 
            depositId, deductionAmount, actualRefundAmount);
        
        DepositSettlementResponse resp = new DepositSettlementResponse();
        resp.setRecordId(depositRecord.getRecordId());
        resp.setOriginalAmount(depositRecord.getOriginalAmount());
        resp.setDeductionAmount(deductionAmount);
        resp.setActualRefundAmount(actualRefundAmount);
        resp.setDeductionReason(deductionReason);
        resp.setStatus(depositRecord.getStatus().name());
        resp.setRefundTime(depositRecord.getRefundTime());
        resp.setSettlementType("PARTIAL");
        return resp;
    }
    
    // endregion
    
    // region DEP-003: 房东查看当前有效持有押金总额
    
    /**
     * 房东查看当前有效持有押金总额
     * 
     * 业务规则：
     * 1. 仅统计status=HELD且押金账单status=PAID的记录
     * 2. 已退押金不纳入统计
     * 3. 仅返回自有房源的押金
     * 
     * @param landlordMemberId 房东会员ID
     * @return 有效押金总额及明细条数
     */
    public LandlordDepositSummaryResponse getLandlordValidDeposit(String landlordMemberId) {
        log.info("查询房东有效押金总额 landlordMemberId={}", landlordMemberId);
        
        BigDecimal totalValidDeposit = depositRepository.sumValidDepositByLandlord(landlordMemberId);
        int count = depositRepository.countValidDepositByLandlord(landlordMemberId);
        
        LandlordDepositSummaryResponse resp = new LandlordDepositSummaryResponse();
        resp.setTotalValidDeposit(totalValidDeposit);
        resp.setCount(count);
        return resp;
    }
    
    /**
     * 房东查看押金明细列表
     * 
     * @param landlordMemberId 房东会员ID
     * @return 押金记录列表
     */
    public List<DepositRecordDTO> getLandlordDepositList(String landlordMemberId) {
        List<DepositRecord> deposits = depositRepository.findAllByLandlord(landlordMemberId);
        
        return deposits.stream().map(deposit -> {
            DepositRecordDTO dto = new DepositRecordDTO();
            dto.setRecordId(deposit.getRecordId());
            dto.setContractId(deposit.getContractId());
            dto.setRoomId(deposit.getRoomId());
            dto.setOriginalAmount(deposit.getOriginalAmount());
            dto.setDeductionAmount(deposit.getDeductionAmount());
            dto.setActualRefundAmount(deposit.getActualRefundAmount());
            dto.setStatus(deposit.getStatus().name());
            dto.setStatusName(deposit.getStatus().getName());
            dto.setRefundTime(deposit.getRefundTime());
            dto.setCreateTime(deposit.getCreateTime());
            
            // 查询关联的押金账单金额
            if (deposit.getInvoiceId() != null) {
                incomeInvoiceRepository.findById(deposit.getInvoiceId()).ifPresent(invoice -> {
                    dto.setInvoiceAmount(invoice.getAmount());
                });
            }
            
            return dto;
        }).collect(java.util.stream.Collectors.toList());
    }
    
    // endregion
    
    // region DEP-004: 租客查看当前有效押金
    
    /**
     * 租客查看本人当前有效押金
     * 
     * 业务规则：
     * 1. 仅统计当前租客关联的履约中合约下的押金
     * 2. 押金状态为HELD且账单已支付
     * 
     * @param tenantMemberId 租客会员ID
     * @return 有效押金金额
     */
    public TenantDepositResponse getTenantValidDeposit(String tenantMemberId) {
        log.info("查询租客有效押金 tenantMemberId={}", tenantMemberId);
        
        BigDecimal validDeposit = depositRepository.sumValidDepositByTenant(tenantMemberId);
        
        TenantDepositResponse resp = new TenantDepositResponse();
        resp.setValidDeposit(validDeposit);
        return resp;
    }
    
    /**
     * 租客查看押金历史记录
     * 
     * @param tenantMemberId 租客会员ID
     * @return 押金记录列表
     */
    public List<DepositRecordDTO> getTenantDepositList(String tenantMemberId) {
        List<DepositRecord> deposits = depositRepository.findAllByTenant(tenantMemberId);
        
        return deposits.stream().map(deposit -> {
            DepositRecordDTO dto = new DepositRecordDTO();
            dto.setRecordId(deposit.getRecordId());
            dto.setContractId(deposit.getContractId());
            dto.setRoomId(deposit.getRoomId());
            dto.setHouseSourceId(deposit.getHouseSourceId());
            dto.setOriginalAmount(deposit.getOriginalAmount());
            dto.setDeductionAmount(deposit.getDeductionAmount());
            dto.setActualRefundAmount(deposit.getActualRefundAmount());
            dto.setDeductionReason(deposit.getDeductionReason());
            dto.setStatus(deposit.getStatus().name());
            dto.setStatusName(deposit.getStatus().getName());
            dto.setRefundTime(deposit.getRefundTime());
            dto.setCreateTime(deposit.getCreateTime());
            return dto;
        }).collect(java.util.stream.Collectors.toList());
    }
    
    // endregion
    
    // region 私有辅助方法
    
    /**
     * 校验押金账单是否已支付
     */
    private boolean checkDepositInvoicePaid(String invoiceId) {
        if (invoiceId == null) {
            return false;
        }
        Optional<IncomeInvoice> invoiceOpt = incomeInvoiceRepository.findById(invoiceId);
        if (invoiceOpt.isEmpty()) {
            return false;
        }
        IncomeInvoice invoice = invoiceOpt.get();
        return invoice.getStatus() == InvoiceStatus.PAID 
            || invoice.getStatus() == InvoiceStatus.DEPOSIT_RECEIVED;
    }
    
    /**
     * 发布押金退还领域事件
     */
    private void publishDepositRefundedEvent(DepositRefundedEvent event) {
        // 实际应通过事件总线发布，这里简化处理
        log.info("发布押金退还事件 recordId={}, refundAmount={}", 
            event.getRecordId(), event.getActualRefundAmount());
    }
    
    // endregion
    
    // region 内部类：响应DTO
    
    /**
     * 押金结算响应
     */
    @lombok.Data
    public static class DepositSettlementResponse {
        /** 押金记录ID */
        private String recordId;
        /** 原始押金金额 */
        private BigDecimal originalAmount;
        /** 扣费金额 */
        private BigDecimal deductionAmount;
        /** 实际退还金额 */
        private BigDecimal actualRefundAmount;
        /** 扣费原因 */
        private String deductionReason;
        /** 押金状态 */
        private String status;
        /** 退还时间 */
        private java.time.LocalDateTime refundTime;
        /** 结算类型：FULL-全额，PARTIAL-部分 */
        private String settlementType;
    }
    
    /**
     * 房东押金汇总响应
     */
    @lombok.Data
    public static class LandlordDepositSummaryResponse {
        /** 当前有效持有押金总额 */
        private BigDecimal totalValidDeposit;
        /** 有效押金记录条数 */
        private int count;
    }
    
    /**
     * 租客押金响应
     */
    @lombok.Data
    public static class TenantDepositResponse {
        /** 有效押金金额 */
        private BigDecimal validDeposit;
    }
    
    /**
     * 押金记录DTO
     */
    @Data
    public static class DepositRecordDTO {
        private String recordId;
        private String contractId;
        private String roomId;
        private String houseSourceId;
        private BigDecimal originalAmount;
        private BigDecimal deductionAmount;
        private BigDecimal actualRefundAmount;
        private String deductionReason;
        private String status;
        private String statusName;
        private BigDecimal invoiceAmount;
        private java.time.LocalDateTime refundTime;
        private java.time.LocalDateTime createTime;

        public java.time.LocalDateTime getCreateTime() { return createTime; }
        public void setCreateTime(java.time.LocalDateTime createTime) { this.createTime = createTime; }
    }
    
    // endregion
}
