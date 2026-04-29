CREATE DATABASE IF NOT EXISTS math_archive DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE math_archive;

DROP TABLE IF EXISTS knowledge_favorite_record;
DROP TABLE IF EXISTS category_plan;

CREATE TABLE IF NOT EXISTS app_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL UNIQUE,
    display_name VARCHAR(64) NOT NULL,
    password VARCHAR(128) NOT NULL,
    email VARCHAR(128),
    avatar_key VARCHAR(32) NOT NULL DEFAULT 'classic',
    avatar_image LONGTEXT,
    bio VARCHAR(255),
    role VARCHAR(16) NOT NULL,
    active BIT NOT NULL DEFAULT b'1',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

SET @avatar_key_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'app_user'
      AND COLUMN_NAME = 'avatar_key'
);
SET @avatar_key_sql := IF(
    @avatar_key_exists = 0,
    'ALTER TABLE app_user ADD COLUMN avatar_key VARCHAR(32) NOT NULL DEFAULT ''classic'' AFTER email',
    'SELECT 1'
);
PREPARE stmt_avatar_key FROM @avatar_key_sql;
EXECUTE stmt_avatar_key;
DEALLOCATE PREPARE stmt_avatar_key;

SET @bio_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'app_user'
      AND COLUMN_NAME = 'bio'
);
SET @bio_sql := IF(
    @bio_exists = 0,
    'ALTER TABLE app_user ADD COLUMN bio VARCHAR(255) NULL AFTER avatar_key',
    'SELECT 1'
);
PREPARE stmt_bio FROM @bio_sql;
EXECUTE stmt_bio;
DEALLOCATE PREPARE stmt_bio;

SET @avatar_image_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'app_user'
      AND COLUMN_NAME = 'avatar_image'
);
SET @avatar_image_sql := IF(
    @avatar_image_exists = 0,
    'ALTER TABLE app_user ADD COLUMN avatar_image LONGTEXT NULL AFTER avatar_key',
    'SELECT 1'
);
PREPARE stmt_avatar_image FROM @avatar_image_sql;
EXECUTE stmt_avatar_image;
DEALLOCATE PREPARE stmt_avatar_image;

SET @updated_at_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'app_user'
      AND COLUMN_NAME = 'updated_at'
);
SET @updated_at_sql := IF(
    @updated_at_exists = 0,
    'ALTER TABLE app_user ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER created_at',
    'SELECT 1'
);
PREPARE stmt_updated_at FROM @updated_at_sql;
EXECUTE stmt_updated_at;
DEALLOCATE PREPARE stmt_updated_at;

CREATE TABLE IF NOT EXISTS question (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    archive_code VARCHAR(32) NOT NULL UNIQUE,
    year_value INT NOT NULL,
    paper_name VARCHAR(32) NOT NULL,
    subject VARCHAR(32) NOT NULL,
    chapter_name VARCHAR(128) NOT NULL,
    knowledge_tags VARCHAR(255) NOT NULL,
    question_type VARCHAR(32) NOT NULL,
    difficulty_level VARCHAR(16) NOT NULL,
    stem_text VARCHAR(2000) NOT NULL,
    option_a VARCHAR(300),
    option_b VARCHAR(300),
    option_c VARCHAR(300),
    option_d VARCHAR(300),
    answer_text VARCHAR(1000),
    analysis_text VARCHAR(2000),
    created_date DATE NOT NULL,
    updated_at DATETIME NOT NULL
);

SET @knowledge_tags_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'question'
      AND COLUMN_NAME = 'knowledge_tags'
);
SET @knowledge_tags_sql := IF(
    @knowledge_tags_exists = 0,
    'ALTER TABLE question ADD COLUMN knowledge_tags VARCHAR(255) NOT NULL DEFAULT ''未分类'' AFTER chapter_name',
    'SELECT 1'
);
PREPARE stmt_question_tags FROM @knowledge_tags_sql;
EXECUTE stmt_question_tags;
DEALLOCATE PREPARE stmt_question_tags;

CREATE TABLE IF NOT EXISTS favorite_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    CONSTRAINT uk_favorite_user_question UNIQUE (user_id, question_id),
    CONSTRAINT fk_favorite_user FOREIGN KEY (user_id) REFERENCES app_user (id),
    CONSTRAINT fk_favorite_question FOREIGN KEY (question_id) REFERENCES question (id),
    KEY idx_favorite_user_created_at (user_id, created_at)
);

