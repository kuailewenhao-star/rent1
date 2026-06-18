package com.rent1.domain.deposit;

import com.rent1.domain.common.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 押金领域服务单元测试
 * 
 * 测试范围：
 * - DEP-001: 押金全额退还
 * - DEP-002: 押金部分扣费退还
 * - 权限校验
 */
class DepositDomainServiceTest {
    
    private DepositDomainService depositDomainService;
    
    @BeforeEach
    void setUp() {
        depositDomainService = new DepositDomainService();
    }
    
    // ==================== DEP-001: 押金全额退还测试 ====================
    
    @Nested
    @DisplayName("DEP-001: 押金全额退还")
    class FullRefundTests {
        
        @Test
        @DisplayName("正常流程：押金全额退还成功")
        void fullRefund_Success() {
            // given: 持有中的押金记录
            DepositRecord depositRecord = createHeldDeposit(
                "DEP_001", "CON_001", "LANDLORD_001", "TENANT_001",
                new BigDecimal("1000.00"));
            
            // when: 执行全额退还
            BigDecimal refundAmount = depositDomainService.refund(depositRecord, true);
            
            // then: 退还金额等于原始押金
            assertEquals(new BigDecimal("1000.00"), refundAmount);
            assertEquals(DepositStatus.REFUNDED, depositRecord.getStatus());
            assertEquals(new BigDecimal("1000.00"), depositRecord.getActualRefundAmount());
            assertEquals(BigDecimal.ZERO, depositRecord.getDeductionAmount());
            assertNull(depositRecord.getDeductionReason());
            assertNotNull(depositRecord.getRefundTime());
        }
        
        @Test
        @DisplayName("异常流程：押金已结算，抛出业务异常")
        void fullRefund_AlreadySettled_ThrowsException() {
            // given: 已结算的押金记录
            DepositRecord depositRecord = createRefundedDeposit(
                "DEP_001", "CON_001", "LANDLORD_001", "TENANT_001",
                new BigDecimal("1000.00"), BigDecimal.ZERO);
            
            // when/then: 抛出业务异常
            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> depositDomainService.refund(depositRecord, true)
            );
            assertEquals(DepositDomainService.ERR_DEPOSIT_ALREADY_SETTLED, exception.getCode());
        }
        
