ALTER TABLE `goal_delinquency_state`
    ADD COLUMN `overdue_count` INT NOT NULL DEFAULT 0
        COMMENT '단기연체(30일) 임계값을 넘은 누적 횟수' AFTER `long_term_fired`;
