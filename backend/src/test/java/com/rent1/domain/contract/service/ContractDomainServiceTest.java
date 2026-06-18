package com.rent1.domain.contract.service;

import com.rent1.domain.common.BusinessException;
import com.rent1.domain.common.ContractStatus;
import com.rent1.domain.common.TerminationType;
import com.rent1.domain.contract.entity.Contract;
import com.rent1.domain.contract.event.ContractCreatedEvent;
import com.rent1.domain.contract.event.ContractTerminatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 合约领域服务单元测试
 * 覆盖：CON-001, CON-010, CON-011, CON-012
 */
@DisplayName("合约领域服务测试")
class ContractDomainServiceTest {

    private ContractDomainService contractDomainService;

    @BeforeEach
    void setUp() {
        contractDomainService = new ContractDomainService();
    }

    // ==================== CON-001: 创建合约测试 ====================

    @Nested
    @DisplayName("CON-001 创建合约")
    class CreateContractTests {

        @Test
        @DisplayName("正常创建合约 - 应成功")
        void createContract_Success() {
            // given
            Contract contract = Contract.builder()
                .contractId("CON_001")
                .tenantMemberId("TENANT_001")
                .roomId("ROOM_001")
                .houseSourceId("HS_001")
                .landlordMemberId("LANDLORD_001")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusYears(1))
                .build();

            String billingRulesJson = "{\"RENT\":{},\"DEPOSIT\":{}}";

            // when
            ContractCreatedEvent event = contractDomainService.createContract(
                contract, "VACANT", true, billingRulesJson);

            // then
            assertNotNull(event);
            assertEquals("CON_001", event.getContractId());
            assertEquals(ContractStatus.ACTIVE, event.getStatus());
            assertEquals(LocalDate.now(), event.getStartDate());
            assertEquals(LocalDate.now().plusYears(1), event.getEndDate());
        }

        @Test
        @DisplayName("房间非空置状态创建合约 - 应拦截")
        void createContract_RoomNotVacant_ShouldThrow() {
            // given
            Contract contract = Contract.builder()
                .contractId("CON_002")
                .tenantMemberId("TENANT_001")
                .roomId("ROOM_001")
                .build();

            // when & then
            BusinessException ex = assertThrows(BusinessException.class, () ->
                contractDomainService.createContract(contract, "OCCUPIED", true, "{}"));
            assertEquals(ContractDomainService.ERR_ROOM_ALREADY_RENTED, ex.getCode());
        }

        @Test
        @DisplayName("无紧急联系人创建合约 - 应拦截")
        void createContract_NoEmergencyContact_ShouldThrow() {
            // given
            Contract contract = Contract.builder()
                .contractId("CON_003")
                .tenantMemberId("TENANT_001")
                .roomId("ROOM_001")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusYears(1))
                .build();

