CREATE DATABASE IF NOT EXISTS audit_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE audit_db;

-- JPA will automatically create the table, but here is the schema for reference

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content TEXT,
    is_safe BOOLEAN,
    reason VARCHAR(1024),
    user_id BIGINT,
    username VARCHAR(255),
    create_time DATETIME(6)
);
-- 审核规则表
CREATE TABLE IF NOT EXISTS audit_rules (
                                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                           word VARCHAR(255) NOT NULL,
                                           category VARCHAR(255) NOT NULL,
                                           level VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 配置表
CREATE TABLE IF NOT EXISTS app_config (
                                          config_key VARCHAR(255) PRIMARY KEY,
                                          config_value VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS audit_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE audit_db;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     username VARCHAR(255) NOT NULL UNIQUE,
                                     password VARCHAR(255) NOT NULL,
                                     role VARCHAR(255) NOT NULL,
                                     email VARCHAR(255),
                                     phone VARCHAR(255),
                                     avatar_url VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 审核日志表
CREATE TABLE IF NOT EXISTS audit_log (
                                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                         content TEXT,
                                         is_safe BIT,
                                         category VARCHAR(255),
                                         reason VARCHAR(1024),
                                         sentiment VARCHAR(255),
                                         user_id BIGINT,
                                         username VARCHAR(255),
                                         create_time DATETIME(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 审核规则表
CREATE TABLE IF NOT EXISTS audit_rules (
                                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                           word VARCHAR(255) NOT NULL,
                                           category VARCHAR(255) NOT NULL,
                                           level VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 配置表
CREATE TABLE IF NOT EXISTS app_config (
                                          config_key VARCHAR(255) PRIMARY KEY,
                                          config_value VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 插入用户
INSERT INTO users (username, password, role, email, phone, avatar_url) VALUES
                                                                           ('admin', '123456', 'ADMIN', 'admin@example.com', '13800138000', 'https://api.example.com/avatar/1.jpg'),
                                                                           ('user1', '123456', 'USER', 'user1@example.com', '13800138001', 'https://api.example.com/avatar/2.jpg'),
                                                                           ('user2', '123456', 'USER', 'user2@example.com', '13800138002', 'https://api.example.com/avatar/3.jpg'),
                                                                           ('user3', '123456', 'USER', 'user3@example.com', '13800138003', 'https://api.example.com/avatar/4.jpg');

-- 插入审核规则
INSERT INTO audit_rules (word, category, level) VALUES
                                                    ('违法', 'Violence', 'High'),
                                                    ('暴力', 'Violence', 'High'),
                                                    ('色情', 'Adult', 'High'),
                                                    ('诈骗', 'Fraud', 'High'),
                                                    ('赌博', 'Gambling', 'High'),
                                                    ('贩毒', 'Drug', 'High'),
                                                    ('政治敏感', 'Politics', 'High'),
                                                    ('恐怖主义', 'Terrorism', 'High'),
                                                    ('仇恨言论', 'HateSpeech', 'High'),
                                                    ('种族歧视', 'Racism', 'High'),
                                                    ('性骚扰', 'SexualHarassment', 'High'),
                                                    ('隐私威胁', 'PrivacyViolation', 'High'),
                                                    ('人身攻击', 'PersonalAttack', 'Medium'),
                                                    ('骚扰', 'Harassment', 'Medium'),
                                                    ('虚假信息', 'Misinformation', 'Medium'),
                                                    ('垃圾信息', 'Spam', 'Medium'),
                                                    ('水军', 'SpamBot', 'Medium'),
                                                    ('极端主义', 'Extremism', 'Medium'),
                                                    ('分裂言论', 'Separatism', 'Medium'),
                                                    ('诬陷', 'Defamation', 'Medium'),
                                                    ('广告推销', 'Advertisement', 'Low'),
                                                    ('低俗', 'Vulgar', 'Low'),
                                                    ('营销', 'Marketing', 'Low');

-- 插入审核日志
INSERT INTO audit_log (username, user_id, content, category, is_safe, sentiment, reason, create_time) VALUES
                                                                                                          ('user1', 2, '这是一条正常的评论', 'Normal', 1, 'positive', '内容审核通过', NOW()),
                                                                                                          ('user2', 3, '很高兴能和大家分享', 'Normal', 1, 'positive', '内容审核通过', NOW()),
                                                                                                          ('user3', 4, '我对这个决定有点失望', 'Normal', 1, 'neutral', '内容审核通过', NOW()),
                                                                                                          ('user1', 2, '你这个骗子！这是诈骗行为', 'Fraud', 0, 'negative', '检测到诈骗词汇', NOW()),
                                                                                                          ('user2', 3, '来赌博赢大钱，联系我', 'Gambling', 0, 'negative', '检测到赌博推广', NOW()),
                                                                                                          ('user3', 4, '买药品找我，保证有效', 'Drug', 0, 'negative', '检测到违法药物', NOW()),
                                                                                                          ('user1', 2, '强烈抗议这个政策！', 'Politics', 0, 'negative', '检测到政治敏感词', NOW()),
                                                                                                          ('user2', 3, '恐怖分子万岁！', 'Terrorism', 0, 'negative', '检测到恐怖内容', NOW()),
                                                                                                          ('user3', 4, '这些人真是垃圾，应该消灭', 'HateSpeech', 0, 'negative', '检测到仇恨言论', NOW()),
                                                                                                          ('user1', 2, '我讨厌这个种族的人', 'Racism', 0, 'negative', '检测到种族歧视内容', NOW()),
                                                                                                          ('user2', 3, '你滚开，别骚扰我', 'SexualHarassment', 0, 'negative', '检测到性骚扰内容', NOW()),
                                                                                                          ('user3', 4, '你的私人信息我都掌握了', 'PrivacyViolation', 0, 'negative', '检测到隐私威胁', NOW()),
                                                                                                          ('user1', 2, '你就是个废物，滚出去', 'PersonalAttack', 0, 'negative', '检测到人身攻击', NOW()),
                                                                                                          ('user2', 3, '我会一直骚扰你直到你屈服', 'Harassment', 0, 'negative', '检测到骚扰威胁', NOW()),
                                                                                                          ('user3', 4, '这都是假新闻，别相信', 'Misinformation', 0, 'negative', '检测到虚假信息', NOW()),
                                                                                                          ('user1', 2, '垃圾信息垃圾信息垃圾信息', 'Spam', 0, 'negative', '检测到垃圾信息', NOW()),
                                                                                                          ('user2', 3, '看我的营销广告赚大钱', 'SpamBot', 0, 'negative', '检测到水军推广', NOW()),
                                                                                                          ('user3', 4, '极端分子应该被消灭', 'Extremism', 0, 'negative', '检测到极端主义', NOW()),
                                                                                                          ('user1', 2, '这个政策必须推翻分裂国家', 'Separatism', 0, 'negative', '检测到分裂言论', NOW()),
                                                                                                          ('user2', 3, '他就是个骗子和小人', 'Defamation', 0, 'negative', '检测到诬陷内容', NOW()),
                                                                                                          ('user3', 4, '看我的广告推销产品', 'Advertisement', 0, 'negative', '检测到广告推销', NOW()),
                                                                                                          ('user1', 2, '低俗内容不能看', 'Vulgar', 0, 'negative', '检测到低俗内容', NOW()),
                                                                                                          ('user2', 3, '营销营销全是营销', 'Marketing', 0, 'negative', '检测到营销内容', NOW()),
                                                                                                          ('user3', 4, '暴力暴力太暴力了', 'Violence', 0, 'negative', '检测到暴力内容', NOW()),
                                                                                                          ('user1', 2, '这是色情内容我不看', 'Adult', 0, 'negative', '检测到成人内容', NOW());

-- 插入配置
INSERT INTO app_config (config_key, config_value) VALUES
                                                      ('audit_enabled', 'true'),
                                                      ('sensitivity_level', 'medium'),
                                                      ('max_content_length', '10000'),
                                                      ('retention_days', '90');

SELECT * FROM audit_log;