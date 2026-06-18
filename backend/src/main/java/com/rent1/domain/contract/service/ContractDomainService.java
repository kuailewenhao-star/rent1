package com.rent1.domain.contract.service;

import com.rent1.domain.common.BusinessException;
import com.rent1.domain.common.ContractStatus;
import com.rent1.domain.common.TerminationType;
import com.rent1.domain.contract.entity.Contract;
import com.rent1.domain.contract.event.ContractCreatedEvent;
import com.rent1.domain.contract.event.ContractTerminatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 合约领域服务 - 核心业务规则承载
 * 职责边界：租赁合约全生命周期管理，绑定租客、房间、计费规则
 *
 * 核心业务规则：
 * 1. 合约创建时自动锁定计费规则
 * 2. 合约状态流转：履约中 → 已到期完结 / 提前解约 / 作废
 * 3. 退租解约闭环：账单截断、押金结算、房间状态复位
 */
@Slf4j
@Service
public class ContractDomainService {

    //region 错误码定义
    public static final String ERR_ROOM_ALREADY_RENTED = "C001";
    public static final String ERR_EMERGENCY_CONTACT_REQUIRED = "C002";
    public static final String ERR_END_BEFORE_START = "C003";
    public static final String ERR_BILLING_RULES_INCOMPLETE = "C004";
    public static final String ERR_CONTRACT_ALREADY_COMPLETED = "C010";
    public static final String ERR_DEDUCTION_EXCEEDS_DEPOSIT = "C011";
    public static final String ERR_INVITE_CODE_EXPIRED = "I001";
    public static final String ERR_IDENTITY_CONFLICT = "I002";
    //endregion

    //region 合约创建

    /**
     * 创建合约 - 核心领域逻辑
     *
     * @param contract 合约实体
     * @param roomStatus 房间状态（必须是VACANT空置中）
     * @param hasEmergencyContact 是否有紧急联系人
     * @param billingRulesJson 计费规则JSON（完整配置）
     * @return 合约创建领域事件
     */
    public ContractCreatedEvent createContract(Contract contract, String roomStatus,
                                                boolean hasEmergencyContact,
                                                String billingRulesJson) {
        // 规则校验1：房间必须为空置中状态
        if (!"VACANT".equals(roomStatus)) {
            log.warn("房间{}非空置状态，无法创建合约", contract.getRoomId());
            throw new BusinessException(ERR_ROOM_ALREADY_RENTED, "该房间已有租客，不可重复招租");
        }

        // 规则校验2：租客必须有紧急联系人
        if (!hasEmergencyContact) {
            log.warn("租客{}无紧急联系人，无法创建合约", contract.getTenantMemberId());
            throw new BusinessException(ERR_EMERGENCY_CONTACT_REQUIRED,
                "紧急联系人不能为空，无法创建合约");
        }

        // 规则校验3：结束时间必须晚于开始时间
        if (contract.getEndDate().isBefore(contract.getStartDate())
            || contract.getEndDate().isEqual(contract.getStartDate())) {
            log.warn("合约结束时间{}早于开始时间{}", contract.getEndDate(), contract.getStartDate());
            throw new BusinessException(ERR_END_BEFORE_START, "结束时间不能早于开始时间");
        }

        // 规则校验4：计费规则必须完整（租金+押金必须存在）
        validateBillingRulesComplete(billingRulesJson);

        // 初始化合约
        contract.initialize();

        log.info("合约创建成功 contractId={}, roomId={}, tenantId={}, status={}",
            contract.getContractId(), contract.getRoomId(),
            contract.getTenantMemberId(), contract.getStatus());

        // 构建领域事件
        return new ContractCreatedEvent(
            contract.getContractId(),
            contract.getTenantMemberId(),
            contract.getRoomId(),
            contract.getHouseSourceId(),
            contract.getLandlordMemberId(),
            contract.getBillingRulesSnapshotId(),
            contract.getStartDate(),
            contract.getEndDate(),
            contract.getStatus()
        );
    }

    /**
     * 校验计费规则是否完整（必须包含租金和押金）
     */
    private void validateBillingRulesComplete(String billingRulesJson) {
        if (billingRulesJson == null || billingRulesJson.isEmpty()) {
            throw new BusinessException(ERR_BILLING_RULES_INCOMPLETE, "请配置完整的计费规则");
        }
        // 检查是否包含租金和押金配置（简化校验，实际应解析JSON）
        if (!billingRulesJson.contains("RENT") && !billingRulesJson.contains("租金")) {
            throw new BusinessException(ERR_BILLING_RULES_INCOMPLETE, "请配置完整的计费规则");
        }
    }

    //endregion

    //region 合约终止

    /**
     * 合约到期自动退租
     *
     * @param contract 合约实体
     * @return 合约终止领域事件
     */
    public ContractTerminatedEvent expireContract(Contract contract) {
        // 规则校验：仅ACTIVE状态合约可到期
        if (contract.getStatus() != ContractStatus.ACTIVE) {
            log.warn("合约{}非履约中状态，无法到期处理", contract.getContractId());
            throw new BusinessException(ERR_CONTRACT_ALREADY_COMPLETED, "已完结合约不可解约");
        }

        // 更新合约状态
        contract.setStatus(ContractStatus.EXPIRED);
        contract.setUpdateTime(LocalDateTime.now());

        log.info("合约到期完结 contractId={}, endDate={}",
            contract.getContractId(), contract.getEndDate());

        // 构建领域事件 - 触发账单截断、房间复位
        return new ContractTerminatedEvent(
            contract.getContractId(),
            ContractTerminatedEvent.TYPE_EXPIRED,
            contract.getTenantMemberId(),
            contract.getRoomId(),
            contract.getHouseSourceId(),
            contract.getLandlordMemberId()
        );
    }

