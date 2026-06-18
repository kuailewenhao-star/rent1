package com.rent1.application.deposit;

import com.rent1.domain.common.BusinessException;
import com.rent1.domain.deposit.DepositRecord;
import com.rent1.domain.deposit.DepositRepository;
import com.rent1.domain.deposit.DepositStatus;
import com.rent1.domain.invoice.entity.IncomeInvoice;
import com.rent1.domain.invoice.enums.FeeType;
import com.rent1.domain.invoice.enums.InvoiceStatus;
import com.rent1.domain.invoice.repository.IncomeInvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 押金应用服务单元测试
 * 
 * 测试范围：
 * - DEP-001: 押金全额退还
 * - DEP-002: 押金部分扣费退还
 * - DEP-003: 房东查看当前有效持有押金总额
 * - DEP-004: 租客查看当前有效押金
 */
@ExtendWith(MockitoExtension.class)
class DepositAppServiceTest {
    
    @Mock
    private DepositRepository depositRepository;
    
    @Mock
    private IncomeInvoiceRepository incomeInvoiceRepository;
    
    @InjectMocks
    private DepositAppService depositAppService;
    
    // ==================== DEP-001: 押金全额退还测试 ====================
    
    @Nested
    @DisplayName("DEP-001: 押金全额退还")
    class FullRefundTests {
        
        @Test
        @DisplayName("正常流程：房东全额退还押金成功")
        void fullRefund_Success() {
            // given
            String depositId = "DEP_001";
            String landlordId = "LANDLORD_001";
            DepositRecord depositRecord = createHeldDeposit(depositId, landlordId, "TENANT_001");
            IncomeInvoice depositInvoice = createPaidDepositInvoice("INV_DEP_001");
            
            when(depositRepository.findById(depositId)).thenReturn(Optional.of(depositRecord));
            when(incomeInvoiceRepository.findById("INV_DEP_001")).thenReturn(Optional.of(depositInvoice));
            when(depositRepository.save(any())).thenReturn(depositRecord);
            
            // when
            DepositAppService.DepositSettlementResponse response = 
                depositAppService.fullRefund(depositId, landlordId, "LANDLORD");
            
            // then
            assertNotNull(response);
            assertEquals("FULL", response.getSettlementType());
            assertEquals(DepositStatus.REFUNDED.name(), response.getStatus());
            verify(depositRepository).save(any());
        }
        
        @Test
        @DisplayName("异常流程：押金记录不存在")
        void fullRefund_DepositNotFound_ThrowsException() {
            // given
            String depositId = "DEP_NOT_EXIST";
            when(depositRepository.findById(depositId)).thenReturn(Optional.empty());
            
            // when/then
            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> depositAppService.fullRefund(depositId, "LANDLORD_001", "LANDLORD")
            );
            assertEquals("D010", exception.getCode());
        }
        
