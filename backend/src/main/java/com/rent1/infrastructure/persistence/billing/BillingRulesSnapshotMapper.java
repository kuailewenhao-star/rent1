package com.rent1.infrastructure.persistence.billing;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * billing_rules_snapshot 表 MyBatis Plus Mapper
 */
@Mapper
public interface BillingRulesSnapshotMapper extends BaseMapper<BillingRulesSnapshotPO> {

    @Select("SELECT * FROM billing_rules_snapshot WHERE contract_id = #{contractId} LIMIT 1")
    BillingRulesSnapshotPO selectByContractId(@Param("contractId") String contractId);
}