CREATE TABLE IF NOT EXISTS mistake_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    note VARCHAR(300),
    created_at DATETIME NOT NULL,
    CONSTRAINT uk_mistake_user_question UNIQUE (user_id, question_id),
    CONSTRAINT fk_mistake_user FOREIGN KEY (user_id) REFERENCES app_user (id),
    CONSTRAINT fk_mistake_question FOREIGN KEY (question_id) REFERENCES question (id),
    KEY idx_mistake_user_created_at (user_id, created_at)
);

CREATE TABLE IF NOT EXISTS practice_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    status VARCHAR(16) NOT NULL,
    source_page VARCHAR(32) NOT NULL,
    practiced_at DATETIME NOT NULL,
    CONSTRAINT fk_practice_user FOREIGN KEY (user_id) REFERENCES app_user (id),
    CONSTRAINT fk_practice_question FOREIGN KEY (question_id) REFERENCES question (id),
    KEY idx_practice_user_practiced_at (user_id, practiced_at),
    KEY idx_practice_question (question_id)
);

CREATE TABLE IF NOT EXISTS category_taxonomy (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    subject VARCHAR(32) NOT NULL,
    subject_order INT NOT NULL,
    chapter_name VARCHAR(128) NOT NULL,
    chapter_order INT NOT NULL,
    knowledge_tag VARCHAR(128),
    knowledge_order INT,
    KEY idx_category_taxonomy_subject (subject, subject_order),
    KEY idx_category_taxonomy_chapter (chapter_name, chapter_order)
);

INSERT INTO app_user (username, display_name, password, email, avatar_key, avatar_image, bio, role, active, created_at, updated_at)
SELECT 'admin', '系统管理员', 'admin', 'admin@example.com', 'night', NULL, '负责系统维护、题库整理与用户管理。', 'ADMIN', b'1', '2026-04-14 09:00:00', '2026-04-14 09:00:00'
WHERE NOT EXISTS (
    SELECT 1 FROM app_user WHERE username = 'admin'
);

INSERT INTO app_user (username, display_name, password, email, avatar_key, avatar_image, bio, role, active, created_at, updated_at)
SELECT 'student_01', '考生 张三', 'password', 'student01@example.com', 'ocean', NULL, '正在进行考研数学真题专项训练，重点突破高数与线代薄弱模块。', 'USER', b'1', '2026-04-14 09:10:00', '2026-04-14 09:10:00'
WHERE NOT EXISTS (
    SELECT 1 FROM app_user WHERE username = 'student_01'
);

DROP VIEW IF EXISTS v_user_favorites;
CREATE VIEW v_user_favorites AS
SELECT
    fr.user_id,
    fr.created_at AS collected_at,
    q.id,
    q.archive_code,
    q.year_value,
    q.paper_name,
    q.subject,
    q.chapter_name,
    q.knowledge_tags,
    q.question_type,
    q.difficulty_level,
    q.stem_text,
    q.option_a,
    q.option_b,
    q.option_c,
    q.option_d,
    DATE_FORMAT(q.created_date, '%Y-%m-%d') AS created_date,
    TRUE AS favorited,
    EXISTS (
        SELECT 1
        FROM mistake_record mr
        WHERE mr.user_id = fr.user_id
          AND mr.question_id = fr.question_id
    ) AS mistake
FROM favorite_record fr
INNER JOIN question q ON q.id = fr.question_id;

DROP VIEW IF EXISTS v_user_mistakes;
CREATE VIEW v_user_mistakes AS
SELECT
    mr.user_id,
    mr.created_at AS marked_at,
    mr.note,
    q.id,
    q.archive_code,
    q.year_value,
    q.paper_name,
    q.subject,
    q.chapter_name,
    q.knowledge_tags,
    q.question_type,
    q.difficulty_level,
    q.stem_text,
    q.option_a,
    q.option_b,
    q.option_c,
    q.option_d,
    DATE_FORMAT(q.created_date, '%Y-%m-%d') AS created_date,
    EXISTS (
        SELECT 1
        FROM favorite_record fr
        WHERE fr.user_id = mr.user_id
          AND fr.question_id = mr.question_id
    ) AS favorited,
    TRUE AS mistake
FROM mistake_record mr
INNER JOIN question q ON q.id = mr.question_id;

DROP PROCEDURE IF EXISTS sp_add_favorite;
DROP PROCEDURE IF EXISTS sp_remove_favorite;
DROP PROCEDURE IF EXISTS sp_add_mistake;
DROP PROCEDURE IF EXISTS sp_remove_mistake;

DELIMITER $$