        @Test
        @DisplayName("异常流程：无权限操作他人押金")
        void fullRefund_NoPermission_ThrowsException() {
            // given
            String depositId = "DEP_001";
            DepositRecord depositRecord = createHeldDeposit(depositId, "LANDLORD_001", "TENANT_001");
            
            when(depositRepository.findById(depositId)).thenReturn(Optional.of(depositRecord));
            
            // when/then
            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> depositAppService.fullRefund(depositId, "LANDLORD_002", "LANDLORD")
            );
            assertEquals("P001", exception.getCode());
        }
        
        @Test
        @DisplayName("异常流程：租客无权限操作押金退还")
        void fullRefund_TenantNoPermission_ThrowsException() {
            // given
            String depositId = "DEP_001";
            DepositRecord depositRecord = createHeldDeposit(depositId, "LANDLORD_001", "TENANT_001");
            
            when(depositRepository.findById(depositId)).thenReturn(Optional.of(depositRecord));
            
            // when/then
            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> depositAppService.fullRefund(depositId, "TENANT_001", "TENANT")
            );
            assertEquals("P001", exception.getCode());
        }
    }
    
    // ==================== DEP-002: 押金部分扣费退还测试 ====================
    
    @Nested
    @DisplayName("DEP-002: 押金部分扣费退还")
    class PartialRefundTests {
        
        @Test
        @DisplayName("正常流程：部分扣费退还成功")
        void partialRefund_Success() {
            // given
            String depositId = "DEP_001";
            String landlordId = "LANDLORD_001";
            BigDecimal deductionAmount = new BigDecimal("300.00");
            String deductionReason = "房屋损坏赔偿";
            
            DepositRecord depositRecord = createHeldDeposit(depositId, landlordId, "TENANT_001");
            IncomeInvoice depositInvoice = createPaidDepositInvoice("INV_DEP_001");
            
            when(depositRepository.findById(depositId)).thenReturn(Optional.of(depositRecord));
            when(incomeInvoiceRepository.findById("INV_DEP_001")).thenReturn(Optional.of(depositInvoice));
            when(depositRepository.save(any())).thenReturn(depositRecord);
            
            // when
            DepositAppService.DepositSettlementResponse response = 
                depositAppService.partialRefund(depositId, deductionAmount, deductionReason, landlordId, "LANDLORD");
            
            // then
            assertNotNull(response);
            assertEquals("PARTIAL", response.getSettlementType());
            assertEquals(deductionAmount, response.getDeductionAmount());
            assertEquals(deductionReason, response.getDeductionReason());
            verify(depositRepository).save(any());
        }
        
        @Test
        @DisplayName("异常流程：扣费金额为负数")
        void partialRefund_NegativeDeduction_ThrowsException() {
            // given
            String depositId = "DEP_001";
            
            // when/then
            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> depositAppService.partialRefund(
                    depositId, new BigDecimal("-100"), "测试", "LANDLORD_001", "LANDLORD")
            );
            assertEquals("V001", exception.getCode());
        }
        
        @Test
        @DisplayName("异常流程：扣费原因为空")
        void partialRefund_EmptyReason_ThrowsException() {
            // given
            String depositId = "DEP_001";
            
            // when/then
            BusinessException exception = assertThrows(
                BusinessException.class,
                () -> depositAppService.partialRefund(
                    depositId, new BigDecimal("100"), "", "LANDLORD_001", "LANDLORD")
            );
            assertEquals("V002", exception.getCode());
        }
    }
    
    // ==================== DEP-003: 房东查看押金测试 ====================
    
    @Nested
    @DisplayName("DEP-003: 房东查看当前有效持有押金总额")
    class LandlordDepositTests {
        
        @Test
        @DisplayName("正常流程：查询房东有效押金总额")
        void getLandlordValidDeposit_Success() {
            // given
            String landlordId = "LANDLORD_001";
            BigDecimal totalDeposit = new BigDecimal("5000.00");
            int count = 3;
            
            when(depositRepository.sumValidDepositByLandlord(landlordId)).thenReturn(totalDeposit);
            when(depositRepository.countValidDepositByLandlord(landlordId)).thenReturn(count);
            
            // when
            DepositAppService.LandlordDepositSummaryResponse response = 
                depositAppService.getLandlordValidDeposit(landlordId);
            
            // then
            assertNotNull(response);
            assertEquals(totalDeposit, response.getTotalValidDeposit());
            assertEquals(count, response.getCount());
        }
        
        @Test
        @DisplayName("正常流程：房东无押金时返回0")
        void getLandlordValidDeposit_NoDeposit_ReturnsZero() {
            // given
            String landlordId = "LANDLORD_NEW";
            
            when(depositRepository.sumValidDepositByLandlord(landlordId)).thenReturn(BigDecimal.ZERO);
            when(depositRepository.countValidDepositByLandlord(landlordId)).thenReturn(0);
            
            // when
            DepositAppService.LandlordDepositSummaryResponse response = 
                depositAppService.getLandlordValidDeposit(landlordId);
            
            // then
            assertEquals(BigDecimal.ZERO, response.getTotalValidDeposit());
            assertEquals(0, response.getCount());
        }
        
        @Test
        @DisplayName("正常流程：查询房东押金明细列表")
        void getLandlordDepositList_Success() {
            // given
            String landlordId = "LANDLORD_001";
            List<DepositRecord> deposits = List.of(
                createHeldDeposit("DEP_001", landlordId, "TENANT_001"),
                createHeldDeposit("DEP_002", landlordId, "TENANT_002")
            );
            
            when(depositRepository.findAllByLandlord(landlordId)).thenReturn(deposits);
            when(incomeInvoiceRepository.findById(any())).thenReturn(Optional.empty());
            
            // when
            List<DepositAppService.DepositRecordDTO> result = 
                depositAppService.getLandlordDepositList(landlordId);
            
            // then
            assertEquals(2, result.size());
        }
    }
    
    // ==================== DEP-004: 租客查看押金测试 ====================
    
    @Nested
    @DisplayName("DEP-004: 租客查看当前有效押金")
    class TenantDepositTests {
        
        @Test
        @DisplayName("正常流程：查询租客有效押金")
        void getTenantValidDeposit_Success() {
            // given
            String tenantId = "TENANT_001";
            BigDecimal validDeposit = new BigDecimal("2000.00");
            
            when(depositRepository.sumValidDepositByTenant(tenantId)).thenReturn(validDeposit);
            
            // when
            DepositAppService.TenantDepositResponse response = 
                depositAppService.getTenantValidDeposit(tenantId);
            
            // then
            assertNotNull(response);
            assertEquals(validDeposit, response.getValidDeposit());
        }
        
        @Test
        @DisplayName("正常流程：租客无押金时返回0")
        void getTenantValidDeposit_NoDeposit_ReturnsZero() {
            // given
            String tenantId = "TENANT_NEW";
            
            when(depositRepository.sumValidDepositByTenant(tenantId)).thenReturn(BigDecimal.ZERO);
            
            // when
            DepositAppService.TenantDepositResponse response = 
                depositAppService.getTenantValidDeposit(tenantId);
            
            // then
            assertEquals(BigDecimal.ZERO, response.getValidDeposit());
        }
        
        @Test
        @DisplayName("正常流程：查询租客押金历史记录")
        void getTenantDepositList_Success() {
            // given
            String tenantId = "TENANT_001";
            List<DepositRecord> deposits = List.of(
                createHeldDeposit("DEP_001", "LANDLORD_001", tenantId),
                createRefundedDeposit("DEP_002", "LANDLORD_001", tenantId)
            );
            
            when(depositRepository.findAllByTenant(tenantId)).thenReturn(deposits);
            
            // when
            List<DepositAppService.DepositRecordDTO> result = 
                depositAppService.getTenantDepositList(tenantId);
            
            // then
            assertEquals(2, result.size());
        }
    }
    
    // ==================== 辅助方法 ====================
    
    private DepositRecord createHeldDeposit(String recordId, String landlordId, String tenantId) {
        return DepositRecord.builder()
            .recordId(recordId)
            .contractId("CON_001")
            .invoiceId("INV_DEP_001")
            .landlordMemberId(landlordId)
            .tenantMemberId(tenantId)
            .roomId("ROOM_001")
            .houseSourceId("HOUSE_001")
            .originalAmount(new BigDecimal("1000.00"))
            .status(DepositStatus.HELD)
            .build();
    }
    
    private DepositRecord createRefundedDeposit(String recordId, String landlordId, String tenantId) {
        return DepositRecord.builder()
            .recordId(recordId)
            .contractId("CON_002")
            .invoiceId("INV_DEP_002")
            .landlordMemberId(landlordId)
            .tenantMemberId(tenantId)
            .roomId("ROOM_002")
            .houseSourceId("HOUSE_001")
            .originalAmount(new BigDecimal("1000.00"))
            .deductionAmount(new BigDecimal("200.00"))
            .actualRefundAmount(new BigDecimal("800.00"))
            .deductionReason("物品损坏")
            .status(DepositStatus.REFUNDED)
            .build();
    }
    
    private IncomeInvoice createPaidDepositInvoice(String invoiceId) {
        return IncomeInvoice.builder()
            .invoiceId(invoiceId)
            .feeType(FeeType.DEPOSIT)
            .amount(new BigDecimal("1000.00"))
            .status(InvoiceStatus.DEPOSIT_RECEIVED)
            .build();
    }
}
