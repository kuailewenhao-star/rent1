package com.rent1.domain.deposit;

import com.rent1.domain.common.BusinessException;
import com.rent1.domain.invoice.enums.FeeType;
import com.rent1.domain.invoice.enums.InvoiceStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 押金领域服务 - 核心业务规则承载
 * 
 * 职责边界：押金台账式结算管理
 * 
 * 核心业务规则：
 * 1. 押金收取记录（关联收入账单-押金类型）
 * 2. 押金退还台账（全额退/部分扣费退）
 * 3. 有效押金统计（房东端：当前持有有效押金；租客端：当前承租有效押金）
 * 
 * 数据隔离：
 * - 房东仅可操作自有房源下的押金
 * - 租客仅可查看本人押金的退还状态
 */
@Slf4j
@Service
public class DepositDomainService {
    
    //region 错误码定义
    /** 押金扣费金额超限 */
    public static final String ERR_DEDUCTION_EXCEEDS_DEPOSIT = "D001";
    /** 押金已结算 */
    public static final String ERR_DEPOSIT_ALREADY_SETTLED = "D002";
    /** 押金账单未支付 */
    public static final String ERR_DEPOSIT_INVOICE_NOT_PAID = "D003";
    //endregion
    
    //region 押金结算 - 全额退还
    
    /**
     * 全额退还押金 - 核心领域逻辑
     * 
     * 业务规则：
     * 1. 仅HELD状态的押金可退还
     * 2. 押金账单状态必须为PAID
     * 3. 退还金额 = 原始押金金额
     * 4. 状态变更为REFUNDED
     * 
     * @param depositRecord 押金记录
     * @param depositInvoicePaid 押金账单是否已支付
     * @return 实际退还金额
     */
    public BigDecimal refund(DepositRecord depositRecord, boolean depositInvoicePaid) {
        // 规则校验1：押金状态必须为持有中
        if (!depositRecord.canSettle()) {
            log.warn("押金记录{}非持有状态，无法退还", depositRecord.getRecordId());
            throw new BusinessException(ERR_DEPOSIT_ALREADY_SETTLED, "押金已结算完成");
        }
        
        // 规则校验2：押金账单必须已支付
        if (!depositInvoicePaid) {
            log.warn("押金记录{}对应账单未支付", depositRecord.getRecordId());
            throw new BusinessException(ERR_DEPOSIT_INVOICE_NOT_PAID, "押金账单未支付，无法退还");
        }
        
        // 执行全额退还
        BigDecimal refundAmount = depositRecord.fullRefund();
        
        log.info("押金全额退还成功 recordId={}, refundAmount={}", 
            depositRecord.getRecordId(), refundAmount);
        
        return refundAmount;
    }
    
    //endregion
    
    //region 押金结算 - 部分扣费后退还
    
    /**
     * 部分扣费后退还押金 - 核心领域逻辑
     * 
     * 业务规则：
     * 1. 仅HELD状态的押金可结算
     * 2. 扣费金额必须 ≤ 押金总额
     * 3. 实际退还 = 押金总额 - 扣费金额
     * 4. 记录扣费明细台账
     * 
     * @param depositRecord 押金记录
     * @param depositInvoicePaid 押金账单是否已支付
     * @param deductionAmount 扣费金额
     * @param deductionReason 扣费原因
     * @return 实际退还金额
     */
    public BigDecimal deduct(DepositRecord depositRecord, 
                            boolean depositInvoicePaid,
                            BigDecimal deductionAmount, 
                            String deductionReason) {
        // 规则校验1：押金状态必须为持有中
        if (!depositRecord.canSettle()) {
            log.warn("押金记录{}非持有状态，无法结算", depositRecord.getRecordId());
            throw new BusinessException(ERR_DEPOSIT_ALREADY_SETTLED, "押金已结算完成");
        }
        
        // 规则校验2：押金账单必须已支付
        if (!depositInvoicePaid) {
            log.warn("押金记录{}对应账单未支付", depositRecord.getRecordId());
            throw new BusinessException(ERR_DEPOSIT_INVOICE_NOT_PAID, "押金账单未支付，无法结算");
        }
        
        // 规则校验3：扣费金额不能超过押金总额
        if (deductionAmount.compareTo(depositRecord.getOriginalAmount()) > 0) {
            log.warn("扣费金额{}超过押金总额{}", deductionAmount, depositRecord.getOriginalAmount());
            throw new BusinessException(ERR_DEDUCTION_EXCEEDS_DEPOSIT, "扣费金额不能超过押金总额");
        }
        
        // 执行部分扣费退还
        BigDecimal actualRefundAmount = depositRecord.partialRefund(deductionAmount, deductionReason);
        
        log.info("押金部分扣费退还成功 recordId={}, deductionAmount={}, actualRefundAmount={}", 
            depositRecord.getRecordId(), deductionAmount, actualRefundAmount);
        
        return actualRefundAmount;
    }
    
    //endregion
    
    //region 押金统计
    
    /**
     * 计算房东有效持有押金总额
     * 
     * 业务规则：
     * 1. 仅统计status=HELD的押金记录
     * 2. 仅统计关联押金账单status=PAID的记录
     * 3. 已退押金不纳入统计
     * 
     * @param landlordMemberId 房东会员ID
     * @return 有效押金总额
     */
    public BigDecimal calculateValidDeposit(String landlordMemberId) {
        // 由Repository层注入，这里仅承载核心校验逻辑
        log.info("计算房东有效持有押金总额 landlordMemberId={}", landlordMemberId);
        return BigDecimal.ZERO; // 占位，由应用层调用Repository实际查询
    }
    
    /**
     * 计算租客有效押金
     * 
     * 业务规则：
     * 1. 仅统计当前租客关联的履约中合约下的押金
     * 2. 押金状态为HELD且账单已支付
     * 
     * @param tenantMemberId 租客会员ID
     * @return 有效押金金额
     */
    public BigDecimal calculateTenantValidDeposit(String tenantMemberId) {
        log.info("计算租客有效押金 tenantMemberId={}", tenantMemberId);
        return BigDecimal.ZERO; // 占位，由应用层调用Repository实际查询
    }
    
    //endregion
    
    //region 权限校验
    
    /**
     * 校验房东是否有权操作该押金
     * 
     * @param depositRecord 押金记录
     * @param landlordMemberId 房东会员ID
     * @return 是否有权
     */
    public boolean canOperateDeposit(DepositRecord depositRecord, String landlordMemberId) {
        return depositRecord.getLandlordMemberId().equals(landlordMemberId);
    }
    
    /**
     * 校验用户是否有权查看该押金
     * 
     * @param depositRecord 押金记录
     * @param memberId 会员ID
     * @param memberType 会员类型
     * @return 是否有权
     */
    public boolean canViewDeposit(DepositRecord depositRecord, String memberId, String memberType) {
        if ("ADMIN".equals(memberType)) {
            return true; // 管理员可查看所有
        }
        if ("LANDLORD".equals(memberType)) {
            return depositRecord.getLandlordMemberId().equals(memberId);
        }
        if ("TENANT".equals(memberType)) {
            return depositRecord.getTenantMemberId().equals(memberId);
        }
        return false;
    }
    
    //endregion
}