        @Test
        @DisplayName("异常流程：押金账单未支付，抛出业务异常")
        void fullRefund_InvoiceNotPaid_ThrowsException() {
            // given: 持有中的押金记录，但账单未支付
            DepositRecord depositRecord = createHeldDeposit(
                "DEP_001", "CON_001", "LANDLORD_001", "TENANT_001",
                new BigDecimal("1000.00"));
            
            // when/then: 抛出业务异常
            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> depositDomainService.refund(depositRecord, false)
            );
            assertEquals(DepositDomainService.ERR_DEPOSIT_INVOICE_NOT_PAID, exception.getCode());
        }
    }
    
    // ==================== DEP-002: 押金部分扣费退还测试 ====================
    
    @Nested
    @DisplayName("DEP-002: 押金部分扣费退还")
    class PartialRefundTests {
        
        @Test
        @DisplayName("正常流程：部分扣费退还成功")
        void partialRefund_Success() {
            // given: 持有中的押金记录，押金1000元，扣费300元
            DepositRecord depositRecord = createHeldDeposit(
                "DEP_001", "CON_001", "LANDLORD_001", "TENANT_001",
                new BigDecimal("1000.00"));
            BigDecimal deductionAmount = new BigDecimal("300.00");
            String deductionReason = "房屋损坏赔偿";
            
            // when: 执行部分扣费退还
            BigDecimal actualRefundAmount = depositDomainService.deduct(
                depositRecord, true, deductionAmount, deductionReason);
            
            // then: 实际退还700元
            assertEquals(new BigDecimal("700.00"), actualRefundAmount);
            assertEquals(DepositStatus.REFUNDED, depositRecord.getStatus());
            assertEquals(new BigDecimal("300.00"), depositRecord.getDeductionAmount());
            assertEquals(deductionReason, depositRecord.getDeductionReason());
        }
        
        @Test
        @DisplayName("边界值：扣费金额等于押金总额，实际退还0元")
        void partialRefund_DeductionEqualsDeposit_ActualRefundZero() {
            // given: 押金1000元，扣费1000元
            DepositRecord depositRecord = createHeldDeposit(
                "DEP_001", "CON_001", "LANDLORD_001", "TENANT_001",
                new BigDecimal("1000.00"));
            BigDecimal deductionAmount = new BigDecimal("1000.00");
            
            // when: 执行部分扣费退还
            BigDecimal actualRefundAmount = depositDomainService.deduct(
                depositRecord, true, deductionAmount, "全额扣款");
            
            // then: 实际退还0元
            assertEquals(BigDecimal.ZERO, actualRefundAmount);
            assertEquals(DepositStatus.REFUNDED, depositRecord.getStatus());
        }
        
        @Test
        @DisplayName("异常流程：扣费金额超过押金总额，抛出业务异常")
        void partialRefund_DeductionExceedsDeposit_ThrowsException() {
            // given: 押金1000元，扣费1500元
            DepositRecord depositRecord = createHeldDeposit(
                "DEP_001", "CON_001", "LANDLORD_001", "TENANT_001",
                new BigDecimal("1000.00"));
            BigDecimal deductionAmount = new BigDecimal("1500.00");
            
            // when/then: 抛出业务异常
            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> depositDomainService.deduct(depositRecord, true, deductionAmount, "测试")
            );
            assertEquals(DepositDomainService.ERR_DEDUCTION_EXCEEDS_DEPOSIT, exception.getCode());
        }
        
        @Test
        @DisplayName("异常流程：押金已结算，抛出业务异常")
        void partialRefund_AlreadySettled_ThrowsException() {
            // given: 已结算的押金记录
            DepositRecord depositRecord = createRefundedDeposit(
                "DEP_001", "CON_001", "LANDLORD_001", "TENANT_001",
                new BigDecimal("1000.00"), new BigDecimal("300.00"));
            
            // when/then: 抛出业务异常
            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> depositDomainService.deduct(
                    depositRecord, true, new BigDecimal("300"), "测试")
            );
            assertEquals(DepositDomainService.ERR_DEPOSIT_ALREADY_SETTLED, exception.getCode());
        }
        
        @Test
        @DisplayName("异常流程：押金账单未支付，抛出业务异常")
        void partialRefund_InvoiceNotPaid_ThrowsException() {
            // given: 持有中的押金记录，但账单未支付
            DepositRecord depositRecord = createHeldDeposit(
                "DEP_001", "CON_001", "LANDLORD_001", "TENANT_001",
                new BigDecimal("1000.00"));
            
            // when/then: 抛出业务异常
            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> depositDomainService.deduct(
                    depositRecord, false, new BigDecimal("300"), "测试")
            );
            assertEquals(DepositDomainService.ERR_DEPOSIT_INVOICE_NOT_PAID, exception.getCode());
        }
    }
    
    // ==================== 权限校验测试 ====================
    
    @Nested
    @DisplayName("权限校验")
    class PermissionTests {
        
        @Test
        @DisplayName("房东可操作自有房源的押金")
        void canOperateDeposit_LandlordOwnDeposit_ReturnsTrue() {
            // given
            DepositRecord depositRecord = createHeldDeposit(
                "DEP_001", "CON_001", "LANDLORD_001", "TENANT_001",
                new BigDecimal("1000.00"));
            
            // when/then
            assertTrue(depositDomainService.canOperateDeposit(depositRecord, "LANDLORD_001"));
        }
        
        @Test
        @DisplayName("房东不可操作他人房源的押金")
        void canOperateDeposit_LandlordOtherDeposit_ReturnsFalse() {
            // given
            DepositRecord depositRecord = createHeldDeposit(
                "DEP_001", "CON_001", "LANDLORD_001", "TENANT_001",
                new BigDecimal("1000.00"));
            
            // when/then
            assertFalse(depositDomainService.canOperateDeposit(depositRecord, "LANDLORD_002"));
        }
        
        @Test
        @DisplayName("租客不可操作押金退还")
        void canOperateDeposit_Tenant_ReturnsFalse() {
            // given
            DepositRecord depositRecord = createHeldDeposit(
                "DEP_001", "CON_001", "LANDLORD_001", "TENANT_001",
                new BigDecimal("1000.00"));
            
            // when/then
            assertFalse(depositDomainService.canOperateDeposit(depositRecord, "TENANT_001"));
        }
        
        @Test
        @DisplayName("房东可查看押金")
        void canViewDeposit_LandlordOwnDeposit_ReturnsTrue() {
            // given
            DepositRecord depositRecord = createHeldDeposit(
                "DEP_001", "CON_001", "LANDLORD_001", "TENANT_001",
                new BigDecimal("1000.00"));
            
            // when/then
            assertTrue(depositDomainService.canViewDeposit(depositRecord, "LANDLORD_001", "LANDLORD"));
        }
        
        @Test
        @DisplayName("租客可查看本人押金")
        void canViewDeposit_TenantOwnDeposit_ReturnsTrue() {
            // given
            DepositRecord depositRecord = createHeldDeposit(
                "DEP_001", "CON_001", "LANDLORD_001", "TENANT_001",
                new BigDecimal("1000.00"));
            
            // when/then
            assertTrue(depositDomainService.canViewDeposit(depositRecord, "TENANT_001", "TENANT"));
        }
        
        @Test
        @DisplayName("管理员可查看所有押金")
        void canViewDeposit_Admin_ReturnsTrue() {
            // given
            DepositRecord depositRecord = createHeldDeposit(
                "DEP_001", "CON_001", "LANDLORD_001", "TENANT_001",
                new BigDecimal("1000.00"));
            
            // when/then
            assertTrue(depositDomainService.canViewDeposit(depositRecord, "ADMIN_001", "ADMIN"));
        }
    }
    
    // ==================== 辅助方法 ====================
    
    /**
     * 创建持有中的押金记录
     */
    private DepositRecord createHeldDeposit(String recordId, String contractId,
                                           String landlordId, String tenantId,
                                           BigDecimal originalAmount) {
        return DepositRecord.builder()
            .recordId(recordId)
            .contractId(contractId)
            .landlordMemberId(landlordId)
            .tenantMemberId(tenantId)
            .roomId("ROOM_001")
            .houseSourceId("HOUSE_001")
            .originalAmount(originalAmount)
            .status(DepositStatus.HELD)
            .build();
    }
    
    /**
     * 创建已退还的押金记录
     */
    private DepositRecord createRefundedDeposit(String recordId, String contractId,
                                                String landlordId, String tenantId,
                                                BigDecimal originalAmount,
                                                BigDecimal deductionAmount) {
        DepositRecord record = DepositRecord.builder()
            .recordId(recordId)
            .contractId(contractId)
            .landlordMemberId(landlordId)
            .tenantMemberId(tenantId)
            .roomId("ROOM_001")
            .houseSourceId("HOUSE_001")
            .originalAmount(originalAmount)
            .status(DepositStatus.REFUNDED)
            .deductionAmount(deductionAmount)
            .actualRefundAmount(originalAmount.subtract(deductionAmount))
            .build();
        return record;
    }
}
