package com.rent1.application.contract.service;

import com.rent1.domain.common.BusinessException;
import com.rent1.domain.common.ContractStatus;
import com.rent1.domain.common.TerminationType;
import com.rent1.domain.contract.entity.ContractBillingRulesSnapshot;
import com.rent1.domain.contract.entity.Contract;
import com.rent1.domain.contract.entity.RoomInvite;
import com.rent1.domain.contract.event.ContractCreatedEvent;
import com.rent1.domain.contract.event.ContractTerminatedEvent;
import com.rent1.domain.contract.repository.ContractBillingRulesSnapshotRepository;
import com.rent1.domain.contract.repository.ContractRepository;
import com.rent1.domain.contract.repository.RoomInviteRepository;
import com.rent1.domain.contract.service.ContractDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 合约应用服务 - 业务流程编排、跨领域调度、事务控制
 *
 * Saga编排合约创建全流程：
 * 1. 校验：房间为空置中、租客手机号有效、开始时间<结束时间
 * 2. 校验：当前会员至少1条紧急联系人
 * 3. 创建合约记录，status=ACTIVE
 * 4. 触发计费规则锁定（BillingRulesLockedEvent）
 * 5. 触发房间状态变更（空置中→已出租）
 * 6. 生成首期租金账单+押金账单
 * 7. 发布ContractCreatedEvent
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContractAppService {

    private final ContractDomainService contractDomainService;
    private final ContractRepository contractRepository;
    private final ContractBillingRulesSnapshotRepository billingRulesSnapshotRepository;
    private final RoomInviteRepository roomInviteRepository;

    // 依赖外部领域服务（实际应通过事件或依赖注入，这里简化处理）
    // private final BillingRulesDomainService billingRulesDomainService;
    // private final RoomStatusDomainService roomStatusDomainService;
    // private final InvoiceDomainService invoiceDomainService;

    //region CON-001: 房东手动创建租客合约

    /**
     * 房东手动创建租客合约 - Saga编排
     *
     * @param landlordMemberId 房东会员ID
     * @param roomId 房间ID
     * @param tenantPhone 租客手机号
     * @param realName 租客真实姓名（可选）
     * @param startDate 合约开始日期
     * @param endDate 合约结束日期
     * @param billingRulesJson 计费规则JSON
     * @param paperContractUrl 纸质合约URL（可选）
     * @return 创建结果
     */
    @Transactional
    public ContractCreateResult createContractByLandlord(String landlordMemberId,
                                                         String roomId,
                                                         String tenantPhone,
                                                         String realName,
                                                         LocalDate startDate,
                                                         LocalDate endDate,
                                                         String billingRulesJson,
                                                         String paperContractUrl) {
        log.info("房东创建合约 landlordId={}, roomId={}, tenantPhone={}",
            landlordMemberId, roomId, tenantPhone);

        // 步骤1：查询租客会员ID（根据手机号查找或创建）
        String tenantMemberId = findOrCreateTenantMember(tenantPhone, realName);

        // 步骤2：查询房间信息（简化处理，实际需调用RoomRepository）
        String houseSourceId = "HS_" + roomId.split("_")[1]; // 简化
        String roomStatus = "VACANT"; // 简化，实际应查询

        // 步骤3：校验紧急联系人（简化处理，实际需调用MemberContactRepository）
        boolean hasEmergencyContact = true; // 简化，实际应校验

        // 步骤4：生成合约ID和快照ID
        String contractId = generateContractId();
        String snapshotId = generateSnapshotId();

        // 步骤5：构建合约实体
        Contract contract = Contract.builder()
            .contractId(contractId)
            .tenantMemberId(tenantMemberId)
            .roomId(roomId)
            .houseSourceId(houseSourceId)
            .landlordMemberId(landlordMemberId)
            .billingRulesSnapshotId(snapshotId)
            .startDate(startDate)
            .endDate(endDate)
            .status(ContractStatus.ACTIVE)
            .paperContractUrl(paperContractUrl)
            .build();

        // 步骤6：调用领域服务创建合约（核心业务规则校验）
        ContractCreatedEvent event = contractDomainService.createContract(
            contract, roomStatus, hasEmergencyContact, billingRulesJson);

        // 步骤7：保存合约
        contractRepository.save(contract);

        // 步骤8：创建计费规则快照并锁定
        ContractBillingRulesSnapshot snapshot = new ContractBillingRulesSnapshot();
        snapshot.setSnapshotId(snapshotId);
        snapshot.setContractId(contractId);
        snapshot.setRulesJson(billingRulesJson);
        snapshot.setLockedAt(LocalDateTime.now());
        snapshot.setCreateTime(LocalDateTime.now());
        billingRulesSnapshotRepository.save(snapshot);

        // 步骤9：触发房间状态变更（发布领域事件，由房间领域服务消费）
        // occupyRoom(roomId); // 通过事件驱动

        // 步骤10：生成首期账单（发布领域事件，由账单领域服务消费）
        // generateFirstBill(contractId); // 通过事件驱动

        log.info("合约创建完成 contractId={}, tenantId={}, roomId={}",
            contractId, tenantMemberId, roomId);

        return ContractCreateResult.builder()
            .contractId(contractId)
            .status(contract.getStatus())
            .roomId(roomId)
            .tenantMemberId(tenantMemberId)
            .billingRulesSnapshotId(snapshotId)
            .firstBillIds(List.of()) // 实际由账单域返回
            .build();
    }

    //endregion

    //region CON-002: 租客自助入驻

    /**
     * 生成入驻邀请码
     *
     * @param roomId 房间ID
     * @param landlordMemberId 房东会员ID
     * @return 邀请码信息
     */
    @Transactional
    public InviteCodeResult generateInviteCode(String roomId, String landlordMemberId) {
        log.info("生成入驻邀请码 roomId={}, landlordId={}", roomId, landlordMemberId);

        // 生成邀请码
        String inviteCode = contractDomainService.generateInviteCode(roomId, landlordMemberId);

        // 保存邀请记录
        RoomInvite invite = RoomInvite.builder()
            .inviteId(UUID.randomUUID().toString())
            .roomId(roomId)
            .inviteCode(inviteCode)
            .landlordMemberId(landlordMemberId)
            .expireTime(LocalDateTime.now().plusHours(24))
            .status("ACTIVE")
            .createTime(LocalDateTime.now())
            .build();
        roomInviteRepository.save(invite);

        return InviteCodeResult.builder()
            .inviteCode(inviteCode)
            .expireTime(invite.getExpireTime())
            .build();
    }

    /**
     * 租客自助入驻确认
     *
     * @param inviteCode 邀请码
     * @param tenantPhone 租客手机号
     * @param realName 真实姓名
     * @param idCard 身份证号
     * @param confirmed 是否确认入驻
     * @return 入驻结果
     */
    @Transactional
    public ContractCreateResult selfCheckin(String inviteCode,
                                             String tenantPhone,
                                             String realName,
                                             String idCard,
                                             boolean confirmed) {
        log.info("租客自助入驻 inviteCode={}, tenantPhone={}", inviteCode, tenantPhone);

        // 步骤1：校验邀请码
        RoomInvite invite = roomInviteRepository.findByInviteCode(inviteCode)
            .orElseThrow(() -> new BusinessException(ContractDomainService.ERR_INVITE_CODE_EXPIRED,
                "邀请链接已过期，请联系房东重新生成"));

        contractDomainService.validateInviteCode(invite.getExpireTime(), invite.getStatus());

        // 步骤2：校验身份冲突（简化处理）
        // 实际需检查：如果当前用户已有LANDLORD身份，需切换为TENANT

        // 步骤3：完善租客信息
        String tenantMemberId = findOrCreateTenantMember(tenantPhone, realName);

        // 步骤4：更新邀请状态
        invite.setStatus("USED");
        invite.setUsedByTenantId(tenantMemberId);
        roomInviteRepository.update(invite);

        // 步骤5：创建合约（简化：实际应获取房间计费规则）
        // 这里应调用完整的合约创建流程
        String billingRulesJson = "{}"; // 简化

        ContractCreateResult result = createContractByLandlord(
            invite.getLandlordMemberId(),
            invite.getRoomId(),
            tenantPhone,
            realName,
            LocalDate.now(), // 简化
            LocalDate.now().plusYears(1), // 简化
            billingRulesJson,
            null
        );

        return result;
    }

    //endregion

    //region CON-010: 合约到期自动退租处理（定时任务调用）

    /**
     * 合约到期自动退租处理
     * 每日00:00定时扫描执行
     *
     * @param contractId 合约ID
     * @return 终止结果
     */
    @Transactional
    public TerminationResult expireContract(String contractId) {
        log.info("执行合约到期退租 contractId={}", contractId);

        Contract contract = contractRepository.findById(contractId)
            .orElseThrow(() -> new BusinessException("C010", "合约不存在"));

        ContractTerminatedEvent event = contractDomainService.expireContract(contract);

        contractRepository.update(contract);

        // 发布领域事件，触发账单截断、房间复位等
        publishTerminationEvent(event);

        return TerminationResult.builder()
            .contractId(contractId)
            .terminationType(event.getTerminationType())
            .build();
    }

    /**
     * 批量处理到期合约（定时任务批量执行）
     */
    @Transactional
    public int batchExpireContracts(LocalDate date) {
        List<Contract> expiredContracts = contractRepository.findExpiredContracts(date);
        log.info("批量处理到期合约 date={}, count={}", date, expiredContracts.size());

        int count = 0;
        for (Contract contract : expiredContracts) {
            try {
                expireContract(contract.getContractId());
                count++;
            } catch (Exception e) {
                log.error("处理到期合约失败 contractId={}", contract.getContractId(), e);
            }
        }
        return count;
    }

    //endregion

    //region CON-011: 手动提前解约

    /**
     * 手动提前解约
     *
     * @param contractId 合约ID
     * @param terminationType 终止类型
     * @param refundAmount 退还金额
     * @param deductionAmount 扣费金额（可选）
     * @param deductionRemark 扣费原因（可选）
     * @return 终止结果
     */
    @Transactional
    public TerminationResult earlyTerminate(String contractId,
                                             TerminationType terminationType,
                                             java.math.BigDecimal refundAmount,
                                             java.math.BigDecimal deductionAmount,
                                             String deductionRemark) {
        log.info("手动提前解约 contractId={}, type={}", contractId, terminationType);

        Contract contract = contractRepository.findById(contractId)
            .orElseThrow(() -> new BusinessException("C010", "合约不存在"));

        ContractTerminatedEvent event = contractDomainService.terminateContract(
            contract, terminationType, refundAmount, deductionAmount, deductionRemark);

        contractRepository.update(contract);

        // 发布领域事件，触发账单截断、押金结算、房间复位等
        publishTerminationEvent(event);

        return TerminationResult.builder()
            .contractId(contractId)
            .terminationType(event.getTerminationType())
            .refundAmount(refundAmount)
            .deductionAmount(deductionAmount)
            .build();
    }

    //endregion

    //region CON-012: 合约作废

    /**
     * 合约作废（新建未履约）
     *
     * @param contractId 合约ID
     * @param reason 作废原因
     * @return 终止结果
     */
    @Transactional
    public TerminationResult voidContract(String contractId, String reason) {
        log.info("合约作废 contractId={}, reason={}", contractId, reason);

        Contract contract = contractRepository.findById(contractId)
            .orElseThrow(() -> new BusinessException("C010", "合约不存在"));

        ContractTerminatedEvent event = contractDomainService.voidContract(contract, reason);

        contractRepository.update(contract);

        // VOID类型不触发下游（不生成账单、不变更房间状态）

        return TerminationResult.builder()
            .contractId(contractId)
            .terminationType(event.getTerminationType())
            .build();
    }

    //endregion

    //region CON-020: 合约列表查询

    /**
     * 合约列表查询（房东/租客双视角）
     *
     * @param memberId 会员ID
     * @param memberType 会员类型
     * @param status 状态筛选（可选）
     * @param roomId 房间ID筛选（可选）
     * @param tenantName 租客姓名筛选（可选）
     * @return 合约列表
     */
    public List<ContractListItem> queryContracts(String memberId,
                                                   String memberType,
                                                   ContractStatus status,
                                                   String roomId,
                                                   String tenantName) {
        log.info("查询合约列表 memberId={}, memberType={}, status={}",
            memberId, memberType, status);

        List<Contract> contracts;

        if ("LANDLORD".equals(memberType)) {
            if (status != null) {
                contracts = contractRepository.findByLandlordMemberIdAndStatus(memberId, status);
            } else {
                contracts = contractRepository.findByLandlordMemberId(memberId);
            }
        } else if ("TENANT".equals(memberType)) {
            if (status != null) {
                contracts = contractRepository.findByTenantMemberIdAndStatus(memberId, status);
            } else {
                contracts = contractRepository.findByTenantMemberId(memberId);
            }
        } else {
            // ADMIN或其他角色
            contracts = contractRepository.findByLandlordMemberId(memberId);
        }

        // 转换为列表项（脱敏处理）
        return contracts.stream()
            .map(this::toContractListItem)
            .collect(Collectors.toList());
    }

    //endregion

    //region CON-021: 合约详情查询

    /**
     * 合约详情查询
     *
     * @param contractId 合约ID
     * @param memberId 会员ID
     * @param memberType 会员类型
     * @return 合约详情
     */
    public ContractDetail getContractDetail(String contractId, String memberId, String memberType) {
        log.info("查询合约详情 contractId={}, memberId={}, memberType={}",
            contractId, memberId, memberType);

        Contract contract = contractRepository.findById(contractId)
            .orElseThrow(() -> new BusinessException("C010", "合约不存在"));

        // 权限校验
        if (!contractDomainService.canAccessContract(contract, memberId, memberType)) {
            throw new BusinessException("P001", "无权限访问该合约");
        }

        // 查询计费规则快照
        ContractBillingRulesSnapshot snapshot = billingRulesSnapshotRepository.findByContractId(contractId)
            .orElse(null);

        // 构建详情（敏感信息脱敏处理）
        return ContractDetail.builder()
            .contractId(contract.getContractId())
            .roomId(contract.getRoomId())
            .houseSourceId(contract.getHouseSourceId())
            .tenantMemberId(contract.getTenantMemberId())
            .tenantNameMasked(maskName("")) // 简化，实际应查询
            .tenantPhoneMasked("138****5678") // 简化，实际应查询并脱敏
            .startDate(contract.getStartDate())
            .endDate(contract.getEndDate())
            .status(contract.getStatus())
            .paperContractUrl(contract.getPaperContractUrl())
            .billingRulesJson(snapshot != null ? snapshot.getRulesJson() : null)
            .remainingDays(contract.getRemainingDays())
            .createTime(contract.getCreateTime())
            .build();
    }

    //endregion

    //region 私有辅助方法

    private String findOrCreateTenantMember(String phone, String realName) {
        // 简化处理：实际应查询member表，如不存在则创建
        return "TENANT_" + phone;
    }

    private String generateContractId() {
        return "CON_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    private String generateSnapshotId() {
        return "SNAP_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    private String maskName(String name) {
        if (name == null || name.isEmpty()) {
            return "***";
        }
        return name.charAt(0) + "***";
    }

    private ContractListItem toContractListItem(Contract contract) {
        return ContractListItem.builder()
            .contractId(contract.getContractId())
            .roomId(contract.getRoomId())
            .tenantMemberId(contract.getTenantMemberId())
            .tenantNameMasked(maskName("")) // 简化
            .startDate(contract.getStartDate())
            .endDate(contract.getEndDate())
            .status(contract.getStatus())
            .remainingDays(contract.getRemainingDays())
            .build();
    }

    private void publishTerminationEvent(ContractTerminatedEvent event) {
        // 简化处理：实际应通过事件总线发布
        log.info("发布合约终止事件 contractId={}, type={}",
            event.getContractId(), event.getTerminationType());
    }

    //endregion

    //region 结果对象

    @lombok.Data
    @lombok.Builder
    public static class ContractCreateResult {
        private String contractId;
        private ContractStatus status;
        private String roomId;
        private String tenantMemberId;
        private String billingRulesSnapshotId;
        private List<String> firstBillIds;
    }

    @lombok.Data
    @lombok.Builder
    public static class InviteCodeResult {
        private String inviteCode;
        private LocalDateTime expireTime;
    }

    @lombok.Data
    @lombok.Builder
    public static class TerminationResult {
        private String contractId;
        private String terminationType;
        private java.math.BigDecimal refundAmount;
        private java.math.BigDecimal deductionAmount;
    }

    @lombok.Data
    @lombok.Builder
    public static class ContractListItem {
        private String contractId;
        private String roomId;
        private String tenantMemberId;
        private String tenantNameMasked;
        private LocalDate startDate;
        private LocalDate endDate;
        private ContractStatus status;
        private long remainingDays;
    }

    @lombok.Data
    @lombok.Builder
    public static class ContractDetail {
        private String contractId;
        private String roomId;
        private String houseSourceId;
        private String tenantMemberId;
        private String tenantNameMasked;
        private String tenantPhoneMasked;
        private LocalDate startDate;
        private LocalDate endDate;
        private ContractStatus status;
        private String paperContractUrl;
        private String billingRulesJson;
        private long remainingDays;
        private LocalDateTime createTime;
    }

    //endregion
}
