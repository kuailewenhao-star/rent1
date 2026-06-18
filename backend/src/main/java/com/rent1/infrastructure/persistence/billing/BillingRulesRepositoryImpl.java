package com.rent1.infrastructure.persistence.billing;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rent1.domain.billing.BillingRulesRepository;
import com.rent1.domain.billing.entity.BillingItem;
import com.rent1.domain.billing.entity.BillingRules;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 计费规则仓储实现 - 完成 PO<->领域实体 的双向转换
 * items_json 通过 Jackson 做 JSON序列化/反序列化
 */
@Repository
public class BillingRulesRepositoryImpl implements BillingRulesRepository {

    private final BillingRulesMapper mapper;
    private final ObjectMapper objectMapper;

    @Autowired
    public BillingRulesRepositoryImpl(BillingRulesMapper mapper) {
        this.mapper = mapper;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public Optional<BillingRules> findByRoomId(String roomId) {
        if (roomId == null) {
            return Optional.empty();
        }
        LambdaQueryWrapper<BillingRulesPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BillingRulesPO::getRoomId, roomId);
        BillingRulesPO po = mapper.selectOne(wrapper);
        if (po == null) {
            return Optional.empty();
        }
        return Optional.of(toDomain(po));
    }

    @Override
    public Optional<BillingRules> findById(String rulesId) {
        if (rulesId == null) {
            return Optional.empty();
        }
        BillingRulesPO po = mapper.selectById(rulesId);
        if (po == null) {
            return Optional.empty();
        }
        return Optional.of(toDomain(po));
    }

    @Override
    public void save(BillingRules rules) {
        if (rules == null) {
            return;
        }
        mapper.insert(toPO(rules));
    }

    @Override
    public void update(BillingRules rules) {
        if (rules == null) {
            return;
        }
        mapper.updateById(toPO(rules));
    }

    // ========== PO <-> 领域实体 转换 ==========

    private BillingRules toDomain(BillingRulesPO po) {
        BillingRules rules = new BillingRules();
        rules.setRulesId(po.getRulesId());
        rules.setRoomId(po.getRoomId());
        rules.setLocked(po.getIsLocked() != null && po.getIsLocked());
        rules.setLockedAt(po.getLockedAt());
        rules.setCreateTime(po.getCreateTime());
        rules.setUpdateTime(po.getUpdateTime());
        rules.setItems(parseItemsJson(po.getItemsJson()));
        return rules;
    }

    private BillingRulesPO toPO(BillingRules rules) {
        BillingRulesPO po = new BillingRulesPO();
        po.setRulesId(rules.getRulesId());
        po.setRoomId(rules.getRoomId());
        po.setIsLocked(rules.isLocked());
        po.setLockedAt(rules.getLockedAt());
        po.setCreateTime(rules.getCreateTime());
        po.setUpdateTime(rules.getUpdateTime());
        po.setItemsJson(toItemsJson(rules.getItems()));
        return po;
    }

    private List<BillingItem> parseItemsJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<BillingItem>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private String toItemsJson(List<BillingItem> items) {
        if (items == null || items.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(items);
        } catch (Exception e) {
            return "[]";
        }
    }
}
