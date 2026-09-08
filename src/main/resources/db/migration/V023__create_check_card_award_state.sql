CREATE TABLE `check_card_award_state` (
         `check_card_award_state_id`   BIGINT NOT NULL AUTO_INCREMENT,
         `user_id`                     BIGINT NOT NULL COMMENT '회원 ID',

         `last_awarded_year_month`     DATE NULL COMMENT '마지막으로 체크카드 가점을 지급한 기준월 (그 달의 1일로 저장)',

         CONSTRAINT `pk_check_card_award_state`
             PRIMARY KEY (`check_card_award_state_id`),

         CONSTRAINT `uk_check_card_award_state_user`
             UNIQUE (`user_id`),

         CONSTRAINT `fk_check_card_award_state_user`
             FOREIGN KEY (`user_id`)
                 REFERENCES `miraero_user` (`user_id`)
                 ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
