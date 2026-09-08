CREATE TABLE `credit_score` (
    `credit_score_id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '회원 ID',
    `current_score` INT NOT NULL COMMENT '현재 신용점수 0~1000점 사이',
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,

    CONSTRAINT `pk_credit_score` PRIMARY KEY (`credit_score_id`),
    CONSTRAINT `uk_credit_score_user` UNIQUE (`user_id`),
    CONSTRAINT `fk_credit_score_user` FOREIGN KEY (`user_id`) REFERENCES `miraero_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=UTF8mb4;



CREATE TABLE `credit_score_event` (
    `credit_score_event_id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '회원 ID',
    `reason_code` VARCHAR(50) NOT NULL COMMENT '변동 사유',
    `delta` INT NOT NULL COMMENT '변동 폭',
    `score_after` INT NOT NULL COMMENT '가점, 차감 직후 점수',
    `description` VARCHAR(255) NULL COMMENT '설명',
    `occurred_at` DATETIME NOT NULL COMMENT '발생 시각',

    CONSTRAINT `pk_credit_score_event` PRIMARY KEY (`credit_score_event_id`),
    CONSTRAINT `fk_credit_score_event_user` FOREIGN KEY (`user_id`) REFERENCES `miraero_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX `idx_credit_score_event_user_occurred` ON `credit_score_event` (`user_id`, `occurred_at`);