-- 지출 카테고리 유형 추가
ALTER TABLE expense_category
    ADD COLUMN expense_type VARCHAR(20) NOT NULL;


-- money_box 테이블에 created_at, updated_at 추가
ALTER TABLE money_box
    ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP;