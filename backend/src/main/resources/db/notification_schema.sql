-- =====================================================
-- 消息域数据库表结构DDL脚本
-- 表名：notification
-- 功能：小程序站内消息存储
-- 核心业务规则：
-- 1. 合约到期提醒：提前7天、前3天双向推送
-- 2. 租金账单每日催收提醒
-- 3. 杂费账单单次提醒
-- 4. 合约状态变更通知
-- 5. 去重机制：同一合约同一天同一类型只推送1次
-- 6. 权限隔离：仅看个人消息
-- =====================================================

-- 创建消息表
CREATE TABLE IF NOT EXISTS `notification` (
    `notification_id` VARCHAR(64) NOT NULL COMMENT '消息ID（雪花算法生成）',
    `member_id` VARCHAR(64) NOT NULL COMMENT '接收者会员ID',
    `type` VARCHAR(32) NOT NULL COMMENT '消息类型：CONTRACT_EXPIRING/RENT_REMINDER/MISC_FEE_REMINDER/CONTRACT_STATUS_CHANGE/SHARED_BILL_NOTICE/GENERAL',
    `title` VARCHAR(128) NOT NULL COMMENT '消息标题',
    `content` VARCHAR(512) NOT NULL COMMENT '消息内容',
    `related_type` VARCHAR(32) DEFAULT 'NONE' COMMENT '关联业务类型：CONTRACT/INVOICE/ROOM/HOUSE_SOURCE/DEPOSIT/NONE',
    `related_id` VARCHAR(64) DEFAULT NULL COMMENT '关联业务ID',
    `status` VARCHAR(16) NOT NULL DEFAULT 'UNREAD' COMMENT '消息状态：UNREAD/READ',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `read_time` DATETIME DEFAULT NULL COMMENT '阅读时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deduplication_key` VARCHAR(128) DEFAULT NULL COMMENT '去重标识（用于防止重复推送）',
    PRIMARY KEY (`notification_id`),
    INDEX `idx_member_id` (`member_id`) COMMENT '会员ID索引（权限隔离查询）',
    INDEX `idx_member_status` (`member_id`, `status`) COMMENT '会员+状态联合索引（未读消息查询）',
    INDEX `idx_create_time` (`create_time`) COMMENT '创建时间索引（消息列表排序）',
    INDEX `idx_deduplication_key` (`deduplication_key`) COMMENT '去重标识索引（防重复推送）',
    INDEX `idx_related_type_id` (`related_type`, `related_id`) COMMENT '关联业务类型+ID联合索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表-小程序站内消息';

-- =====================================================
-- 索引设计说明：
-- 1. idx_member_id：核心查询索引，用于按会员ID查询消息列表
-- 2. idx_member_status：用于统计未读消息数量、查询未读消息列表
-- 3. idx_create_time：用于消息列表按创建时间倒序排序
-- 4. idx_deduplication_key：用于去重查询，防止同一合约同一天重复推送
-- 5. idx_related_type_id：用于按关联业务查询消息（如查询某合约的所有消息）
-- =====================================================

-- =====================================================
-- 初始化数据（可选）
-- =====================================================

-- 无初始化数据，消息由业务事件触发生成

-- =====================================================
-- 变更说明
-- =====================================================
-- V1.0 初始版本
-- - 创建消息表
-- - 添加核心索引
-- - 支持消息类型：合约到期提醒、租金催收、杂费提醒、合约状态变更、公摊账单通知
-- =====================================================