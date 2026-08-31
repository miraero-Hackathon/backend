-- 마일스톤 관련 신규 테이블
-- 기존 miraero 스키마 컨벤션(pk_, fk_, uk_, ck_) 준수

CREATE TABLE `milestone` (
          `milestone_id`    BIGINT NOT NULL AUTO_INCREMENT COMMENT '마일스톤 ID',
          `goal_id`              BIGINT NOT NULL COMMENT '목표 ID',
          `milestone_percentage` INT NOT NULL COMMENT '25, 50, 75, 100',
          `milestone_amount`     BIGINT NOT NULL COMMENT 'goal_amount * percentage / 100 스냅샷',
          `achieved`             BOOLEAN NOT NULL DEFAULT FALSE,
          `achieved_at`          DATETIME NULL,
          `created_at`           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
          `updated_at`           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

          CONSTRAINT `pk_goal_milestone`
              PRIMARY KEY (`milestone_id`),

          CONSTRAINT `uk_goal_milestone_goal_percentage`
              UNIQUE (`goal_id`, `milestone_percentage`),

          CONSTRAINT `ck_goal_milestone_percentage`
              CHECK (`milestone_percentage` IN (25, 50, 75, 100)),

          CONSTRAINT `fk_goal_milestone_goal`
              FOREIGN KEY (`goal_id`)
                  REFERENCES `goal` (`goal_id`)
                  ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='목표별 마일스톤(25/50/75/100%)';

CREATE TABLE `milestone_report` (
     `milestone_report_id` BIGINT NOT NULL AUTO_INCREMENT,
     `milestone_id` BIGINT NOT NULL COMMENT '연결된 마일스톤 ID',
     `title` VARCHAR(100) NOT NULL COMMENT 'AI 리포트 제목',
     `content` TEXT NOT NULL COMMENT 'AI가 생성한 리포트 본문',
     `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING'
         COMMENT 'PENDING, COMPLETED, FAILED',
     `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
     `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
         ON UPDATE CURRENT_TIMESTAMP,

     CONSTRAINT `pk_goal_milestone_report`
         PRIMARY KEY (`milestone_report_id`),

     CONSTRAINT `uk_goal_milestone_report_milestone`
         UNIQUE (`milestone_id`),

     CONSTRAINT `ck_goal_milestone_report_status`
         CHECK (`status` IN ('PENDING', 'COMPLETED', 'FAILED')),

     CONSTRAINT `fk_goal_milestone_report_milestone`
         FOREIGN KEY (`milestone_id`)
             REFERENCES `milestone` (`milestone_id`)
             ON DELETE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
    COMMENT='마일스톤 달성 시 생성되는 AI 리포트';