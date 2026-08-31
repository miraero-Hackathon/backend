ALTER TABLE youth_policy
    ADD COLUMN zip_cd VARCHAR(1500) NULL COMMENT '청년정책 API 법정시군구코드 원본';

CREATE TABLE youth_policy_region (
    youth_policy_region_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '청년정책 지역 연결 ID',
    youth_policy_id BIGINT NOT NULL COMMENT '청년정책 ID',
    region_code CHAR(2) NOT NULL COMMENT '시도 코드. 전국은 00',
    region_name VARCHAR(30) NOT NULL COMMENT '시도명 또는 전국',
    is_nationwide BOOLEAN NOT NULL DEFAULT FALSE COMMENT '전국 정책 여부',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (youth_policy_region_id),
    UNIQUE KEY uk_youth_policy_region (youth_policy_id, region_code),
    CONSTRAINT fk_youth_policy_region_youth_policy
        FOREIGN KEY (youth_policy_id) REFERENCES youth_policy (youth_policy_id)
        ON DELETE CASCADE,
    INDEX idx_youth_policy_region_code (region_code, is_nationwide)
)
    ENGINE=InnoDB
    DEFAULT CHARSET=utf8mb4;
