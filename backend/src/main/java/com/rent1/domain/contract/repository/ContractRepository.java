package com.rent1.domain.contract.repository;

import com.rent1.domain.common.ContractStatus;
import com.rent1.domain.contract.entity.Contract;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 合约仓储接口
 * 定义合约数据持久化操作
 */
public interface ContractRepository {

    /**
     * 根据合约ID查询
     */
    Optional<Contract> findById(String contractId);

    /**
     * 根据房间ID查询有效合约（履约中）
     */
    Optional<Contract> findActiveByRoomId(String roomId);

    /**
     * 根据房间ID查询所有合约
     */
    List<Contract> findByRoomId(String roomId);

    /**
     * 根据租客会员ID查询合约列表
     */
    List<Contract> findByTenantMemberId(String tenantMemberId);

    /**
     * 根据房东会员ID查询合约列表
     */
    List<Contract> findByLandlordMemberId(String landlordMemberId);

    /**
     * 根据房东ID + 状态筛选
     */
    List<Contract> findByLandlordMemberIdAndStatus(String landlordMemberId, ContractStatus status);

    /**
     * 根据房东ID + 多状态筛选（数据域统计专用）
     */
    List<Contract> findByLandlordMemberIdAndStatusIn(String landlordMemberId, List<ContractStatus> statuses);

    /**
     * 根据租客ID + 状态筛选
     */
    List<Contract> findByTenantMemberIdAndStatus(String tenantMemberId, ContractStatus status);

    /**
     * 查询到期合约（用于定时任务）
     * 条件：endDate <= 当前日期 AND status = ACTIVE
     */
    List<Contract> findExpiredContracts(LocalDate date);

    /**
     * 保存合约
     */
    Contract save(Contract contract);

    /**
     * 更新合约
     */
    Contract update(Contract contract);

    /**
     * 删除合约（谨慎使用，V1.0通常不做物理删除）
     */
    void delete(String contractId);

    /**
     * 根据状态查询合约列表
     */
    List<Contract> findByStatus(ContractStatus status);

    /**
     * 根据ID查询合约（非Optional版本）
     */
    Contract findByIdDirect(String contractId);
}
