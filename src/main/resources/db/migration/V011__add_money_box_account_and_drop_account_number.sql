-- 저금통을 입출금통장 안의 구획(서브 레저)으로 전환한다.
-- 저금통 잔액은 소속 계좌 안에 있는 돈이며, 계좌를 떠나지 않는다.
ALTER TABLE money_box
    ADD COLUMN account_id BIGINT NULL COMMENT '소속 계좌 ID' AFTER user_id;

-- 기존 저금통은 자동이체/페이스메이커 출금계좌가 곧 소속 계좌이므로 그 값으로 채운다
UPDATE money_box mb
    JOIN auto_transfer at ON at.money_box_id = mb.money_box_id
SET mb.account_id = at.withdrawal_account_id
WHERE mb.account_id IS NULL;

UPDATE money_box mb
    JOIN auto_saving aus ON aus.money_box_id = mb.money_box_id
SET mb.account_id = aus.account_id
WHERE mb.account_id IS NULL;

ALTER TABLE money_box
    ADD CONSTRAINT fk_money_box_account
        FOREIGN KEY (account_id)
            REFERENCES account (account_id);

-- 저금통은 자체 계좌번호를 갖지 않는다. 화면에는 소속 통장의 마스킹 번호를 쓴다.
ALTER TABLE money_box
    DROP INDEX uk_money_box_account_number_hash;

ALTER TABLE money_box
    DROP COLUMN account_number,
    DROP COLUMN account_number_hash,
    DROP COLUMN masked_account_number;
