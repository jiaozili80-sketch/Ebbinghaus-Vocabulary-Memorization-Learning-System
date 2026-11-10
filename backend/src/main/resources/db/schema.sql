-- 艾宾浩斯单词背诵学习系统数据库初始化脚本
-- 执行方式：在 MySQL 客户端/数据库工具中直接运行整个文件

CREATE DATABASE IF NOT EXISTS `ebbinghaus_vocab`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE `ebbinghaus_vocab`;

CREATE TABLE IF NOT EXISTS `sys_user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `nickname` VARCHAR(50) COMMENT '昵称',
    `avatar` VARCHAR(255) COMMENT '头像',
    `email` VARCHAR(100) COMMENT '邮箱',
    `role` VARCHAR(20) DEFAULT 'USER' COMMENT '角色 USER普通用户 ADMIN管理员',
    `status` TINYINT DEFAULT 1 COMMENT '状态 1正常 0封禁',
    `total_words` INT DEFAULT 0 COMMENT '累计学习单词数',
    `mastered_words` INT DEFAULT 0 COMMENT '已掌握单词数',
    `continuous_checkin_days` INT DEFAULT 0 COMMENT '连续打卡天数',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

CREATE TABLE IF NOT EXISTS `word_book` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT DEFAULT NULL COMMENT '私人词库所属用户，公共词库为空',
    `name` VARCHAR(100) NOT NULL COMMENT '词库名称',
    `category` VARCHAR(50) COMMENT '分类',
    `description` VARCHAR(500) COMMENT '描述',
    `word_count` INT DEFAULT 0 COMMENT '单词数量',
    `type` TINYINT DEFAULT 1 COMMENT '类型 1公共 2私人',
    `status` TINYINT DEFAULT 1 COMMENT '状态 1启用 0停用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    UNIQUE KEY `uk_word_book_name_type_user` (`name`, `type`, `user_id`),
    INDEX `idx_word_book_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='词库表';

