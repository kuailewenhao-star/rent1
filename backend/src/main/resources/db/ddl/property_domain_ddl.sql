-- =====================================================
-- 房东租赁管理系统 V1.0 - 房源域数据库DDL脚本
-- =====================================================
-- 创建时间：2026-06-17
-- 模块：房源域 Property Domain
-- 数据库：MySQL 5.7+
-- =====================================================

-- -----------------------------------------------------
-- 表 house_source - 房源表
-- -----------------------------------------------------
-- 说明：房源为整套房屋管理载体，记录房东收房成本与房屋基础信息
-- 核心业务规则：
-- 1. 房源层级不存储图片、租金，所有出租属性全部下沉至房间层级
-- 2. 停用/终止房源仅留存历史数据，不再参与平台出租率统计
-- 3. V1.0不支持房源物理删除，仅做状态管控
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS `house_source` (
    `house_source_id` VARCHAR(64) NOT NULL COMMENT '房源ID（主键，雪花算法生成）',
    `landlord_member_id` VARCHAR(64) NOT NULL COMMENT '房东会员ID（归属）',
    `name` VARCHAR(50) NOT NULL COMMENT '房源名称（≤50字符）',
    `province` VARCHAR(50) NOT NULL COMMENT '省份',
    `city` VARCHAR(50) NOT NULL COMMENT '城市',
    `district` VARCHAR(50) NOT NULL COMMENT '区县',
    `address` VARCHAR(200) NOT NULL COMMENT '详细地址',
    `total_rooms` INT NOT NULL DEFAULT 1 COMMENT '总户型数',
    `type` VARCHAR(20) NOT NULL COMMENT '房源类型：ENTIRE整租/SHARED合租',
    `status` VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT '房源业务状态：NORMAL正常/LEASE_EXPIRED到期停用/TERMINATED终止经营/VOID作废',
    `lease_start` DATE NOT NULL COMMENT '承租起始时间（与大房东的合约）',
    `lease_end` DATE NOT NULL COMMENT '承租结束时间（与大房东的合约）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记（0未删除，1已删除）',
    PRIMARY KEY (`house_source_id`),
    INDEX `idx_landlord_member_id` (`landlord_member_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_type` (`type`),
    INDEX `idx_city` (`city`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='房源表';

-- -----------------------------------------------------
-- 表 room - 房间表
-- -----------------------------------------------------
-- 说明：房间是系统最小出租、签约、计费、统计单元
-- 核心业务规则：
-- 1. 房间状态仅设置两种物理真实状态：空置中/已出租
-- 2. 即将到期为前端计算标签（≤30天），不修改底层状态
-- 3. 房间状态仅允许空置↔已出租两种流转
-- 4. 已出租房间不可编辑房间信息
-- 5. 单房间最多9张图片
-- 6. 金额边界：月租0~999999.99，押金0~999999.99
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS `room` (
    `room_id` VARCHAR(64) NOT NULL COMMENT '房间ID（主键，雪花算法生成）',
    `house_source_id` VARCHAR(64) NOT NULL COMMENT '所属房源ID',
    `room_name` VARCHAR(20) NOT NULL COMMENT '房间名称（≤20字符）',
    `area` DECIMAL(10,2) DEFAULT NULL COMMENT '房间面积（平方米）',
    `monthly_rent` DECIMAL(10,2) DEFAULT NULL COMMENT '月租金额（边界0~999999.99）',
    `deposit` DECIMAL(10,2) DEFAULT NULL COMMENT '押金金额（边界0~999999.99）',
    `images` JSON DEFAULT NULL COMMENT '房间图片URL数组（JSON格式，最多9张）',
    `status` VARCHAR(20) NOT NULL DEFAULT 'VACANT' COMMENT '房间状态：VACANT空置中/OCCUPIED已出租',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记（0未删除，1已删除）',
    PRIMARY KEY (`room_id`),
    INDEX `idx_house_source_id` (`house_source_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_create_time` (`create_time`),
    CONSTRAINT `fk_room_house_source` FOREIGN KEY (`house_source_id`) 
        REFERENCES `house_source` (`house_source_id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='房间表';

-- -----------------------------------------------------
-- 初始化数据 - 枚举字典对照表
-- -----------------------------------------------------
-- 说明：用于前端展示枚举值与名称的对照
-- -----------------------------------------------------

-- 房源类型枚举
INSERT INTO `enum_dict` (`category`, `code`, `name`, `sort_order`) VALUES
('HOUSE_SOURCE_TYPE', 'ENTIRE', '整租房源', 1),
('HOUSE_SOURCE_TYPE', 'SHARED', '合租房源', 2);

-- 房源业务状态枚举
INSERT INTO `enum_dict` (`category`, `code`, `name`, `sort_order`) VALUES
('HOUSE_SOURCE_STATUS', 'NORMAL', '正常经营', 1),
('HOUSE_SOURCE_STATUS', 'LEASE_EXPIRED', '租期到期停用', 2),
('HOUSE_SOURCE_STATUS', 'TERMINATED', '主动终止经营', 3),
('HOUSE_SOURCE_STATUS', 'VOID', '作废', 4);

-- 房间状态枚举
INSERT INTO `enum_dict` (`category`, `code`, `name`, `sort_order`) VALUES
('ROOM_STATUS', 'VACANT', '空置中', 1),
('ROOM_STATUS', 'OCCUPIED', '已出租', 2);

-- -----------------------------------------------------
-- 索引优化说明
-- -----------------------------------------------------
-- 1. house_source表索引：
--    - idx_landlord_member_id：按房东ID查询房源列表（高频查询）
--    - idx_status：按状态筛选房源（高频筛选）
--    - idx_type：按类型筛选房源（高频筛选）
--    - idx_city：按城市筛选房源（高频筛选）
--    - idx_create_time：按创建时间排序（列表展示）
-- 
-- 2. room表索引：
--    - idx_house_source_id：按房源ID查询房间列表（高频查询）
--    - idx_status：按状态筛选房间（高频筛选）
--    - idx_create_time：按创建时间排序（列表展示）
-- 
-- -----------------------------------------------------

-- =====================================================
-- DDL脚本结束
-- =====================================================