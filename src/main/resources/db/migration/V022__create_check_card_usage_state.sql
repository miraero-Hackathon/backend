CREATE TABLE `check_card_usage_state` (
              `check_card_usage_state_id`    BIGINT NOT NULL AUTO_INCREMENT,
              `user_id`                      BIGINT NOT NULL COMMENT '회원 ID (조회 편의용 — 카드는 유저 1명 소유)',
              `card_id`                      BIGINT NOT NULL COMMENT '대상 체크카드 ID',

              `consecutive_qualified_months` INT NOT NULL DEFAULT 0 COMMENT '이 카드로 연속 월 30만원 이상 결제한 개월 수',

              CONSTRAINT `pk_check_card_usage_state`
                  PRIMARY KEY (`check_card_usage_state_id`),

              CONSTRAINT `uk_check_card_usage_state_card`
                  UNIQUE (`card_id`),

              CONSTRAINT `fk_check_card_usage_state_user`
                  FOREIGN KEY (`user_id`)
                      REFERENCES `miraero_user` (`user_id`)
                      ON DELETE CASCADE,

              CONSTRAINT `fk_check_card_usage_state_card`
                  FOREIGN KEY (`card_id`)
                      REFERENCES `card` (`card_id`)
                      ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;