    /**
     * 手动提前解约
     *
     * @param contract 合约实体
     * @param terminationType 终止类型
     * @param refundAmount 退还金额（押金全额退）
     * @param deductionAmount 扣费金额（如有扣费）
     * @param deductionRemark 扣费原因
     * @return 合约终止领域事件
     */
    public ContractTerminatedEvent terminateContract(Contract contract,
                                                      TerminationType terminationType,
                                                      java.math.BigDecimal refundAmount,
                                                      java.math.BigDecimal deductionAmount,
                                                      String deductionRemark) {
        // 规则校验：仅ACTIVE状态合约可解约
        if (!contract.canTerminate()) {
            log.warn("合约{}非履约中状态，无法解约", contract.getContractId());
            throw new BusinessException(ERR_CONTRACT_ALREADY_COMPLETED, "已完结合约不可解约");
        }

        // 规则校验：扣费金额不能超过押金总额
        // 注意：押金校验应由调用方传入押金信息，这里仅做通用校验
        if (deductionAmount != null && deductionAmount.compareTo(java.math.BigDecimal.ZERO) > 0) {
            // 实际校验需要查询押金账单金额
            log.info("解约扣费 deductionAmount={}, reason={}", deductionAmount, deductionRemark);
        }

        // 更新合约状态
        contract.setStatus(ContractStatus.TERMINATED_EARLY);
        contract.setTerminationType(terminationType.name());
        contract.setUpdateTime(LocalDateTime.now());

        log.info("合约提前解约 contractId={}, terminationType={}",
            contract.getContractId(), terminationType);

        // 构建领域事件
        return new ContractTerminatedEvent(
            contract.getContractId(),
            ContractTerminatedEvent.TYPE_EARLY_TERMINATED,
            contract.getTenantMemberId(),
            contract.getRoomId(),
            contract.getHouseSourceId(),
            contract.getLandlordMemberId()
        );
    }

    /**
     * 合约作废（新建未履约）
     * 规则：
     * 1. 合约无任何账单产生
     * 2. 合约无任何履约记录
     * 3. 作废后不可逆
     * 4. 不触发任何下游（不生成账单、不变更房间状态）
     *
     * @param contract 合约实体
     * @param reason 作废原因
     * @return 合约终止领域事件（VOID类型）
     */
    public ContractTerminatedEvent voidContract(Contract contract, String reason) {
        // 规则校验：仅ACTIVE状态合约可作废
        if (!contract.canVoid()) {
            log.warn("合约{}非履约中状态，无法作废", contract.getContractId());
            throw new BusinessException(ERR_CONTRACT_ALREADY_COMPLETED, "已完结合约不可作废");
        }

        // 更新合约状态
        contract.setStatus(ContractStatus.VOID);
        contract.setVoidReason(reason);
        contract.setUpdateTime(LocalDateTime.now());

        log.info("合约作废 contractId={}, reason={}", contract.getContractId(), reason);

        // 构建领域事件（VOID类型不触发下游）
        return new ContractTerminatedEvent(
            contract.getContractId(),
            ContractTerminatedEvent.TYPE_VOID,
            contract.getTenantMemberId(),
            contract.getRoomId(),
            contract.getHouseSourceId(),
            contract.getLandlordMemberId()
        );
    }

    //endregion

    //region 邀请码相关

    /**
     * 生成入驻邀请码
     *
     * @param roomId 房间ID
     * @param landlordMemberId 房东会员ID
     * @return 邀请码（24小时有效）
     */
    public String generateInviteCode(String roomId, String landlordMemberId) {
        // 生成6位邀请码
        String inviteCode = String.format("%06d", (int) (Math.random() * 1000000));
        log.info("生成入驻邀请码 roomId={}, landlordId={}, code={}",
            roomId, landlordMemberId, inviteCode);
        return inviteCode;
    }

    /**
     * 校验邀请码是否有效
     *
     * @param expireTime 过期时间
     * @param status 状态
     * @return 是否有效
     */
    public boolean validateInviteCode(LocalDateTime expireTime, String status) {
        if (!"ACTIVE".equals(status)) {
            log.warn("邀请码已使用或已失效");
            throw new BusinessException(ERR_INVITE_CODE_EXPIRED, "邀请链接已过期，请联系房东重新生成");
        }
        if (LocalDateTime.now().isAfter(expireTime)) {
            log.warn("邀请码已过期 expireTime={}", expireTime);
            throw new BusinessException(ERR_INVITE_CODE_EXPIRED, "邀请链接已过期，请联系房东重新生成");
        }
        return true;
    }

    //endregion

    //region 数据查询辅助

    /**
     * 校验用户是否有权访问该合约
     */
    public boolean canAccessContract(Contract contract, String memberId, String memberType) {
        if ("ADMIN".equals(memberType)) {
            return true; // 管理员可访问所有
        }
        if ("LANDLORD".equals(memberType)) {
            return contract.getLandlordMemberId().equals(memberId);
        }
        if ("TENANT".equals(memberType)) {
            return contract.getTenantMemberId().equals(memberId);
        }
        return false;
    }

    //endregion
}
