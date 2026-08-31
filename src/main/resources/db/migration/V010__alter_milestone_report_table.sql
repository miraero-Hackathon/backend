ALTER TABLE `milestone_report`
    MODIFY COLUMN `title`
        VARCHAR(100) NULL
        COMMENT 'AI 리포트 제목',
    MODIFY COLUMN `content`
        TEXT NULL
        COMMENT 'AI가 생성한 리포트 본문';