CREATE TABLE IF NOT EXISTS `word` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `book_id` BIGINT NOT NULL COMMENT '词库ID',
    `spelling` VARCHAR(100) NOT NULL COMMENT '英文单词',
    `phonetic` VARCHAR(100) DEFAULT '' COMMENT '音标，原始词库无音标时为空',
    `part_of_speech` VARCHAR(30) COMMENT '词性',
    `meaning` VARCHAR(500) NOT NULL COMMENT '中文释义',
    `example_sentence` VARCHAR(1000) COMMENT '英文例句',
    `example_translation` VARCHAR(1000) COMMENT '例句翻译',
    `audio_url` VARCHAR(255) COMMENT '发音地址',
    `difficulty` TINYINT DEFAULT 1 COMMENT '难度 1-5',
    `status` TINYINT DEFAULT 1 COMMENT '状态 1上架 0下架',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    INDEX `idx_word_book_id` (`book_id`),
    INDEX `idx_word_spelling` (`spelling`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='单词表';

CREATE TABLE IF NOT EXISTS `review_plan` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `word_id` BIGINT NOT NULL,
    `stage` TINYINT DEFAULT 0 COMMENT '复习阶段',
    `status` TINYINT DEFAULT 0 COMMENT '状态 0待复习 1已掌握',
    `familiarity` TINYINT DEFAULT 1 COMMENT '熟悉度 1不认识 2模糊 3认识',
    `next_review_time` DATETIME COMMENT '下次复习时间',
    `last_review_time` DATETIME COMMENT '上次复习时间',
    `first_study_time` DATETIME COMMENT '首次提交答案时间，NULL表示还没学过',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    UNIQUE KEY `uk_review_user_word` (`user_id`, `word_id`),
    INDEX `idx_review_next_time` (`next_review_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='艾宾浩斯复习计划表';

CREATE TABLE IF NOT EXISTS `wrong_word` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `word_id` BIGINT NOT NULL,
    `wrong_count` INT DEFAULT 1 COMMENT '错误/薄弱次数',
    `status` TINYINT DEFAULT 1 COMMENT '状态 1强化中 0已移除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    UNIQUE KEY `uk_wrong_user_word` (`user_id`, `word_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='生词本/错题表';

CREATE TABLE IF NOT EXISTS `study_record` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `study_date` DATE NOT NULL,
    `new_word_count` INT DEFAULT 0,
    `review_word_count` INT DEFAULT 0,
    `mastered_word_count` INT DEFAULT 0,
    `wrong_word_count` INT DEFAULT 0,
    `study_duration` INT DEFAULT 0 COMMENT '学习时长，单位秒',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_study_user_date` (`user_id`, `study_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学习记录表';

CREATE TABLE IF NOT EXISTS `daily_goal` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL UNIQUE,
    `target_word_count` INT DEFAULT 30 COMMENT '每日目标词数',
    `target_study_minutes` INT DEFAULT 30 COMMENT '每日目标学习分钟数',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='每日学习目标表';

CREATE TABLE IF NOT EXISTS `study_plan` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `book_id` BIGINT NOT NULL COMMENT '词库ID',
    `plan_word_count` INT NOT NULL COMMENT '每日新词数',
    `review_multiplier` INT NOT NULL COMMENT '复习倍数',
    `daily_review_count` INT NOT NULL COMMENT '每日复习词数',
    `daily_total_count` INT NOT NULL COMMENT '每日总词数',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    INDEX `idx_study_plan_user_id` (`user_id`),
    INDEX `idx_study_plan_book_id` (`book_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学习计划表';

CREATE TABLE IF NOT EXISTS `checkin_record` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `checkin_date` DATE NOT NULL,
    `study_duration` INT DEFAULT 0 COMMENT '打卡学习时长，单位秒',
    `completed_target` TINYINT DEFAULT 0 COMMENT '是否完成目标 1是 0否',
    `remark` VARCHAR(255) COMMENT '打卡备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_checkin_user_date` (`user_id`, `checkin_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学习打卡记录表';

CREATE TABLE IF NOT EXISTS `announcement` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `title` VARCHAR(100) NOT NULL COMMENT '标题',
    `content` TEXT NOT NULL COMMENT '内容',
    `type` VARCHAR(30) NOT NULL COMMENT '类型 公告/贴士/资讯',
    `status` TINYINT DEFAULT 1 COMMENT '状态 1发布 0下架',
    `create_by` BIGINT COMMENT '创建人',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    INDEX `idx_announcement_type` (`type`),
    INDEX `idx_announcement_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告表';

CREATE TABLE IF NOT EXISTS `operation_log` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT COMMENT '操作用户',
    `module` VARCHAR(50) COMMENT '模块',
    `operation` VARCHAR(50) COMMENT '操作',
    `request_uri` VARCHAR(255) COMMENT '请求地址',
    `request_method` VARCHAR(20) COMMENT '请求方法',
    `ip` VARCHAR(80) COMMENT 'IP地址',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_operation_log_user_id` (`user_id`),
    INDEX `idx_operation_log_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';



INSERT INTO `word_book` (`user_id`, `name`, `category`, `description`, `word_count`, `type`, `status`)
SELECT NULL, '大学英语四级核心词库', 'CET4', '适合大学英语四级备考的核心高频词汇', 0, 1, 1
WHERE NOT EXISTS (SELECT 1 FROM `word_book` WHERE `name` = '大学英语四级核心词库' AND `type` = 1);

INSERT INTO `word_book` (`user_id`, `name`, `category`, `description`, `word_count`, `type`, `status`)
SELECT NULL, '大学英语六级核心词库', 'CET6', '适合大学英语六级备考的核心高频词汇', 0, 1, 1
WHERE NOT EXISTS (SELECT 1 FROM `word_book` WHERE `name` = '大学英语六级核心词库' AND `type` = 1);

INSERT INTO `word_book` (`user_id`, `name`, `category`, `description`, `word_count`, `type`, `status`)
SELECT NULL, '考研英语核心词库', '考研', '覆盖考研英语常见核心单词', 0, 1, 1
WHERE NOT EXISTS (SELECT 1 FROM `word_book` WHERE `name` = '考研英语核心词库' AND `type` = 1);

INSERT INTO `word_book` (`user_id`, `name`, `category`, `description`, `word_count`, `type`, `status`)
SELECT NULL, '雅思核心词库', '雅思', '适合雅思考试备考的场景词汇', 0, 1, 1
WHERE NOT EXISTS (SELECT 1 FROM `word_book` WHERE `name` = '雅思核心词库' AND `type` = 1);

INSERT INTO `word_book` (`user_id`, `name`, `category`, `description`, `word_count`, `type`, `status`)
SELECT NULL, '高中英语核心词库', '高中', '适合高中英语学习和高考备考的核心词汇', 0, 1, 1
WHERE NOT EXISTS (SELECT 1 FROM `word_book` WHERE `name` = '高中英语核心词库' AND `type` = 1);

INSERT INTO `word_book` (`user_id`, `name`, `category`, `description`, `word_count`, `type`, `status`)
SELECT NULL, '初中英语核心词库', '初中', '适合初中英语学习的基础核心词汇', 0, 1, 1
WHERE NOT EXISTS (SELECT 1 FROM `word_book` WHERE `name` = '初中英语核心词库' AND `type` = 1);

INSERT INTO `word` (`book_id`, `spelling`, `phonetic`, `part_of_speech`, `meaning`, `example_sentence`, `example_translation`, `difficulty`, `status`)
SELECT wb.`id`, 'abandon', '', 'v.', '放弃；抛弃；离弃', 'Never abandon your learning plan halfway.', '永远不要半途放弃你的学习计划。', 2, 1
FROM `word_book` wb WHERE wb.`name` = '大学英语四级核心词库'
ON DUPLICATE KEY UPDATE `update_time` = CURRENT_TIMESTAMP;

INSERT INTO `word` (`book_id`, `spelling`, `phonetic`, `part_of_speech`, `meaning`, `example_sentence`, `example_translation`, `difficulty`, `status`)
SELECT wb.`id`, 'benefit', '', 'n./v.', '益处；使受益', 'Daily review will benefit your memory.', '每日复习会有益于你的记忆。', 2, 1
FROM `word_book` wb WHERE wb.`name` = '大学英语四级核心词库'
ON DUPLICATE KEY UPDATE `update_time` = CURRENT_TIMESTAMP;

INSERT INTO `announcement` (`title`, `content`, `type`, `status`)
VALUES
('欢迎使用艾宾浩斯单词背诵学习系统', '系统会根据遗忘曲线为你自动安排复习任务，请坚持每日学习和打卡。', '公告', 1),
('背词小贴士：先回忆再看答案', '背诵时先主动回忆中文释义，再点击显示答案，可以显著提升记忆效果。', '贴士', 1)
ON DUPLICATE KEY UPDATE `update_time` = CURRENT_TIMESTAMP;
