CREATE TABLE `youth_policy` (
                                `youth_policy_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '내부 청년정책 ID',
                                `policy_no` VARCHAR(30) NOT NULL COMMENT '온통청년 정책번호',
                                `policy_name` VARCHAR(200) NOT NULL COMMENT '정책명',
                                `policy_keyword` VARCHAR(255) NULL COMMENT '정책 키워드',
                                `policy_description` TEXT NULL COMMENT '정책 설명',
                                `support_content` TEXT NULL COMMENT '지원 내용',

                                `provider_institution_code` VARCHAR(20) NULL COMMENT '정책 제공기관 코드',
                                `provider_institution_name` VARCHAR(100) NULL COMMENT '정책 제공기관명',

                                `application_start_date` DATE NULL COMMENT '신청 시작일',
                                `application_end_date` DATE NULL COMMENT '신청 종료일',
                                `application_period_text` VARCHAR(100) NULL COMMENT '신청기간 원문',

                                `application_method` TEXT NULL COMMENT '신청 방법',
                                `application_url` VARCHAR(2048) NULL COMMENT '신청 URL',
                                `reference_url` VARCHAR(2048) NULL COMMENT '참고 URL',

                                `min_age` INT NULL COMMENT '지원 최소 나이',
                                `max_age` INT NULL COMMENT '지원 최대 나이',

                                `income_condition_code` VARCHAR(20) NULL COMMENT '소득 조건 구분 코드',
                                `min_income` BIGINT NULL COMMENT '최소 소득',
                                `max_income` BIGINT NULL COMMENT '최대 소득',
                                `income_condition_text` TEXT NULL COMMENT '소득 조건 설명',

                                `qualification` TEXT NULL COMMENT '추가 신청 자격 및 지원 대상',

                                `synced_at` DATETIME NOT NULL COMMENT '마지막 API 동기화 일시',
                                `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                                    ON UPDATE CURRENT_TIMESTAMP,

                                PRIMARY KEY (`youth_policy_id`),
                                UNIQUE KEY `uk_youth_policy_policy_no` (`policy_no`),

                                INDEX `idx_youth_policy_application_end_date` (`application_end_date`)
)
    ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4;