CREATE PROCEDURE sp_add_favorite(IN p_user_id BIGINT, IN p_question_id BIGINT)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM app_user
        WHERE id = p_user_id
          AND active = b'1'
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '用户不存在或已停用';
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM question
        WHERE id = p_question_id
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '题目不存在';
    END IF;

    INSERT INTO favorite_record (user_id, question_id, created_at)
    SELECT p_user_id, p_question_id, NOW()
    FROM DUAL
    WHERE NOT EXISTS (
        SELECT 1
        FROM favorite_record
        WHERE user_id = p_user_id
          AND question_id = p_question_id
    );
END $$

CREATE PROCEDURE sp_remove_favorite(IN p_user_id BIGINT, IN p_question_id BIGINT)
BEGIN
    DELETE FROM favorite_record
    WHERE user_id = p_user_id
      AND question_id = p_question_id;
END $$

CREATE PROCEDURE sp_add_mistake(IN p_user_id BIGINT, IN p_question_id BIGINT, IN p_note VARCHAR(300))
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM app_user
        WHERE id = p_user_id
          AND active = b'1'
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '用户不存在或已停用';
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM question
        WHERE id = p_question_id
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '题目不存在';
    END IF;

    INSERT INTO mistake_record (user_id, question_id, note, created_at)
    SELECT p_user_id, p_question_id, p_note, NOW()
    FROM DUAL
    WHERE NOT EXISTS (
        SELECT 1
        FROM mistake_record
        WHERE user_id = p_user_id
          AND question_id = p_question_id
    );
END $$

CREATE PROCEDURE sp_remove_mistake(IN p_user_id BIGINT, IN p_question_id BIGINT)
BEGIN
    DELETE FROM mistake_record
    WHERE user_id = p_user_id
      AND question_id = p_question_id;
END $$

DELIMITER ;

SET @student_no_exists_v2 := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'app_user'
      AND COLUMN_NAME = 'student_no'
);
SET @student_no_sql_v2 := IF(
    @student_no_exists_v2 = 0,
    'ALTER TABLE app_user ADD COLUMN student_no VARCHAR(32) NULL AFTER email',
    'SELECT 1'
);
PREPARE stmt_student_no_v2 FROM @student_no_sql_v2;
EXECUTE stmt_student_no_v2;
DEALLOCATE PREPARE stmt_student_no_v2;

UPDATE app_user
SET student_no = 'A000001'
WHERE username = 'admin' AND (student_no IS NULL OR student_no = '');

UPDATE app_user
SET student_no = '2023123456'
WHERE username = 'student_01' AND (student_no IS NULL OR student_no = '');

SET @phone_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'app_user'
      AND COLUMN_NAME = 'phone'
);
SET @phone_sql := IF(
    @phone_exists = 0,
    'ALTER TABLE app_user ADD COLUMN phone VARCHAR(32) NULL AFTER student_no',
    'SELECT 1'
);
PREPARE stmt_phone FROM @phone_sql;
EXECUTE stmt_phone;
DEALLOCATE PREPARE stmt_phone;

SET @last_login_at_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'app_user'
      AND COLUMN_NAME = 'last_login_at'
);
SET @last_login_at_sql := IF(
    @last_login_at_exists = 0,
    'ALTER TABLE app_user ADD COLUMN last_login_at DATETIME NULL AFTER updated_at',
    'SELECT 1'
);
PREPARE stmt_last_login_at FROM @last_login_at_sql;
EXECUTE stmt_last_login_at;
DEALLOCATE PREPARE stmt_last_login_at;

SET @source_org_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'question'
      AND COLUMN_NAME = 'source_org'
);
SET @source_org_sql := IF(
    @source_org_exists = 0,
    'ALTER TABLE question ADD COLUMN source_org VARCHAR(64) NULL AFTER paper_name',
    'SELECT 1'
);
PREPARE stmt_source_org FROM @source_org_sql;
EXECUTE stmt_source_org;
DEALLOCATE PREPARE stmt_source_org;

SET @review_status_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'question'
      AND COLUMN_NAME = 'review_status'
);
SET @review_status_sql := IF(
    @review_status_exists = 0,
    'ALTER TABLE question ADD COLUMN review_status VARCHAR(16) NOT NULL DEFAULT ''APPROVED'' AFTER difficulty_level',
    'SELECT 1'
);
PREPARE stmt_review_status FROM @review_status_sql;
EXECUTE stmt_review_status;
DEALLOCATE PREPARE stmt_review_status;

SET @submitter_name_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'question'
      AND COLUMN_NAME = 'submitter_name'
);
SET @submitter_name_sql := IF(
    @submitter_name_exists = 0,
    'ALTER TABLE question ADD COLUMN submitter_name VARCHAR(64) NULL AFTER review_status',
    'SELECT 1'
);
PREPARE stmt_submitter_name FROM @submitter_name_sql;
EXECUTE stmt_submitter_name;
DEALLOCATE PREPARE stmt_submitter_name;

SET @submitted_at_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'question'
      AND COLUMN_NAME = 'submitted_at'
);
SET @submitted_at_sql := IF(
    @submitted_at_exists = 0,
    'ALTER TABLE question ADD COLUMN submitted_at DATETIME NULL AFTER submitter_name',
    'SELECT 1'
);
PREPARE stmt_submitted_at FROM @submitted_at_sql;
EXECUTE stmt_submitted_at;
DEALLOCATE PREPARE stmt_submitted_at;

SET @reject_reason_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'question'
      AND COLUMN_NAME = 'reject_reason'
);
SET @reject_reason_sql := IF(
    @reject_reason_exists = 0,
    'ALTER TABLE question ADD COLUMN reject_reason VARCHAR(255) NULL AFTER submitted_at',
    'SELECT 1'
);
PREPARE stmt_reject_reason FROM @reject_reason_sql;
EXECUTE stmt_reject_reason;
DEALLOCATE PREPARE stmt_reject_reason;

CREATE TABLE IF NOT EXISTS operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    operator_name VARCHAR(64) NOT NULL,
    operator_role VARCHAR(32) NOT NULL,
    action_type VARCHAR(64) NOT NULL,
    action_module VARCHAR(64) NOT NULL,
    action_detail VARCHAR(255) NOT NULL,
    action_status VARCHAR(16) NOT NULL,
    operated_at DATETIME NOT NULL,
    ip_address VARCHAR(32),
    KEY idx_operation_log_operated_at (operated_at),
    KEY idx_operation_log_module (action_module)
);

CREATE TABLE IF NOT EXISTS system_setting (
    setting_key VARCHAR(32) PRIMARY KEY,
    setting_value VARCHAR(512) NOT NULL,
    updated_at DATETIME NOT NULL
);

UPDATE app_user
SET phone = '13800000000',
    last_login_at = COALESCE(last_login_at, NOW())
WHERE username = 'admin';

UPDATE app_user
SET phone = '13900001111',
    last_login_at = COALESCE(last_login_at, DATE_SUB(NOW(), INTERVAL 1 DAY))
WHERE username = 'student_01';

UPDATE question
SET review_status = COALESCE(review_status, 'APPROVED'),
    submitter_name = COALESCE(NULLIF(submitter_name, ''), '题库管理员'),
    submitted_at = COALESCE(submitted_at, updated_at, NOW())
WHERE 1 = 1;

INSERT INTO system_setting (setting_key, setting_value, updated_at)
SELECT 'site_name', '考研数学真题分类管理系统', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM system_setting WHERE setting_key = 'site_name'
);

INSERT INTO system_setting (setting_key, setting_value, updated_at)
SELECT 'admin_email', 'admin@example.com', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM system_setting WHERE setting_key = 'admin_email'
);

INSERT INTO system_setting (setting_key, setting_value, updated_at)
SELECT 'contact_phone', '13800001234', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM system_setting WHERE setting_key = 'contact_phone'
);

INSERT INTO system_setting (setting_key, setting_value, updated_at)
SELECT 'site_intro', '系统提供真题管理、题目审核、学习分析和用户服务等功能。', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM system_setting WHERE setting_key = 'site_intro'
);

INSERT INTO system_setting (setting_key, setting_value, updated_at)
SELECT 'record_number', '陕ICP备12345678号-1', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM system_setting WHERE setting_key = 'record_number'
);

INSERT INTO system_setting (setting_key, setting_value, updated_at)
SELECT 'version', 'v1.0.0', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM system_setting WHERE setting_key = 'version'
);

INSERT INTO system_setting (setting_key, setting_value, updated_at)
SELECT 'publish_date', '2026-04-01', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM system_setting WHERE setting_key = 'publish_date'
);

INSERT INTO system_setting (setting_key, setting_value, updated_at)
SELECT 'team_name', '考研数学项目组', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM system_setting WHERE setting_key = 'team_name'
);

INSERT INTO operation_log (operator_name, operator_role, action_type, action_module, action_detail, action_status, operated_at, ip_address)
SELECT 'admin', '管理员', '初始化数据', '系统设置', '创建系统默认配置', '成功', NOW(), '127.0.0.1'
WHERE NOT EXISTS (
    SELECT 1 FROM operation_log
);
