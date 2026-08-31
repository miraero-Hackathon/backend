-- 적립 이력을 페이스메이커 전용에서 저금통 기준으로 넓힌다.
-- 목표 저금통 자동이체(auto_transfer)는 auto_saving에 속하지 않으므로 NULL을 허용한다.
ALTER TABLE auto_saving_history
    MODIFY COLUMN auto_saving_id BIGINT NULL COMMENT '페이스메이커 적립인 경우에만 채워진다';

-- FK가 기존 UNIQUE 인덱스를 참조하고 있어 바로 지울 수 없다. 대체 인덱스를 먼저 만든다.
ALTER TABLE auto_saving_history
    ADD INDEX idx_auto_saving_history_auto_saving (auto_saving_id);

ALTER TABLE auto_saving_history
    DROP INDEX uk_auto_saving_history_date;

-- 중복 적립 방지 기준을 저금통으로 옮긴다.
-- 페이스메이커든 목표 자동이체든 한 저금통에 하루 한 건만 쌓이도록 DB가 보장한다.
ALTER TABLE auto_saving_history
    ADD CONSTRAINT uk_auto_saving_history_money_box_date
        UNIQUE (money_box_id, transacted_at);
