-- =====================================================
-- 房东租赁管理系统 - 计费域 Billing Rules 表结构
-- 版本: V1.0
-- 描述: 计费规则主表 + 计费规则快照表
-- =====================================================

-- -----------------------------------------------------
-- billing_rules 计费规则主表
-- 核心约束: room_id 唯一，每个房间仅对应一套计费规则
-- 业务规则: 合约创建后 is_locked=TRUE，所有编辑操作被领域服务拦截
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS billing_rules (
    rules_id        VARCHAR(64)    NOT NULL    COMMENT '计费规则ID',
    room_id         VARCHAR(64)    NOT NULL    COMMENT '关联房间ID',
    items_json      JSON           NULL        COMMENT '费用项列表JSON: [{feeType,chargeType,chargeValue,chargeCycle,remark,immutable}]',
    is_locked       TINYINT(1)     NOT NULL    DEFAULT 0    COMMENT '是否已锁定: 0=未锁定 1=已锁定(签约后不可编辑)',
    locked_at       DATETIME       NULL        COMMENT '锁定时间',
    create_time     DATETIME       NOT NULL    DEFAULT CURRENT_TIMESTAMP    COMMENT '创建时间',
    update_time     DATETIME       NOT NULL    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP    COMMENT '更新时间',
    PRIMARY KEY (rules_id),
    UNIQUE KEY uk_room_id (room_id),
    KEY idx_locked (is_locked)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='计费规则表';

-- -----------------------------------------------------
-- billing_rules_snapshot 计费规则快照表
-- 核心约束: contract_id 唯一，一个合约对应一份锁定快照
-- 设计目的: 签约瞬间冻结费用项配置，历史账单严格基于当时快照生成
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS billing_rules_snapshot (
    snapshot_id    VARCHAR(64)    NOT NULL    COMMENT '快照ID',
    contract_id    VARCHAR(64)    NOT NULL    COMMENT '关联合约ID',
    rules_json     JSON           NOT NULL    COMMENT '锁定时的完整费用项JSON',
    locked_at      DATETIME       NOT NULL    COMMENT '锁定时间',
    create_time    DATETIME       NOT NULL    DEFAULT CURRENT_TIMESTAMP    COMMENT '创建时间',
    PRIMARY KEY (snapshot_id),
    UNIQUE KEY uk_contract_id (contract_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='计费规则快照表(签约时冻结)';

-- -----------------------------------------------------
-- 索引说明
-- -----------------------------------------------------
-- 1. billing_rules.uk_room_id: 保证每个房间仅一套计费规则，
--    并发新增时由数据库唯一性约束兜底，防止重复建单。
-- 2. billing_rules_snapshot.uk_contract_id: 保证每合约仅一份快照，
--    防止重复锁定。
-- 3. items_json / rules_json 使用 MySQL JSON 类型，便于未来扩展
--    费用项字段无需变更表结构，同时可通过 JSON_EXTRACT 做查询。
-- -----------------------------------------------------
