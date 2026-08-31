ALTER TABLE `auto_saving_history`
    ADD COLUMN `transfer_status` VARCHAR(30) NOT NULL DEFAULT 'SUCCESS'
        COMMENT 'SUCCESS, PARTIAL_LIMIT, FAILED_INSUFFICIENT_FUNDS'
        AFTER `amount`;

ALTER TABLE `auto_saving_history`
    ADD CONSTRAINT `ck_auto_saving_history_transfer_status`
        CHECK (`transfer_status` IN (
            'SUCCESS',
            'PARTIAL_LIMIT',
            'FAILED_INSUFFICIENT_FUNDS'
        ));
