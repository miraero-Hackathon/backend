-- MySQL 8.x
-- Miraero schema with PK, FK, and agreed UNIQUE constraints
-- Logical corrections applied:
-- 1) saving_option uses saving_product_id
-- 2) deposit_option uses deposit_product_id
-- 3) auto_saving_history.transacted_at is NOT NULL so daily UNIQUE works
-- 4) prepaid_instrument.external_prepaid_instrument_id is NOT NULL
-- 5) account.account_number_hash length expanded to VARCHAR(64)
-- 6) goal_account and goal_loan are integrated into goal_asset

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `ai_coach_message`;
DROP TABLE IF EXISTS `ai_coach_conversation`;
DROP TABLE IF EXISTS `auto_saving_constant_history`;
DROP TABLE IF EXISTS `auto_saving_history`;
DROP TABLE IF EXISTS `auto_saving`;
DROP TABLE IF EXISTS `auto_transfer`;
DROP TABLE IF EXISTS `expense_category_target`;
DROP TABLE IF EXISTS `category`;
DROP TABLE IF EXISTS `transaction`;
DROP TABLE IF EXISTS `goal_asset`;
DROP TABLE IF EXISTS `money_box`;
DROP TABLE IF EXISTS `goal`;
DROP TABLE IF EXISTS `loan`;
DROP TABLE IF EXISTS `card`;
DROP TABLE IF EXISTS `prepaid_instrument`;
DROP TABLE IF EXISTS `account`;
DROP TABLE IF EXISTS `mydata_connection`;
DROP TABLE IF EXISTS `mydata_consent`;
DROP TABLE IF EXISTS `refresh_token`;
DROP TABLE IF EXISTS `saving_option`;
DROP TABLE IF EXISTS `saving_product`;
DROP TABLE IF EXISTS `deposit_option`;
DROP TABLE IF EXISTS `deposit_product`;
DROP TABLE IF EXISTS `financial_institution`;
DROP TABLE IF EXISTS `miraero_user`;


SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE `miraero_user` (
                                `user_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '회원 ID',
                                `name` VARCHAR(30) NOT NULL COMMENT '닉네임',
                                `birth_date` DATE NULL COMMENT '생년월일',
                                `company_name` VARCHAR(100) NULL COMMENT '직장명',
                                `monthly_income` BIGINT NULL COMMENT '월 소득',
                                `email` VARCHAR(30) NOT NULL,
                                `password_hash` VARCHAR(255) NOT NULL,
                                `kb_pay_id` BIGINT NULL,
                                `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '가입 일시',

                                CONSTRAINT `pk_user`
                                    PRIMARY KEY (`user_id`),

                                CONSTRAINT `uk_user_email`
                                    UNIQUE (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `refresh_token` (
                                 `refresh_token_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Refresh Token ID',
                                 `user_id` BIGINT NOT NULL COMMENT '회원 ID',
                                 `token_hash` VARCHAR(255) NOT NULL COMMENT 'Refresh Token 해시값',
                                 `expires_at` DATETIME NOT NULL COMMENT '토큰 만료 일시',
                                 `revoked_at` DATETIME NULL COMMENT '토큰 폐기 일시',
                                 `last_used_at` DATETIME NULL COMMENT '마지막 사용 일시',
                                 `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '발급 일시',

                                 CONSTRAINT `pk_refresh_token`
                                     PRIMARY KEY (`refresh_token_id`),

                                 CONSTRAINT `uk_refresh_token_hash`
                                     UNIQUE (`token_hash`),

                                 CONSTRAINT `fk_refresh_token_user`
                                     FOREIGN KEY (`user_id`)
                                         REFERENCES `miraero_user` (`user_id`)
                                         ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `financial_institution` (
                                         `financial_institution_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '금융기관 ID',
                                         `financial_institution_code` VARCHAR(10) NOT NULL,
                                         `financial_institution_name` VARCHAR(100) NOT NULL COMMENT '금융기관명',
                                         `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
                                         `updated_at` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                                         CONSTRAINT `pk_financial_institution`
                                             PRIMARY KEY (`financial_institution_id`),

                                         CONSTRAINT `uk_financial_institution_code`
                                             UNIQUE (`financial_institution_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `saving_product` (
                                  `saving_product_id` BIGINT NOT NULL AUTO_INCREMENT,
                                  `financial_institution_id` BIGINT NOT NULL COMMENT '금융기관 ID',
                                  `product_code` VARCHAR(50) NOT NULL,
                                  `product_name` VARCHAR(100) NOT NULL,
                                  `join_method` TEXT NULL,
                                  `join_target` TEXT NULL,
                                  `join_restriction` CHAR(1) NULL COMMENT '1: 제한없음, 2: 서민전용, 3: 일부제한',
                                  `special_condition` TEXT NULL,
                                  `maturity_interest` TEXT NULL,
                                  `max_limit` BIGINT NULL,
                                  `notice` TEXT NULL,
                                  `disclosure_month` CHAR(6) NOT NULL,
                                  `disclosure_start_date` DATE NULL,
                                  `disclosure_end_date` DATE NULL,
                                  `submitted_at` DATETIME NULL COMMENT '금융회사가 금감원에 데이터를 제출한 날짜 정보',
                                  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                                  CONSTRAINT `pk_saving_product`
                                      PRIMARY KEY (`saving_product_id`),

                                  CONSTRAINT `uk_saving_product_institution_code`
                                      UNIQUE (`financial_institution_id`, `product_code`),

                                  CONSTRAINT `fk_saving_product_financial_institution`
                                      FOREIGN KEY (`financial_institution_id`)
                                          REFERENCES `financial_institution` (`financial_institution_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `saving_option` (
                                 `saving_option_id` BIGINT NOT NULL AUTO_INCREMENT,
                                 `saving_product_id` BIGINT NOT NULL,
                                 `interest_rate_type` VARCHAR(20) NOT NULL COMMENT 'S: 단리, M: 복리',
                                 `reserve_type` VARCHAR(20) NOT NULL COMMENT 'S: 정액적립식, F: 자유적립식',
                                 `save_term` INT NOT NULL,
                                 `base_interest_rate` DECIMAL(4,2) NULL,
                                 `max_interest_rate` DECIMAL(4,2) NULL,
                                 `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                                 CONSTRAINT `pk_saving_option`
                                     PRIMARY KEY (`saving_option_id`),

                                 CONSTRAINT `uk_saving_option_condition`
                                     UNIQUE (
                                             `saving_product_id`,
                                             `interest_rate_type`,
                                             `reserve_type`,
                                             `save_term`
                                     ),

                                 CONSTRAINT `fk_saving_option_saving_product`
                                     FOREIGN KEY (`saving_product_id`)
                                         REFERENCES `saving_product` (`saving_product_id`)
                                         ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `deposit_product` (
                                   `deposit_product_id` BIGINT NOT NULL AUTO_INCREMENT,
                                   `financial_institution_id` BIGINT NOT NULL COMMENT '금융기관 ID',
                                   `product_code` VARCHAR(50) NOT NULL,
                                   `product_name` VARCHAR(100) NOT NULL,
                                   `join_method` TEXT NULL,
                                   `join_target` TEXT NULL,
                                   `join_restriction` CHAR(1) NULL COMMENT '1: 제한없음, 2: 서민전용, 3: 일부제한',
                                   `special_condition` TEXT NULL,
                                   `maturity_interest` TEXT NULL,
                                   `max_limit` BIGINT NULL,
                                   `notice` TEXT NULL,
                                   `disclosure_month` CHAR(6) NOT NULL,
                                   `disclosure_start_date` DATE NULL,
                                   `disclosure_end_date` DATE NULL,
                                   `submitted_at` DATETIME NULL COMMENT '금융회사가 금감원에 데이터를 제출한 날짜 정보',
                                   `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                                   CONSTRAINT `pk_deposit_product`
                                       PRIMARY KEY (`deposit_product_id`),

                                   CONSTRAINT `uk_deposit_product_institution_code`
                                       UNIQUE (`financial_institution_id`, `product_code`),

                                   CONSTRAINT `fk_deposit_product_financial_institution`
                                       FOREIGN KEY (`financial_institution_id`)
                                           REFERENCES `financial_institution` (`financial_institution_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `deposit_option` (
                                  `deposit_option_id` BIGINT NOT NULL AUTO_INCREMENT,
                                  `deposit_product_id` BIGINT NOT NULL,
                                  `interest_rate_type` VARCHAR(20) NOT NULL COMMENT 'S: 단리, M: 복리',
                                  `save_term` INT NOT NULL,
                                  `base_interest_rate` DECIMAL(4,2) NULL,
                                  `max_interest_rate` DECIMAL(4,2) NULL,
                                  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                                  CONSTRAINT `pk_deposit_option`
                                      PRIMARY KEY (`deposit_option_id`),

                                  CONSTRAINT `uk_deposit_option_condition`
                                      UNIQUE (
                                              `deposit_product_id`,
                                              `interest_rate_type`,
                                              `save_term`
                                      ),

                                  CONSTRAINT `fk_deposit_option_deposit_product`
                                      FOREIGN KEY (`deposit_product_id`)
                                          REFERENCES `deposit_product` (`deposit_product_id`)
                                          ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `mydata_consent` (
                                  `user_id` BIGINT NOT NULL COMMENT '회원 ID',
                                  `terms_version` VARCHAR(30) NOT NULL COMMENT '동의 약관 버전',
                                  `agreed_at` DATETIME NOT NULL COMMENT '동의 일시',
                                  `expires_at` DATETIME NOT NULL COMMENT '전송요구 만료 일시',
                                  `revoked_at` DATETIME NULL COMMENT '동의 철회 일시',
                                  `agree_status` VARCHAR(10) NOT NULL,

                                  CONSTRAINT `ck_mydata_consent_status`
                                      CHECK (`agree_status` IN (
                                                                'AGREED',
                                                                'REVOKED'
                                          )),

                                  CONSTRAINT `pk_mydata_consent`
                                      PRIMARY KEY (`user_id`),

                                  CONSTRAINT `fk_mydata_consent_user`
                                      FOREIGN KEY (`user_id`)
                                          REFERENCES `miraero_user` (`user_id`)
                                          ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `mydata_connection` (
                                     `connection_id` BIGINT NOT NULL AUTO_INCREMENT,
                                     `user_id` BIGINT NOT NULL COMMENT '회원 ID',
                                     `financial_institution_id` BIGINT NOT NULL COMMENT '금융기관 ID',
                                     `connection_status` VARCHAR(20) NOT NULL
                                         COMMENT 'CONNECTED, EXPIRED, REVOKED',
                                     `agreed_at` DATETIME NOT NULL,
                                     `expires_at` DATETIME NOT NULL,
                                     `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     `updated_at` DATETIME NOT NULL
                                                                    DEFAULT CURRENT_TIMESTAMP
                                         ON UPDATE CURRENT_TIMESTAMP,

                                     CONSTRAINT `pk_mydata_connection`
                                         PRIMARY KEY (`connection_id`),

                                     CONSTRAINT `uk_mydata_connection_user_institution`
                                         UNIQUE (`user_id`, `financial_institution_id`),

                                     CONSTRAINT `fk_mydata_connection_user`
                                         FOREIGN KEY (`user_id`)
                                             REFERENCES `miraero_user` (`user_id`)
                                             ON DELETE CASCADE,

                                     CONSTRAINT `fk_mydata_connection_financial_institution`
                                         FOREIGN KEY (`financial_institution_id`)
                                             REFERENCES `financial_institution` (`financial_institution_id`),

                                     CONSTRAINT `ck_connection_status`
                                         CHECK (`connection_status` IN (
                                                                        'CONNECTED',
                                                                        'EXPIRED',
                                                                        'REVOKED'
                                             ))

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `account` (
                           `account_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '내부 계좌 ID',
                           `user_id` BIGINT NOT NULL COMMENT '회원 ID',
                           `financial_institution_id` BIGINT NOT NULL COMMENT '금융기관 ID',

                           `ex_account_id` BIGINT NOT NULL COMMENT '외부 금융기관 계좌 ID',

                           `account_type` VARCHAR(30) NOT NULL COMMENT '계좌 유형',
                           `account_name` VARCHAR(100) NOT NULL COMMENT '계좌 또는 금융상품명',

                           `account_number` VARBINARY(512) NOT NULL
                               COMMENT '암호화된 계좌번호',

                           `account_number_hash` VARCHAR(64) NOT NULL
                               COMMENT '검색 및 중복 확인용 HMAC-SHA256 HEX',

                           `masked_account_number` VARCHAR(30) NOT NULL
                               COMMENT '마스킹된 계좌번호',

                           `balance` BIGINT NULL COMMENT '현재 잔액 또는 평가금액',
                           `account_status` VARCHAR(20) NOT NULL COMMENT '계좌 상태',

                           `opened_at` DATE NOT NULL COMMENT '개설일',
                           `maturity_at` DATE NULL COMMENT '만기일',

                           `interest_rate` DECIMAL(7,4) NULL COMMENT '금리',
                           `monthly_payment_limit` BIGINT NULL COMMENT '월 납입 한도',

                           `created_at` DATETIME NOT NULL
                               DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',

                           `updated_at` DATETIME NOT NULL
                               DEFAULT CURRENT_TIMESTAMP
                               ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',

                           `synced_at` DATETIME NOT NULL
                               DEFAULT CURRENT_TIMESTAMP
                               ON UPDATE CURRENT_TIMESTAMP COMMENT '마지막 동기화 일시',

                           CONSTRAINT `ck_account_type`
                               CHECK (`account_type` IN (
                                                         'CHECKING',
                                                         'SAVINGS',
                                                         'DEPOSIT',
                                                         'INSTALLMENT',
                                                         'ISA',
                                                         'CMA'
                                   )),

                           CONSTRAINT `pk_account`
                               PRIMARY KEY (`account_id`),

                           CONSTRAINT `uk_account_ex_account_id`
                               UNIQUE (`ex_account_id`),

                           CONSTRAINT `fk_account_user`
                               FOREIGN KEY (`user_id`)
                                   REFERENCES `miraero_user` (`user_id`)
                                   ON DELETE CASCADE,

                           CONSTRAINT `fk_account_financial_institution`
                               FOREIGN KEY (`financial_institution_id`)
                                   REFERENCES `financial_institution` (`financial_institution_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `card` (
                        `card_id` BIGINT NOT NULL AUTO_INCREMENT,
                        `user_id` BIGINT NOT NULL COMMENT '회원 ID',
                        `financial_institution_id` BIGINT NOT NULL COMMENT '금융기관 ID',

                        `ex_card_id` BIGINT NOT NULL COMMENT '외부 금융기관 카드 ID',

                        `card_name` VARCHAR(100) NOT NULL COMMENT '카드 상품명',
                        `card_type` VARCHAR(20) NOT NULL
                            COMMENT 'CREDIT, CHECK, PREPAID',

                        `synced_at` DATETIME NOT NULL
                            DEFAULT CURRENT_TIMESTAMP COMMENT '마지막 동기화 일시',

                        `created_at` DATETIME NOT NULL
                            DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',

                        `updated_at` DATETIME NOT NULL
                            DEFAULT CURRENT_TIMESTAMP
                            ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',

                        CONSTRAINT `ck_card_type`
                            CHECK (`card_type` IN (
                                                   'CREDIT',
                                                   'CHECK',
                                                   'PREPAID'
                                )),

                        CONSTRAINT `pk_card`
                            PRIMARY KEY (`card_id`),

                        CONSTRAINT `uk_card_ex_card_id`
                            UNIQUE (`ex_card_id`),

                        CONSTRAINT `fk_card_user`
                            FOREIGN KEY (`user_id`)
                                REFERENCES `miraero_user` (`user_id`)
                                ON DELETE CASCADE,

                        CONSTRAINT `fk_card_financial_institution`
                            FOREIGN KEY (`financial_institution_id`)
                                REFERENCES `financial_institution` (`financial_institution_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `prepaid_instrument` (
                                      `prepaid_instrument_id` BIGINT NOT NULL AUTO_INCREMENT,
                                      `user_id` BIGINT NOT NULL COMMENT '회원 ID',
                                      `financial_institution_id` BIGINT NOT NULL COMMENT '금융기관 ID',

                                      `prepaid_instrument_name` VARCHAR(100) NOT NULL
                                          COMMENT '카카오페이머니, 네이버페이머니 등',

                                      `prepaid_instrument_type` VARCHAR(30) NOT NULL
                                          COMMENT 'PAY_MONEY, CASH, POINT, PREPAID_CARD',

                                      `ex_prepaid_instrument_id` BIGINT NOT NULL
                                          COMMENT '외부 선불전자지급수단 ID',

                                      `synced_at` DATETIME NOT NULL
                                          DEFAULT CURRENT_TIMESTAMP COMMENT '마지막 동기화 일시',

                                      `created_at` DATETIME NOT NULL
                                          DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',

                                      `updated_at` DATETIME NOT NULL
                                          DEFAULT CURRENT_TIMESTAMP
                                          ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',

                                      CONSTRAINT `ck_prepaid_instrument_type`
                                          CHECK (`prepaid_instrument_type` IN (
                                                                               'PAY_MONEY',
                                                                               'CASH',
                                                                               'POINT',
                                                                               'PREPAID_CARD'
                                              )),

                                      CONSTRAINT `pk_prepaid_instrument`
                                          PRIMARY KEY (`prepaid_instrument_id`),

                                      CONSTRAINT `uk_prepaid_instrument_ex_id`
                                          UNIQUE (`ex_prepaid_instrument_id`),

                                      CONSTRAINT `fk_prepaid_instrument_user`
                                          FOREIGN KEY (`user_id`)
                                              REFERENCES `miraero_user` (`user_id`)
                                              ON DELETE CASCADE,

                                      CONSTRAINT `fk_prepaid_instrument_financial_institution`
                                          FOREIGN KEY (`financial_institution_id`)
                                              REFERENCES `financial_institution` (`financial_institution_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `loan` (
                        `loan_id` BIGINT NOT NULL AUTO_INCREMENT,
                        `user_id` BIGINT NOT NULL COMMENT '회원 ID',

                        `ex_loan_id` BIGINT NOT NULL COMMENT '외부 금융기관 대출 ID',

                        `loan_name` VARCHAR(40) NOT NULL,
                        `loan_amount` BIGINT NOT NULL COMMENT '최초 대출 금액',
                        `remaining_amount` BIGINT NOT NULL COMMENT '남은 대출 금액',
                        `interest_rate` DECIMAL(7,4) NOT NULL COMMENT '대출 금리',

                        `loan_start_date` DATE NOT NULL COMMENT '대출 실행일',
                        `maturity_date` DATE NOT NULL COMMENT '만기일',

                        `created_at` DATETIME NOT NULL
                            DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',

                        `updated_at` DATETIME NOT NULL
                            DEFAULT CURRENT_TIMESTAMP
                            ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',

                        `synced_at` DATETIME NOT NULL
                            DEFAULT CURRENT_TIMESTAMP COMMENT '마지막 동기화 일시',

                        CONSTRAINT `pk_loan`
                            PRIMARY KEY (`loan_id`),

                        CONSTRAINT `uk_loan_ex_loan_id`
                            UNIQUE (`ex_loan_id`),

                        CONSTRAINT `fk_loan_user`
                            FOREIGN KEY (`user_id`)
                                REFERENCES `miraero_user` (`user_id`)
                                ON DELETE CASCADE,

                        CONSTRAINT `ck_loan_amount`
                            CHECK (`loan_amount` > 0),

                        CONSTRAINT `ck_loan_remaining_amount`
                            CHECK (`remaining_amount` >= 0),

                        CONSTRAINT `ck_loan_remaining_less_than_amount`
                            CHECK (`remaining_amount` <= `loan_amount`),

                        CONSTRAINT `ck_loan_interest_rate`
                            CHECK (`interest_rate` >= 0),

                        CONSTRAINT `ck_loan_date`
                            CHECK (`loan_start_date` <= `maturity_date`)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `goal` (
                        `goal_id` BIGINT NOT NULL AUTO_INCREMENT,
                        `user_id` BIGINT NOT NULL COMMENT '회원 ID',
                        `goal_name` VARCHAR(100) NOT NULL,
                        `goal_amount` BIGINT NOT NULL,
                        `start_amount` BIGINT NOT NULL DEFAULT 0,
                        `start_date` DATE NOT NULL,
                        `goal_date` DATE NOT NULL,
                        `goal_status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE, PAUSED, COMPLETED',
                        `goal_type` VARCHAR(20) NOT NULL,
                        `is_collected` BOOLEAN NOT NULL DEFAULT FALSE,
                        `completed_date` DATE NULL,
                        `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                        CONSTRAINT `ck_goal_status`
                            CHECK (`goal_status` IN (
                                                     'ACTIVE',
                                                     'PAUSED',
                                                     'COMPLETED'
                                )),

                        CONSTRAINT `pk_goal`
                            PRIMARY KEY (`goal_id`),

                        CONSTRAINT `fk_goal_user`
                            FOREIGN KEY (`user_id`)
                                REFERENCES `miraero_user` (`user_id`)
                                ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `money_box` (
                             `money_box_id` BIGINT NOT NULL AUTO_INCREMENT,
                             `user_id` BIGINT NOT NULL COMMENT '회원 ID',
                             `balance` BIGINT NOT NULL,
                             `account_number_hash` VARCHAR(64) NOT NULL,
                             `account_number` VARBINARY(512) NOT NULL,
                             `masked_account_number` VARCHAR(50) NULL,
                             `money_box_type` VARCHAR(20) NOT NULL COMMENT 'GOAL, SAVING',

                             CONSTRAINT `ck_money_box_type`
                                 CHECK (`money_box_type` IN (
                                                             'GOAL',
                                                             'SAVING'
                                     )),

                             CONSTRAINT `pk_money_box`
                                 PRIMARY KEY (`money_box_id`),

                             CONSTRAINT `fk_money_box_user`
                                 FOREIGN KEY (`user_id`)
                                     REFERENCES `miraero_user` (`user_id`)
                                     ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `goal_asset` (
                              `goal_asset_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '목표 자산 ID',
                              `goal_id` BIGINT NOT NULL COMMENT '목표 ID',
                              `asset_type` VARCHAR(20) NOT NULL COMMENT 'ACCOUNT, LOAN, MONEY_BOX',
                              `asset_id` BIGINT NOT NULL COMMENT '연결된 자산 ID',
                              `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',

                              CONSTRAINT `ck_goal_asset_type`
                                  CHECK (`asset_type` IN (
                                                          'ACCOUNT',
                                                          'LOAN',
                                                          'MONEY_BOX'
                                      )),

                              CONSTRAINT `pk_goal_asset`
                                  PRIMARY KEY (`goal_asset_id`),

                              CONSTRAINT `uk_goal_asset`
                                  UNIQUE (`goal_id`, `asset_type`, `asset_id`),

                              CONSTRAINT `fk_goal_asset_goal`
                                  FOREIGN KEY (`goal_id`)
                                      REFERENCES `goal` (`goal_id`)
                                      ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `auto_transfer` (
                                 `auto_transfer_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '자동이체 ID',
                                 `withdrawal_account_id` BIGINT NOT NULL COMMENT '출금 계좌 ID',
                                 `deposit_account_id` BIGINT NULL COMMENT '입금 계좌 ID',
                                 `money_box_id` BIGINT NULL,
                                 `deposit_institution_name` VARCHAR(100) NULL COMMENT '입금 금융기관명',
                                 `masked_deposit_account` VARCHAR(30) NULL COMMENT '마스킹된 입금 계좌번호',
                                 `transfer_amount` BIGINT NOT NULL COMMENT '자동이체 금액',
                                 `transfer_day` INT NOT NULL COMMENT '매월 자동이체 예정일',
                                 `start_date` DATE NULL COMMENT '자동이체 시작일',
                                 `end_date` DATE NULL COMMENT '자동이체 종료일',
                                 `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '데이터 기준 일시',
                                 `auto_transfer_status` VARCHAR(20) NOT NULL,

                                 CONSTRAINT `ck_auto_transfer_status`
                                     CHECK (`auto_transfer_status` IN (
                                                                       'ACTIVE',
                                                                       'PAUSED',
                                                                       'ENDED'
                                         )),

                                 CONSTRAINT `pk_auto_transfer`
                                     PRIMARY KEY (`auto_transfer_id`),

                                 CONSTRAINT `fk_auto_transfer_withdrawal_account`
                                     FOREIGN KEY (`withdrawal_account_id`)
                                         REFERENCES `account` (`account_id`),

                                 CONSTRAINT `fk_auto_transfer_deposit_account`
                                     FOREIGN KEY (`deposit_account_id`)
                                         REFERENCES `account` (`account_id`),

                                 CONSTRAINT `fk_auto_transfer_money_box`
                                     FOREIGN KEY (`money_box_id`)
                                         REFERENCES `money_box` (`money_box_id`)
                                         ON DELETE SET NULL,

                                 CONSTRAINT `ck_auto_transfer_day`
                                     CHECK (`transfer_day` BETWEEN 1 AND 31)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `auto_saving` (
                               `auto_saving_id` BIGINT NOT NULL AUTO_INCREMENT,
                               `user_id` BIGINT NOT NULL COMMENT '회원 ID',
                               `money_box_id` BIGINT NOT NULL,
                               `account_id` BIGINT NOT NULL COMMENT '내부 출금 계좌 ID',
                               `max_amount` BIGINT NULL,
                               `auto_saving_status` VARCHAR(20) NOT NULL COMMENT 'ACTIVE, PAUSED',

                               CONSTRAINT `ck_auto_saving_status`
                                   CHECK (`auto_saving_status` IN (
                                                                   'ACTIVE',
                                                                   'PAUSED'
                                       )),

                               CONSTRAINT `pk_auto_saving`
                                   PRIMARY KEY (`auto_saving_id`),

                               CONSTRAINT `uk_auto_saving_user`
                                   UNIQUE (`user_id`),

                               CONSTRAINT `uk_auto_saving_money_box`
                                   UNIQUE (`money_box_id`),

                               CONSTRAINT `uk_auto_saving_account`
                                   UNIQUE (`account_id`),

                               CONSTRAINT `fk_auto_saving_user`
                                   FOREIGN KEY (`user_id`)
                                       REFERENCES `miraero_user` (`user_id`)
                                       ON DELETE CASCADE,

                               CONSTRAINT `fk_auto_saving_money_box`
                                   FOREIGN KEY (`money_box_id`)
                                       REFERENCES `money_box` (`money_box_id`),

                               CONSTRAINT `fk_auto_saving_account`
                                   FOREIGN KEY (`account_id`)
                                       REFERENCES `account` (`account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `auto_saving_history` (
                                       `auto_saving_history_id` BIGINT NOT NULL AUTO_INCREMENT,
                                       `money_box_id` BIGINT NOT NULL,
                                       `auto_saving_id` BIGINT NOT NULL,
                                       `amount` BIGINT NOT NULL,
                                       `transacted_at` DATE NOT NULL,

                                       CONSTRAINT `pk_auto_saving_history`
                                           PRIMARY KEY (`auto_saving_history_id`),

                                       CONSTRAINT `uk_auto_saving_history_date`
                                           UNIQUE (`auto_saving_id`, `transacted_at`),

                                       CONSTRAINT `fk_auto_saving_history_money_box`
                                           FOREIGN KEY (`money_box_id`)
                                               REFERENCES `money_box` (`money_box_id`),

                                       CONSTRAINT `fk_auto_saving_history_auto_saving`
                                           FOREIGN KEY (`auto_saving_id`)
                                               REFERENCES `auto_saving` (`auto_saving_id`)
                                               ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `expense_category` (
                                    `expense_category_id` BIGINT NOT NULL AUTO_INCREMENT
                                        COMMENT '지출 카테고리 ID',

                                    `category_name` VARCHAR(50) NOT NULL
                                        COMMENT '지출 카테고리명',

                                    CONSTRAINT `pk_expense_category`
                                        PRIMARY KEY (`expense_category_id`),

                                    CONSTRAINT `uk_expense_category_name`
                                        UNIQUE (`category_name`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
    COMMENT='지출 카테고리';

CREATE TABLE `transaction` (
                               `transaction_id` BIGINT NOT NULL AUTO_INCREMENT
                                   COMMENT '내부 거래 ID',

                               `user_id` BIGINT NOT NULL
                                   COMMENT '회원 ID',

                               `account_id` BIGINT NULL
                                   COMMENT '연결된 금융계좌 ID',

                               `prepaid_instrument_id` BIGINT NULL
                                   COMMENT '연결된 선불전자지급수단 ID',

                               `card_id` BIGINT NULL
                                   COMMENT '연결된 카드 ID',

                               `expense_category_id` BIGINT NULL
                                   COMMENT '지출 카테고리 ID',

                               `ex_transaction_id` BIGINT NOT NULL
                                   COMMENT '외부 금융기관 거래 ID',

                               `transaction_type` VARCHAR(30) NOT NULL
                                   COMMENT '거래 유형',

                               `amount` BIGINT NOT NULL
                                   COMMENT '거래금액',

                               `balance_after` BIGINT NULL
                                   COMMENT '거래 후 잔액',

                               `transacted_at` DATETIME NOT NULL
                                   COMMENT '거래 일시',

                               `merchant_name` VARCHAR(100) NULL
                                   COMMENT '가맹점명 또는 거래처명',

                               `created_at` DATETIME NOT NULL
                                   DEFAULT CURRENT_TIMESTAMP
                                   COMMENT '생성 일시',

                               `updated_at` DATETIME NOT NULL
                                   DEFAULT CURRENT_TIMESTAMP
                                   ON UPDATE CURRENT_TIMESTAMP
                                   COMMENT '수정 일시',

                               `synced_at` DATETIME NOT NULL
                                   DEFAULT CURRENT_TIMESTAMP COMMENT '마지막 동기화 일시',

                               CONSTRAINT `pk_transaction`
                                   PRIMARY KEY (`transaction_id`),

                               CONSTRAINT `uk_transaction_user_ex_transaction`
                                   UNIQUE (`user_id`, `ex_transaction_id`),

                               CONSTRAINT `ck_transaction_type`
                                   CHECK (
                                       `transaction_type` IN (
                                                              'DEPOSIT',
                                                              'WITHDRAWAL',
                                                              'PAYMENT',
                                                              'REFUND',
                                                              'TRANSFER'
                                           )
                                       ),

                               CONSTRAINT `ck_transaction_amount`
                                   CHECK (`amount` >= 0),

                               INDEX `idx_transaction_user_transacted_at`
                                   (`user_id`, `transacted_at`),

                               INDEX `idx_transaction_user_category_transacted_at`
                                   (`user_id`, `expense_category_id`, `transacted_at`),

                               CONSTRAINT `fk_transaction_user`
                                   FOREIGN KEY (`user_id`)
                                       REFERENCES `miraero_user` (`user_id`)
                                       ON DELETE CASCADE,

                               CONSTRAINT `fk_transaction_account`
                                   FOREIGN KEY (`account_id`)
                                       REFERENCES `account` (`account_id`)
                                       ON DELETE SET NULL,

                               CONSTRAINT `fk_transaction_prepaid_instrument`
                                   FOREIGN KEY (`prepaid_instrument_id`)
                                       REFERENCES `prepaid_instrument` (`prepaid_instrument_id`)
                                       ON DELETE SET NULL,

                               CONSTRAINT `fk_transaction_card`
                                   FOREIGN KEY (`card_id`)
                                       REFERENCES `card` (`card_id`)
                                       ON DELETE SET NULL,

                               CONSTRAINT `fk_transaction_expense_category`
                                   FOREIGN KEY (`expense_category_id`)
                                       REFERENCES `expense_category` (`expense_category_id`)
                                       ON DELETE SET NULL
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
    COMMENT='사용자 금융 거래내역';

CREATE TABLE `expense_category_target` (
                                           `expense_category_target_id` BIGINT NOT NULL AUTO_INCREMENT
                                               COMMENT '카테고리별 목표 지출 ID',

                                           `user_id` BIGINT NOT NULL
                                               COMMENT '회원 ID',

                                           `expense_category_id` BIGINT NOT NULL
                                               COMMENT '지출 카테고리 ID',

                                           `target_amount` BIGINT NOT NULL
                                               COMMENT '목표 지출금액',

                                           `created_at` DATETIME NOT NULL
                                               DEFAULT CURRENT_TIMESTAMP
                                               COMMENT '생성 일시',

                                           `updated_at` DATETIME NOT NULL
                                               DEFAULT CURRENT_TIMESTAMP
                                               ON UPDATE CURRENT_TIMESTAMP
                                               COMMENT '수정 일시',

                                           CONSTRAINT `pk_expense_category_target`
                                               PRIMARY KEY (`expense_category_target_id`),

                                           CONSTRAINT `uk_expense_category_target_user_category`
                                               UNIQUE (`user_id`, `expense_category_id`),

                                           CONSTRAINT `ck_expense_category_target_amount`
                                               CHECK (`target_amount` >= 0),

                                           INDEX `idx_expense_category_target_user`
                                               (`user_id`),

                                           CONSTRAINT `fk_expense_category_target_user`
                                               FOREIGN KEY (`user_id`)
                                                   REFERENCES `miraero_user` (`user_id`)
                                                   ON DELETE CASCADE,

                                           CONSTRAINT `fk_expense_category_target_category`
                                               FOREIGN KEY (`expense_category_id`)
                                                   REFERENCES `expense_category` (`expense_category_id`)
                                                   ON DELETE RESTRICT
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
    COMMENT='사용자 카테고리별 목표 지출';

CREATE TABLE `ai_coach_conversation` (
                                         `ai_coach_conversation_id` BIGINT NOT NULL AUTO_INCREMENT,
                                         `user_id` BIGINT NOT NULL COMMENT '회원 ID',
                                         `title` VARCHAR(100) NULL,
                                         `conversation_summary` TEXT NULL,
                                         `last_message_at` DATETIME NULL,
                                         `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                         `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                                         CONSTRAINT `pk_ai_coach_conversation`
                                             PRIMARY KEY (`ai_coach_conversation_id`),

                                         CONSTRAINT `fk_ai_coach_conversation_user`
                                             FOREIGN KEY (`user_id`)
                                                 REFERENCES `miraero_user` (`user_id`)
                                                 ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `ai_coach_message` (
                                    `ai_coach_message_id` BIGINT NOT NULL AUTO_INCREMENT,
                                    `ai_coach_conversation_id` BIGINT NOT NULL,
                                    `sender_type` VARCHAR(20) NOT NULL COMMENT 'USER, ASSISTANT',
                                    `content` TEXT NOT NULL,
                                    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                    CONSTRAINT `pk_ai_coach_message`
                                        PRIMARY KEY (`ai_coach_message_id`),

                                    CONSTRAINT `fk_ai_coach_message_conversation`
                                        FOREIGN KEY (`ai_coach_conversation_id`)
                                            REFERENCES `ai_coach_conversation` (`ai_coach_conversation_id`)
                                            ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
