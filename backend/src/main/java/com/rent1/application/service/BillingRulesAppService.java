package com.rent1.application.service;

import com.rent1.domain.billing.BillingRulesDomainService;
import com.rent1.domain.billing.BillingRulesRepository;
import com.rent1.domain.billing.BillingRulesSnapshotRepository;
import com.rent1.domain.billing.ChargeCycle;
import com.rent1.domain.billing.ChargeType;
import com.rent1.domain.billing.FeeType;
import com.rent1.domain.billing.entity.BillingItem;
import com.rent1.domain.billing.entity.BillingRules;
import com.rent1.domain.billing.entity.BillingRulesSnapshot;
import com.rent1.api.dto.billing.AddBillingItemRequest;
import com.rent1.api.dto.billing.BillingItemResponse;
import com.rent1.api.dto.billing.BillingRulesResponse;
import com.rent1.api.dto.billing.UpdateBillingItemRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 计费规则应用服务 - 业务流程编排层
 *
 * 本层仅做：
 * 1. DTO <-> 领域对象 转换
 * 2. 调用领域服务完成业务校验与状态流转
 * 3. 调用仓储层完成持久化
 * 4. 事务控制
 *
 * 禁止在本层编写任何核心业务规则。
 */
@Service
public class BillingRulesAppService {

    private final BillingRulesRepository rulesRepository;
    private final BillingRulesSnapshotRepository snapshotRepository;
    private final BillingRulesDomainService domainService;

    @Autowired
    public BillingRulesAppService(BillingRulesRepository rulesRepository,
                                   BillingRulesSnapshotRepository snapshotRepository) {
        this.rulesRepository = rulesRepository;
        this.snapshotRepository = snapshotRepository;
        this.domainService = new BillingRulesDomainService();
    }

    /**
     * 新增费用项配置
     * 若该房间尚无计费规则记录，自动初始化一条
     */
    @Transactional
    public BillingRulesResponse addBillingItem(String roomId, AddBillingItemRequest request) {
        BillingItem item = toDomainItem(request);

        // 1. 读取或初始化房间计费规则
        BillingRules rules = rulesRepository.findByRoomId(roomId)
                .orElseGet(() -> {
                    BillingRules newRules = new BillingRules();
                    newRules.initialize(roomId);
                    return newRules;
                });

        // 2. 领域服务完成校验 + 添加
        rules = domainService.configureNewItem(rules, item);

        // 3. 持久化
        if (rules.getRulesId() == null) {
            rulesRepository.save(rules);
        } else {
            rulesRepository.update(rules);
        }

        return toResponse(rules);
    }

    /**
     * 编辑指定 feeType 的计费项配置
     */
    @Transactional
    public BillingRulesResponse updateBillingItem(String roomId, String feeTypeCode,
                                                  UpdateBillingItemRequest request) {
        // 1. 校验房间计费规则存在
        BillingRules rules = rulesRepository.findByRoomId(roomId)
                .orElseThrow(() -> new IllegalArgumentException("该房间尚未配置计费规则"));

        // 2. 解析 feeType、构造更新对象
        FeeType feeType = FeeType.fromCode(feeTypeCode);
        BillingItem updated = new BillingItem();
        if (request.getChargeType() != null) {
            updated.setChargeType(ChargeType.fromCode(request.getChargeType()));
        }
        if (request.getChargeValue() != null) {
            updated.setChargeValue(request.getChargeValue());
        }
        if (request.getChargeCycle() != null) {
            updated.setChargeCycle(ChargeCycle.fromCode(request.getChargeCycle()));
        }
        updated.setRemark(request.getRemark());

        // 3. 领域服务完成校验 + 更新
        rules = domainService.updateExistingItem(rules, feeType, updated);

        // 4. 持久化
        rulesRepository.update(rules);

        return toResponse(rules);
    }

    /**
     * 删除指定 feeType 的计费项
     * 租金/押金不可删除，由领域服务拦截
     */
    @Transactional
    public BillingRulesResponse deleteBillingItem(String roomId, String feeTypeCode) {
        BillingRules rules = rulesRepository.findByRoomId(roomId)
                .orElseThrow(() -> new IllegalArgumentException("该房间尚未配置计费规则"));

        FeeType feeType = FeeType.fromCode(feeTypeCode);
        rules = domainService.removeExistingItem(rules, feeType);
        rulesRepository.update(rules);

        return toResponse(rules);
    }

