-- FK 제거
ALTER TABLE goal_asset
    DROP FOREIGN KEY fk_goal_asset_goal;

-- 기존 UNIQUE 제거
ALTER TABLE goal_asset
    DROP INDEX uk_goal_asset;

-- FK가 사용할 일반 인덱스 생성
ALTER TABLE goal_asset
    ADD INDEX idx_goal_asset_goal(goal_id);

-- 새로운 UNIQUE 생성
ALTER TABLE goal_asset
    ADD CONSTRAINT uk_goal_asset
        UNIQUE(asset_type, asset_id);

-- FK 다시 생성
ALTER TABLE goal_asset
    ADD CONSTRAINT fk_goal_asset_goal
        FOREIGN KEY (goal_id)
            REFERENCES goal(goal_id)
            ON DELETE CASCADE;


-- money_box의 balance DEFAULT 0 추가
ALTER TABLE money_box
    MODIFY COLUMN balance BIGINT NOT NULL DEFAULT 0;

-- money_box의 계좌번호해시값에 UNIQUE 제약조건 추가
ALTER TABLE money_box
    ADD CONSTRAINT uk_money_box_account_number_hash
        UNIQUE (account_number_hash);