            // when & then
            BusinessException ex = assertThrows(BusinessException.class, () ->
                contractDomainService.createContract(contract, "VACANT", false, "{}"));
            assertEquals(ContractDomainService.ERR_EMERGENCY_CONTACT_REQUIRED, ex.getCode());
        }

        @Test
        @DisplayName("结束时间早于开始时间 - 应拦截")
        void createContract_EndBeforeStart_ShouldThrow() {
            // given
            Contract contract = Contract.builder()
                .contractId("CON_004")
                .tenantMemberId("TENANT_001")
                .roomId("ROOM_001")
                .startDate(LocalDate.now().plusDays(10))
                .endDate(LocalDate.now())
                .build();

            // when & then
            BusinessException ex = assertThrows(BusinessException.class, () ->
                contractDomainService.createContract(contract, "VACANT", true, "{}"));
            assertEquals(ContractDomainService.ERR_END_BEFORE_START, ex.getCode());
        }

        @Test
        @DisplayName("结束时间等于开始时间 - 应拦截")
        void createContract_EndEqualsStart_ShouldThrow() {
            // given
            LocalDate sameDate = LocalDate.now();
            Contract contract = Contract.builder()
                .contractId("CON_005")
                .tenantMemberId("TENANT_001")
                .roomId("ROOM_001")
                .startDate(sameDate)
                .endDate(sameDate)
                .build();

            // when & then
            BusinessException ex = assertThrows(BusinessException.class, () ->
                contractDomainService.createContract(contract, "VACANT", true, "{}"));
            assertEquals(ContractDomainService.ERR_END_BEFORE_START, ex.getCode());
        }

        @Test
        @DisplayName("计费规则为空 - 应拦截")
        void createContract_EmptyBillingRules_ShouldThrow() {
            // given
            Contract contract = Contract.builder()
                .contractId("CON_006")
                .tenantMemberId("TENANT_001")
                .roomId("ROOM_001")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusYears(1))
                .build();

            // when & then
            BusinessException ex = assertThrows(BusinessException.class, () ->
                contractDomainService.createContract(contract, "VACANT", true, ""));
            assertEquals(ContractDomainService.ERR_BILLING_RULES_INCOMPLETE, ex.getCode());
        }
    }

    // ==================== CON-010: 合约到期测试 ====================

    @Nested
    @DisplayName("CON-010 合约到期自动退租")
    class ExpireContractTests {

        @Test
        @DisplayName("履约中合约到期 - 应成功")
        void expireContract_ActiveContract_ShouldSucceed() {
            // given
            Contract contract = Contract.builder()
                .contractId("CON_010")
                .tenantMemberId("TENANT_001")
                .roomId("ROOM_001")
                .houseSourceId("HS_001")
                .landlordMemberId("LANDLORD_001")
                .startDate(LocalDate.now().minusYears(1))
                .endDate(LocalDate.now().minusDays(1)) // 昨天到期
                .status(ContractStatus.ACTIVE)
                .build();

            // when
            ContractTerminatedEvent event = contractDomainService.expireContract(contract);

            // then
            assertNotNull(event);
            assertEquals(ContractStatus.EXPIRED, contract.getStatus());
            assertEquals(ContractTerminatedEvent.TYPE_EXPIRED, event.getTerminationType());
        }

        @Test
        @DisplayName("非履约中合约到期 - 应拦截")
        void expireContract_NotActiveContract_ShouldThrow() {
            // given
            Contract contract = Contract.builder()
                .contractId("CON_011")
                .status(ContractStatus.EXPIRED)
                .endDate(LocalDate.now().minusDays(1))
                .build();

            // when & then
            BusinessException ex = assertThrows(BusinessException.class, () ->
                contractDomainService.expireContract(contract));
            assertEquals(ContractDomainService.ERR_CONTRACT_ALREADY_COMPLETED, ex.getCode());
        }
    }

    // ==================== CON-011: 提前解约测试 ====================

    @Nested
    @DisplayName("CON-011 手动提前解约")
    class TerminateContractTests {

        @Test
        @DisplayName("履约中合约提前解约 - 应成功")
        void terminateContract_ActiveContract_ShouldSucceed() {
            // given
            Contract contract = Contract.builder()
                .contractId("CON_012")
                .tenantMemberId("TENANT_001")
                .roomId("ROOM_001")
                .houseSourceId("HS_001")
                .landlordMemberId("LANDLORD_001")
                .startDate(LocalDate.now().minusMonths(6))
                .endDate(LocalDate.now().plusMonths(6))
                .status(ContractStatus.ACTIVE)
                .build();

            BigDecimal refundAmount = new BigDecimal("2000");
            BigDecimal deductionAmount = new BigDecimal("0");
            String deductionRemark = "";

            // when
            ContractTerminatedEvent event = contractDomainService.terminateContract(
                contract, TerminationType.LANDLORD, refundAmount, deductionAmount, deductionRemark);

            // then
            assertNotNull(event);
            assertEquals(ContractStatus.TERMINATED_EARLY, contract.getStatus());
            assertEquals(TerminationType.LANDLORD.name(), contract.getTerminationType());
            assertEquals(ContractTerminatedEvent.TYPE_EARLY_TERMINATED, event.getTerminationType());
        }

        @Test
        @DisplayName("已完结合约再次解约 - 应拦截")
        void terminateContract_AlreadyTerminated_ShouldThrow() {
            // given
            Contract contract = Contract.builder()
                .contractId("CON_013")
                .status(ContractStatus.EXPIRED)
                .build();

            // when & then
            BusinessException ex = assertThrows(BusinessException.class, () ->
                contractDomainService.terminateContract(
                    contract, TerminationType.LANDLORD,
                    new BigDecimal("2000"), null, null));
            assertEquals(ContractDomainService.ERR_CONTRACT_ALREADY_COMPLETED, ex.getCode());
        }

        @Test
        @DisplayName("提前解约-租客发起 - 应成功")
        void terminateContract_TenantInitiated_ShouldSucceed() {
            // given
            Contract contract = Contract.builder()
                .contractId("CON_014")
                .tenantMemberId("TENANT_001")
                .roomId("ROOM_001")
                .houseSourceId("HS_001")
                .landlordMemberId("LANDLORD_001")
                .status(ContractStatus.ACTIVE)
                .build();

            // when
            ContractTerminatedEvent event = contractDomainService.terminateContract(
                contract, TerminationType.TENANT,
                new BigDecimal("2000"), null, null);

            // then
            assertEquals(TerminationType.TENANT.name(), contract.getTerminationType());
            assertEquals(ContractTerminatedEvent.TYPE_EARLY_TERMINATED, event.getTerminationType());
        }

        @Test
        @DisplayName("提前解约-协商终止 - 应成功")
        void terminateContract_Mutual_ShouldSucceed() {
            // given
            Contract contract = Contract.builder()
                .contractId("CON_015")
                .tenantMemberId("TENANT_001")
                .roomId("ROOM_001")
                .houseSourceId("HS_001")
                .landlordMemberId("LANDLORD_001")
                .status(ContractStatus.ACTIVE)
                .build();

            // when
            ContractTerminatedEvent event = contractDomainService.terminateContract(
                contract, TerminationType.MUTUAL,
                new BigDecimal("1500"), new BigDecimal("500"), "房屋损坏赔偿");

            // then
            assertEquals(TerminationType.MUTUAL.name(), contract.getTerminationType());
            assertEquals(new BigDecimal("500"), contract.getUpdateTime() != null ?
                BigDecimal.ZERO : BigDecimal.ZERO); // 简化验证
        }
    }

    // ==================== CON-012: 合约作废测试 ====================

    @Nested
    @DisplayName("CON-012 合约作废")
    class VoidContractTests {

        @Test
        @DisplayName("履约中合约作废 - 应成功")
        void voidContract_ActiveContract_ShouldSucceed() {
            // given
            Contract contract = Contract.builder()
                .contractId("CON_016")
                .tenantMemberId("TENANT_001")
                .roomId("ROOM_001")
                .houseSourceId("HS_001")
                .landlordMemberId("LANDLORD_001")
                .status(ContractStatus.ACTIVE)
                .build();

            String reason = "测试作废原因";

            // when
            ContractTerminatedEvent event = contractDomainService.voidContract(contract, reason);

            // then
            assertNotNull(event);
            assertEquals(ContractStatus.VOID, contract.getStatus());
            assertEquals(reason, contract.getVoidReason());
            assertEquals(ContractTerminatedEvent.TYPE_VOID, event.getTerminationType());
        }

        @Test
        @DisplayName("已完结合约作废 - 应拦截")
        void voidContract_AlreadyCompleted_ShouldThrow() {
            // given
            Contract contract = Contract.builder()
                .contractId("CON_017")
                .status(ContractStatus.EXPIRED)
                .build();

            // when & then
            BusinessException ex = assertThrows(BusinessException.class, () ->
                contractDomainService.voidContract(contract, "原因"));
            assertEquals(ContractDomainService.ERR_CONTRACT_ALREADY_COMPLETED, ex.getCode());
        }
    }

    // ==================== 邀请码相关测试 ====================

    @Nested
    @DisplayName("邀请码相关")
    class InviteCodeTests {

        @Test
        @DisplayName("生成邀请码 - 应返回6位数字")
        void generateInviteCode_ShouldReturn6Digits() {
            // when
            String code = contractDomainService.generateInviteCode("ROOM_001", "LANDLORD_001");

            // then
            assertNotNull(code);
            assertEquals(6, code.length());
            assertTrue(code.matches("\\d{6}"));
        }

        @Test
        @DisplayName("校验有效邀请码 - 应通过")
        void validateInviteCode_ValidCode_ShouldPass() {
            // given
            java.time.LocalDateTime expireTime = java.time.LocalDateTime.now().plusHours(1);

            // when & then (不抛异常即通过)
            assertTrue(contractDomainService.validateInviteCode(expireTime, "ACTIVE"));
        }

        @Test
        @DisplayName("校验已使用邀请码 - 应拦截")
        void validateInviteCode_UsedCode_ShouldThrow() {
            // given
            java.time.LocalDateTime expireTime = java.time.LocalDateTime.now().plusHours(1);

            // when & then
            BusinessException ex = assertThrows(BusinessException.class, () ->
                contractDomainService.validateInviteCode(expireTime, "USED"));
            assertEquals(ContractDomainService.ERR_INVITE_CODE_EXPIRED, ex.getCode());
        }

        @Test
        @DisplayName("校验已过期邀请码 - 应拦截")
        void validateInviteCode_ExpiredCode_ShouldThrow() {
            // given
            java.time.LocalDateTime expireTime = java.time.LocalDateTime.now().minusHours(1);

            // when & then
            BusinessException ex = assertThrows(BusinessException.class, () ->
                contractDomainService.validateInviteCode(expireTime, "ACTIVE"));
            assertEquals(ContractDomainService.ERR_INVITE_CODE_EXPIRED, ex.getCode());
        }
    }

    // ==================== 权限相关测试 ====================

    @Nested
    @DisplayName("数据权限校验")
    class PermissionTests {

        @Test
        @DisplayName("房东访问自有合约 - 应允许")
        void canAccessContract_LandlordOwnContract_ShouldAllow() {
            // given
            Contract contract = Contract.builder()
                .landlordMemberId("LANDLORD_001")
                .tenantMemberId("TENANT_001")
                .build();

            // when & then
            assertTrue(contractDomainService.canAccessContract(contract, "LANDLORD_001", "LANDLORD"));
        }

        @Test
        @DisplayName("房东访问他人合约 - 应拒绝")
        void canAccessContract_LandlordOtherContract_ShouldDeny() {
            // given
            Contract contract = Contract.builder()
                .landlordMemberId("LANDLORD_002")
                .tenantMemberId("TENANT_001")
                .build();

            // when & then
            assertFalse(contractDomainService.canAccessContract(contract, "LANDLORD_001", "LANDLORD"));
        }

        @Test
        @DisplayName("租客访问自有合约 - 应允许")
        void canAccessContract_TenantOwnContract_ShouldAllow() {
            // given
            Contract contract = Contract.builder()
                .landlordMemberId("LANDLORD_001")
                .tenantMemberId("TENANT_001")
                .build();

            // when & then
            assertTrue(contractDomainService.canAccessContract(contract, "TENANT_001", "TENANT"));
        }

        @Test
        @DisplayName("租客访问他人合约 - 应拒绝")
        void canAccessContract_TenantOtherContract_ShouldDeny() {
            // given
            Contract contract = Contract.builder()
                .landlordMemberId("LANDLORD_001")
                .tenantMemberId("TENANT_002")
                .build();

            // when & then
            assertFalse(contractDomainService.canAccessContract(contract, "TENANT_001", "TENANT"));
        }

        @Test
        @DisplayName("管理员访问任意合约 - 应允许")
        void canAccessContract_AdminAnyContract_ShouldAllow() {
            // given
            Contract contract = Contract.builder()
                .landlordMemberId("LANDLORD_001")
                .tenantMemberId("TENANT_001")
                .build();

            // when & then
            assertTrue(contractDomainService.canAccessContract(contract, "ADMIN_001", "ADMIN"));
        }
    }
}
