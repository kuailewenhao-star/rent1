-- 会员域数据库表结构

-- 会员表
CREATE TABLE IF NOT EXISTS `member` (
    `member_id` VARCHAR(64) NOT NULL COMMENT '会员ID',
    `project_id` VARCHAR(64) DEFAULT 'default' COMMENT '项目ID',
    `subject_id` VARCHAR(64) DEFAULT NULL COMMENT '主体ID(同用户多角色关联)',
    `user_id` VARCHAR(128) DEFAULT NULL COMMENT '小程序用户ID(openid)',
    `member_type` VARCHAR(20) NOT NULL COMMENT '角色：LANDLORD/TENANT/ADMIN',
    `phone_encrypted` TEXT COMMENT '手机号(加密)',
    `real_name_encrypted` TEXT COMMENT '真实姓名(加密)',
    `id_card_encrypted` TEXT COMMENT '身份证号(加密)',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `status` VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '账号状态：ACTIVE/DISABLED',
    `password_hash` VARCHAR(255) DEFAULT NULL COMMENT '密码哈希（仅管理员）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`member_id`),
    INDEX `idx_subject_id` (`subject_id`),
    INDEX `idx_member_type` (`member_type`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_phone_type` (`phone_encrypted`, `member_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员表';

-- 会员联系人表
CREATE TABLE IF NOT EXISTS `member_contact` (
    `contact_id` VARCHAR(64) NOT NULL COMMENT '联系人ID',
    `member_id` VARCHAR(64) NOT NULL COMMENT '关联会员ID',
    `name_encrypted` TEXT COMMENT '姓名(加密)',
    `phone_encrypted` TEXT COMMENT '手机号(加密)',
    `relationship` VARCHAR(50) DEFAULT NULL COMMENT '关系：RELATIVE/FRIEND/COLLEAGUE/OTHER',
    `is_default` TINYINT(1) DEFAULT FALSE COMMENT '是否默认联系人',
    `is_emergency` TINYINT(1) DEFAULT FALSE COMMENT '是否紧急联系人',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`contact_id`),
    INDEX `idx_member_id` (`member_id`),
    INDEX `idx_member_emergency` (`member_id`, `is_emergency`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员联系人表';

-- 初始化超级管理员账号（密码: admin123）
-- 密码需要通过BCrypt加密，这里仅作为结构参考
-- INSERT INTO `member` (`member_id`, `project_id`, `subject_id`, `user_id`, `member_type`, `status`, `password_hash`)
-- VALUES ('ADMIN001', 'default', 'SUB-ADMIN', 'admin', 'ADMIN', 'ACTIVE', '$2a$10$xxx'); -- 实际密码admin123
