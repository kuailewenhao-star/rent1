package com.rent1.infrastructure.persistence.billing;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rent1.domain.billing.BillingRulesSnapshotRepository;
import com.rent1.domain.billing.entity.BillingItem;
import com.rent1.domain.billing.entity.BillingRulesSnapshot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 计费规则快照仓储实现
 */
@Repository
public class BillingRulesSnapshotRepositoryImpl implements BillingRulesSnapshotRepository {

    private final BillingRulesSnapshotMapper mapper;
    private final ObjectMapper objectMapper;

    @Autowired
    public BillingRulesSnapshotRepositoryImpl(BillingRulesSnapshotMapper mapper) {
        this.mapper = mapper;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public Optional<BillingRulesSnapshot> findByContractId(String contractId) {
        if (contractId == null) {
            return Optional.empty();
        }
        BillingRulesSnapshotPO po = mapper.selectByContractId(contractId);
        if (po == null) {
            return Optional.empty();
        }
        return Optional.of(toDomain(po));
    }

    @Override
    public void save(BillingRulesSnapshot snapshot) {
        if (snapshot == null) {
            return;
        }
        mapper.insert(toPO(snapshot));
    }

    // ========== PO <-> 领域实体 转换 ==========

    private BillingRulesSnapshot toDomain(BillingRulesSnapshotPO po) {
        BillingRulesSnapshot snapshot = new BillingRulesSnapshot();
        snapshot.setSnapshotId(po.getSnapshotId());
        snapshot.setContractId(po.getContractId());
        snapshot.setLockedAt(po.getLockedAt());
        snapshot.setCreateTime(po.getCreateTime());
        snapshot.setRulesJson(parseItemsJson(po.getRulesJson()));
        return snapshot;
    }

    private BillingRulesSnapshotPO toPO(BillingRulesSnapshot snapshot) {
        BillingRulesSnapshotPO po = new BillingRulesSnapshotPO();
        po.setSnapshotId(snapshot.getSnapshotId());
        po.setContractId(snapshot.getContractId());
        po.setLockedAt(snapshot.getLockedAt());
        po.setCreateTime(snapshot.getCreateTime());
        po.setRulesJson(toItemsJson(snapshot.getRulesJson()));
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
