CREATE TABLE `peer_spending_benchmark` (
    `peer_spending_benchmark_id` BIGINT NOT NULL AUTO_INCREMENT
        COMMENT '또래 지출 기준값 ID',
    `age_group` VARCHAR(20) NOT NULL
        COMMENT '연령대: AGE_20_24, AGE_25_29, AGE_30_34',
    `expense_category_id` BIGINT NOT NULL
        COMMENT '지출 카테고리 ID',
    `baseline_average_amount` BIGINT NOT NULL
        COMMENT '공공 통계 기반 월평균 지출액',
    `baseline_weight` INT NOT NULL DEFAULT 500
        COMMENT '공공 통계 기준값의 가상 표본 수',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        COMMENT '생성 일시',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP
        COMMENT '수정 일시',

    CONSTRAINT `pk_peer_spending_benchmark`
        PRIMARY KEY (`peer_spending_benchmark_id`),
    CONSTRAINT `uk_peer_spending_benchmark_age_category`
        UNIQUE (`age_group`, `expense_category_id`),
    CONSTRAINT `fk_peer_spending_benchmark_expense_category`
        FOREIGN KEY (`expense_category_id`)
        REFERENCES `expense_category` (`expense_category_id`)
        ON DELETE RESTRICT,
    CONSTRAINT `ck_peer_spending_benchmark_age_group`
        CHECK (`age_group` IN ('AGE_20_24', 'AGE_25_29', 'AGE_30_34')),
    CONSTRAINT `ck_peer_spending_benchmark_average_amount`
        CHECK (`baseline_average_amount` >= 0),
    CONSTRAINT `ck_peer_spending_benchmark_weight`
        CHECK (`baseline_weight` > 0)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
    COMMENT='공공 통계 기반 또래 지출 기준값';

-- 2024년 비혼 단신근로자 실태생계비 분석의 월평균 소비지출을 사용한다.
-- AGE_20_24와 AGE_25_29는 29세 이하 통계값을 공통으로 사용한다.
-- AGE_30_34는 34세 이하 통계값을 사용한다.
-- 식비와 카페는 공공 통계의 식료품·비주류음료 및 음식·숙박 합계를
-- 목서버 시드의 식비:카페 비율(약 88:12)로 나눠 중복을 방지한다.
INSERT INTO `peer_spending_benchmark` (
    `age_group`,
    `expense_category_id`,
    `baseline_average_amount`,
    `baseline_weight`
)
SELECT
    benchmark.`age_group`,
    category.`expense_category_id`,
    benchmark.`baseline_average_amount`,
    500
FROM (
    SELECT 'AGE_20_24' AS `age_group`, '교통' AS `category_name`, 206968 AS `baseline_average_amount`
    UNION ALL SELECT 'AGE_20_24', '문화', 213883
    UNION ALL SELECT 'AGE_20_24', '의료', 92493
    UNION ALL SELECT 'AGE_20_24', '쇼핑', 170230
    UNION ALL SELECT 'AGE_20_24', '기타', 166667
    UNION ALL SELECT 'AGE_20_24', '식비', 502901
    UNION ALL SELECT 'AGE_20_24', '카페', 69085
    UNION ALL SELECT 'AGE_25_29', '교통', 206968
    UNION ALL SELECT 'AGE_25_29', '문화', 213883
    UNION ALL SELECT 'AGE_25_29', '의료', 92493
    UNION ALL SELECT 'AGE_25_29', '쇼핑', 170230
    UNION ALL SELECT 'AGE_25_29', '기타', 166667
    UNION ALL SELECT 'AGE_25_29', '식비', 502901
    UNION ALL SELECT 'AGE_25_29', '카페', 69085
    UNION ALL SELECT 'AGE_30_34', '교통', 255436
    UNION ALL SELECT 'AGE_30_34', '문화', 217416
    UNION ALL SELECT 'AGE_30_34', '의료', 98188
    UNION ALL SELECT 'AGE_30_34', '쇼핑', 190469
    UNION ALL SELECT 'AGE_30_34', '기타', 154745
    UNION ALL SELECT 'AGE_30_34', '식비', 537100
    UNION ALL SELECT 'AGE_30_34', '카페', 73783
) AS benchmark
JOIN `expense_category` AS category
    ON category.`category_name` = benchmark.`category_name`;