    /**
     * 查询房间计费规则
     * 对外暴露：未签约房间=当前配置，已签约房间=锁定快照
     */
    @Transactional(readOnly = true)
    public BillingRulesResponse getRoomBillingRules(String roomId, String contractId) {
        // 已签约：优先返回锁定快照
        if (contractId != null && !contractId.trim().isEmpty()) {
            return snapshotRepository.findByContractId(contractId)
                    .map(this::toResponseFromSnapshot)
                    .orElseGet(() -> findByRoomIdOrEmpty(roomId));
        }
        return findByRoomIdOrEmpty(roomId);
    }

    private BillingRulesResponse findByRoomIdOrEmpty(String roomId) {
        return rulesRepository.findByRoomId(roomId)
                .map(this::toResponse)
                .orElseGet(() -> emptyResponse(roomId));
    }

    /**
     * 合约创建时锁定计费规则 + 生成快照
     * 供合约域 ContractAppService 调用
     *
     * @param roomId     待签约房间ID
     * @param contractId 新创建的合约ID
     * @return 锁定后生成的快照对象
     */
    @Transactional
    public BillingRulesSnapshot lockRulesForContract(String roomId, String contractId) {
        BillingRules rules = rulesRepository.findByRoomId(roomId)
                .orElseThrow(() -> new IllegalArgumentException("该房间尚未配置计费规则，无法签约"));

        BillingRulesSnapshot snapshot = domainService.lockAndCreateSnapshot(rules, contractId);

        // 1. 更新计费规则为锁定状态
        rulesRepository.update(rules);
        // 2. 保存快照
        snapshotRepository.save(snapshot);

        return snapshot;
    }

    // ========== 领域对象 <-> DTO 转换 ==========

    private BillingItem toDomainItem(AddBillingItemRequest request) {
        BillingItem item = new BillingItem();
        item.setFeeType(FeeType.fromCode(request.getFeeType()));
        item.setChargeType(ChargeType.fromCode(request.getChargeType()));
        item.setChargeValue(request.getChargeValue());
        if (request.getChargeCycle() != null) {
            item.setChargeCycle(ChargeCycle.fromCode(request.getChargeCycle()));
        }
        item.setRemark(request.getRemark());
        item.normalizeByFeeType();
        return item;
    }

    private BillingRulesResponse toResponse(BillingRules rules) {
        return BillingRulesResponse.builder()
                .rulesId(rules.getRulesId())
                .roomId(rules.getRoomId())
                .locked(rules.isLocked())
                .lockedAt(rules.getLockedAt())
                .createTime(rules.getCreateTime())
                .updateTime(rules.getUpdateTime())
                .items(toItemResponseList(rules.getItems()))
                .build();
    }

    private BillingRulesResponse toResponseFromSnapshot(BillingRulesSnapshot snapshot) {
        return BillingRulesResponse.builder()
                .rulesId(snapshot.getSnapshotId())
                .roomId(null) // 快照不直接关联roomId
                .locked(true)
                .lockedAt(snapshot.getLockedAt())
                .createTime(snapshot.getCreateTime())
                .updateTime(snapshot.getCreateTime())
                .items(toItemResponseList(snapshot.getRulesJson()))
                .build();
    }

    private List<BillingItemResponse> toItemResponseList(List<BillingItem> items) {
        if (items == null || items.isEmpty()) {
            return new ArrayList<>();
        }
        return items.stream().map(item -> BillingItemResponse.builder()
                        .feeType(item.getFeeType() != null ? item.getFeeType().getCode() : null)
                        .feeTypeName(item.getFeeTypeName())
                        .chargeType(item.getChargeType() != null ? item.getChargeType().getCode() : null)
                        .chargeValue(item.getChargeValue())
                        .chargeCycle(item.getChargeCycle() != null ? item.getChargeCycle().getCode() : null)
                        .remark(item.getRemark())
                        .immutable(item.isImmutable())
                        .build())
                .collect(Collectors.toList());
    }

    private BillingRulesResponse emptyResponse(String roomId) {
        return BillingRulesResponse.builder()
                .roomId(roomId)
                .locked(false)
                .items(new ArrayList<>())
                .build();
    }
}
