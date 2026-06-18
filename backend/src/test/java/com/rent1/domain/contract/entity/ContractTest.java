package com.rent1.domain.contract.entity;

import com.rent1.domain.common.ContractStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 合约实体单元测试
 */
@DisplayName("合约实体测试")
class ContractTest {

    @Test
    @DisplayName("初始化合约 - 状态应为ACTIVE")
    void initialize_ShouldSetStatusToActive() {
        // given
        Contract contract = Contract.builder()
            .contractId("CON_001")
            .tenantMemberId("TENANT_001")
            .roomId("ROOM_001")
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusYears(1))
            .build();

        // when
        contract.initialize();

        // then
        assertEquals(ContractStatus.ACTIVE, contract.getStatus());
        assertNotNull(contract.getCreateTime());
        assertNotNull(contract.getUpdateTime());
    }

    @Test
    @DisplayName("履约中合约可终止")
    void canTerminate_ActiveContract_ShouldReturnTrue() {
        // given
        Contract contract = Contract.builder()
            .status(ContractStatus.ACTIVE)
            .build();

        // when & then
        assertTrue(contract.canTerminate());
    }

    @Test
    @DisplayName("已到期合约不可终止")
    void canTerminate_ExpiredContract_ShouldReturnFalse() {
        // given
        Contract contract = Contract.builder()
            .status(ContractStatus.EXPIRED)
            .build();

        // when & then
        assertFalse(contract.canTerminate());
    }

    @Test
    @DisplayName("提前解约合约不可终止")
    void canTerminate_TerminatedEarlyContract_ShouldReturnFalse() {
        // given
        Contract contract = Contract.builder()
            .status(ContractStatus.TERMINATED_EARLY)
            .build();

        // when & then
        assertFalse(contract.canTerminate());
    }

    @Test
    @DisplayName("履约中合约可作废")
    void canVoid_ActiveContract_ShouldReturnTrue() {
        // given
        Contract contract = Contract.builder()
            .status(ContractStatus.ACTIVE)
            .build();

        // when & then
        assertTrue(contract.canVoid());
    }

    @Test
    @DisplayName("已完结合约不可作废")
    void canVoid_CompletedContract_ShouldReturnFalse() {
        // given
        Contract contract = Contract.builder()
            .status(ContractStatus.EXPIRED)
            .build();

        // when & then
        assertFalse(contract.canVoid());
    }

    @Test
    @DisplayName("已完结判定 - EXPIRED")
    void isCompleted_Expired_ShouldReturnTrue() {
        // given
        Contract contract = Contract.builder()
            .status(ContractStatus.EXPIRED)
            .build();

        // when & then
        assertTrue(contract.isCompleted());
    }

    @Test
    @DisplayName("已完结判定 - TERMINATED_EARLY")
    void isCompleted_TerminatedEarly_ShouldReturnTrue() {
        // given
        Contract contract = Contract.builder()
            .status(ContractStatus.TERMINATED_EARLY)
            .build();

        // when & then
        assertTrue(contract.isCompleted());
    }

    @Test
    @DisplayName("已完结判定 - VOID")
    void isCompleted_Void_ShouldReturnTrue() {
        // given
        Contract contract = Contract.builder()
            .status(ContractStatus.VOID)
            .build();

        // when & then
        assertTrue(contract.isCompleted());
    }

    @Test
    @DisplayName("已完结判定 - ACTIVE")
    void isCompleted_Active_ShouldReturnFalse() {
        // given
        Contract contract = Contract.builder()
            .status(ContractStatus.ACTIVE)
            .build();

        // when & then
        assertFalse(contract.isCompleted());
    }

    @Test
    @DisplayName("剩余天数计算 - 未来日期")
    void getRemainingDays_FutureDate_ShouldReturnPositive() {
        // given
        Contract contract = Contract.builder()
            .endDate(LocalDate.now().plusDays(30))
            .build();

        // when
        long remainingDays = contract.getRemainingDays();

        // then
        assertTrue(remainingDays >= 29 && remainingDays <= 30);
    }

    @Test
    @DisplayName("剩余天数计算 - 过去日期")
    void getRemainingDays_PastDate_ShouldReturnNegative() {
        // given
        Contract contract = Contract.builder()
            .endDate(LocalDate.now().minusDays(10))
            .build();

        // when
        long remainingDays = contract.getRemainingDays();

        // then
        assertTrue(remainingDays <= -10);
    }

    @Test
    @DisplayName("剩余天数计算 - 结束日期为null")
    void getRemainingDays_NullEndDate_ShouldReturnZero() {
        // given
        Contract contract = Contract.builder()
            .endDate(null)
            .build();

        // when
        long remainingDays = contract.getRemainingDays();

        // then
        assertEquals(0, remainingDays);
    }
}
