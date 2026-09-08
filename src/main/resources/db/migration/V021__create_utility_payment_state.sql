CREATE TABLE `utility_payment_state` (
         `utility_payment_state_id`   BIGINT NOT NULL AUTO_INCREMENT,
         `user_id`                    BIGINT NOT NULL COMMENT '회원 ID',
         `utility_type`               VARCHAR(30) NOT NULL COMMENT '항목 종류 (UtilityType enum 이름)',

         `consecutive_paid_months`    INT NOT NULL DEFAULT 0 COMMENT '연속 납부 개월 수',
-- 도시가스/수도요금만 의미 있음. 나머지 3항목은 항상 0 유지 (연체 차감 대상 아님).
         `consecutive_missed_months`  INT NOT NULL DEFAULT 0 COMMENT '연속 미납 개월 수',

         CONSTRAINT `pk_utility_payment_state`
             PRIMARY KEY (`utility_payment_state_id`),

         CONSTRAINT `uk_utility_payment_state_user_type`
             UNIQUE (`user_id`, `utility_type`),

         CONSTRAINT `fk_utility_payment_state_user`
             FOREIGN KEY (`user_id`)
                 REFERENCES `miraero_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;