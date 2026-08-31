ALTER TABLE `mydata_connection`
    ADD COLUMN `synced_at` DATETIME NULL
        COMMENT '마지막 데이터 동기화 일시'
        AFTER `expires_at`;

ALTER TABLE `mydata_connection`
    DROP COLUMN `agreed_at`;
