package com.rent1.domain.deposit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 押金记录实体单元测试
 */
class DepositRecordTest {
    
    // ==================== 初始化测试 ====================
    
    @Nested
    @DisplayName("押金记录初始化")
    class InitializeTests {
        
        @Test
        @DisplayName("初始化押金记录，状态为HELD")
        void initialize_SetsCorrectStatus() {
            // given
            DepositRecord record = new DepositRecord();
            BigDecimal originalAmount = new BigDecimal("1000.00");
            
            // when
            record.initialize(originalAmount);
            
            // then
            assertEquals(DepositStatus.HELD, record.getStatus());
            assertEquals(originalAmount, record.getOriginalAmount());
            assertEquals(BigDecimal.ZERO, record.getDeductionAmount());
            assertEquals(BigDecimal.ZERO, record.getActualRefundAmount());
            assertNotNull(record.getCreateTime());
            assertNotNull(record.getUpdateTime());
        }
    }
    
    // ==================== 全额退还测试 ====================
    
    @Nested
    @DisplayName("全额退还")
    class FullRefundTests {
        
        @Test
        @DisplayName("正常流程：全额退还成功")
        void fullRefund_Success() {
            // given
            DepositRecord record = createHeldDeposit(new BigDecimal("1000.00"));
            
            // when
            BigDecimal refundAmount = record.fullRefund();
            
            // then
            assertEquals(new BigDecimal("1000.00"), refundAmount);
            assertEquals(DepositStatus.REFUNDED, record.getStatus());
            assertEquals(new BigDecimal("1000.00"), record.getActualRefundAmount());
            assertEquals(BigDecimal.ZERO, record.getDeductionAmount());
            assertNull(record.getDeductionReason());
            assertNotNull(record.getRefundTime());
        }
        
        @Test
        @DisplayName("异常流程：已结算状态不可重复操作")
        void fullRefund_AlreadySettled_ThrowsException() {
            // given
            DepositRecord record = createRefundedDeposit(new BigDecimal("1000.00"), BigDecimal.ZERO);
            
            // when/then
            assertThrows(IllegalStateException.class, record::fullRefund);
        }
    }
    
    // ==================== 部分扣费退还测试 ====================
    
    @Nested
    @DisplayName("部分扣费退还")
    class PartialRefundTests {
        
        @Test
        @DisplayName("正常流程：部分扣费退还成功")
        void partialRefund_Success() {
            // given
            DepositRecord record = createHeldDeposit(new BigDecimal("1000.00"));
            BigDecimal deductionAmount = new BigDecimal("300.00");
            String deductionReason = "房屋损坏赔偿";
            
            // when
            BigDecimal actualRefundAmount = record.partialRefund(deductionAmount, deductionReason);
            
            // then
            assertEquals(new BigDecimal("700.00"), actualRefundAmount);
            assertEquals(DepositStatus.REFUNDED, record.getStatus());
            assertEquals(deductionAmount, record.getDeductionAmount());
            assertEquals(deductionReason, record.getDeductionReason());
            assertEquals(new BigDecimal("700.00"), record.getActualRefundAmount());
            assertNotNull(record.getRefundTime());
        }
        
        @Test
        @DisplayName("边界值：扣费金额等于押金总额，实际退还0元")
        void partialRefund_DeductionEqualsTotal_ActualRefundZero() {
            // given
            DepositRecord record = createHeldDeposit(new BigDecimal("1000.00"));
            
            // when
            BigDecimal actualRefundAmount = record.partialRefund(new BigDecimal("1000.00"), "全额扣款");
            
            // then
            assertEquals(BigDecimal.ZERO, actualRefundAmount);
        }
        
        @Test
        @DisplayName("异常流程：扣费金额超过押金总额")
        void partialRefund_DeductionExceedsTotal_ThrowsException() {
            // given
            DepositRecord record = createHeldDeposit(new BigDecimal("1000.00"));
            
            // when/then
            assertThrows(IllegalArgumentException.class,
                () -> record.partialRefund(new BigDecimal("1500.00"), "超额扣款"));
        }
        
        @Test
        @DisplayName("异常流程：已结算状态不可重复操作")
        void partialRefund_AlreadySettled_ThrowsException() {
            // given
            DepositRecord record = createRefundedDeposit(new BigDecimal("1000.00"), new BigDecimal("300.00"));
            
            // when/then
            assertThrows(IllegalStateException.class,
                () -> record.partialRefund(new BigDecimal("300.00"), "测试"));
        }
    }
    
    // ==================== 状态校验测试 ====================
    
    @Nested
    @DisplayName("状态校验")
    class StatusCheckTests {
        
        @Test
        @DisplayName("持有中状态可结算")
        void canSettle_HeldStatus_ReturnsTrue() {
            // given
            DepositRecord record = createHeldDeposit(new BigDecimal("1000.00"));
            
            // then
            assertTrue(record.canSettle());
        }
        
        @Test
        @DisplayName("已退还状态不可结算")
        void canSettle_RefundedStatus_ReturnsFalse() {
            // given
            DepositRecord record = createRefundedDeposit(new BigDecimal("1000.00"), BigDecimal.ZERO);
            
            // then
            assertFalse(record.canSettle());
        }
        
        @Test
        @DisplayName("持有中状态为有效押金")
        void isValidHeldDeposit_HeldStatus_ReturnsTrue() {
            // given
            DepositRecord record = createHeldDeposit(new BigDecimal("1000.00"));
            
            // then
            assertTrue(record.isValidHeldDeposit());
        }
        
        @Test
        @DisplayName("已退还状态不是有效押金")
        void isValidHeldDeposit_RefundedStatus_ReturnsFalse() {
            // given
            DepositRecord record = createRefundedDeposit(new BigDecimal("1000.00"), BigDecimal.ZERO);
            
            // then
            assertFalse(record.isValidHeldDeposit());
        }
    }
    
    // ==================== 辅助方法 ====================
    
    private DepositRecord createHeldDeposit(BigDecimal originalAmount) {
        return DepositRecord.builder()
            .recordId("DEP_001")
            .contractId("CON_001")
            .landlordMemberId("LANDLORD_001")
            .tenantMemberId("TENANT_001")
            .roomId("ROOM_001")
            .houseSourceId("HOUSE_001")
            .originalAmount(originalAmount)
            .status(DepositStatus.HELD)
            .build();
    }
    
    private DepositRecord createRefundedDeposit(BigDecimal originalAmount, BigDecimal deductionAmount) {
        return DepositRecord.builder()
            .recordId("DEP_001")
            .contractId("CON_001")
            .landlordMemberId("LANDLORD_001")
            .tenantMemberId("TENANT_001")
            .roomId("ROOM_001")
            .houseSourceId("HOUSE_001")
            .originalAmount(originalAmount)
            .deductionAmount(deductionAmount)
            .actualRefundAmount(originalAmount.subtract(deductionAmount))
            .status(DepositStatus.REFUNDED)
            .build();
    }
}
