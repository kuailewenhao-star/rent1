package com.rent1.domain.deposit;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 押金仓储接口 - 数据持久化抽象
 * 
 * 职责：押金记录的数据访问层抽象
 */
public interface DepositRepository {
    
    //region CRUD操作
    
    /**
     * 保存押金记录
     * @param depositRecord 押金记录
     * @return 保存后的押金记录
     */
    DepositRecord save(DepositRecord depositRecord);
    
    /**
     * 根据ID查询押金记录
     * @param recordId 押金记录ID
     * @return 押金记录
     */
    Optional<DepositRecord> findById(String recordId);
    
    /**
     * 根据合约ID查询押金记录
     * @param contractId 合约ID
     * @return 押金记录
     */
    Optional<DepositRecord> findByContractId(String contractId);
    
    //endregion
    
    //region 押金统计查询
    
    /**
     * 查询房东有效持有押金总额
     * 
     * 业务规则：
     * 1. 仅统计status=HELD的押金记录
     * 2. 仅统计关联押金账单status=PAID的记录
     * 3. 已退押金不纳入统计
     * 
     * @param landlordMemberId 房东会员ID
     * @return 有效押金总额
     */
    BigDecimal sumValidDepositByLandlord(String landlordMemberId);
    
    /**
     * 查询房东有效押金记录数
     * @param landlordMemberId 房东会员ID
     * @return 有效押金记录数
     */
    int countValidDepositByLandlord(String landlordMemberId);
    
    /**
     * 查询租客有效押金总额
     * 
     * 业务规则：
     * 1. 仅统计当前租客关联的履约中合约下的押金
     * 2. 押金状态为HELD且账单已支付
     * 
     * @param tenantMemberId 租客会员ID
     * @return 有效押金金额
     */
    BigDecimal sumValidDepositByTenant(String tenantMemberId);
    
    /**
     * 根据房东ID查询所有押金记录
     * @param landlordMemberId 房东会员ID
     * @return 押金记录列表
     */
    List<DepositRecord> findAllByLandlord(String landlordMemberId);
    
    /**
     * 根据租客ID查询所有押金记录
     * @param tenantMemberId 租客会员ID
     * @return 押金记录列表
     */
    List<DepositRecord> findAllByTenant(String tenantMemberId);
    
    //endregion
    
    //region 押金账单关联查询
    
    /**
     * 根据押金账单ID查询押金记录
     * @param invoiceId 押金账单ID
     * @return 押金记录
     */
    Optional<DepositRecord> findByInvoiceId(String invoiceId);
    
    //endregion
}
