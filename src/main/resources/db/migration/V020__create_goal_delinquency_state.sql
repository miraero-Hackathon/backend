CREATE TABLE `goal_delinquency_state` (
      `goal_delinquency_state_id` BIGINT NOT NULL AUTO_INCREMENT,
      `goal_id`                   BIGINT NOT NULL COMMENT '목표 ID',

    -- 누적 기준으로 "부족한 상태"가 된 날짜. 정상이면 NULL.
      `behind_since`              DATE NULL COMMENT '부족 상태 시작일',
    -- 이번 부족 기간 동안 단기/장기연체 이벤트를 이미 발생시켰는지. 중복 차감 방지용.
      `short_term_fired`          BOOLEAN NOT NULL DEFAULT FALSE,
      `long_term_fired`           BOOLEAN NOT NULL DEFAULT FALSE,

    -- 반대로 "정상(부족 아님)" 상태가 된 날짜. 연속상환 가점 판정에 씀.
      `on_track_since`            DATE NULL COMMENT '정상 상태 시작일',
      `consecutive_fired`         BOOLEAN NOT NULL DEFAULT FALSE,

      CONSTRAINT `pk_goal_delinquency_state`
          PRIMARY KEY (`goal_delinquency_state_id`),

    -- 목표 하나당 상태는 하나뿐이어야 하므로 UNIQUE. 이 제약 덕분에 upsert(있으면 갱신, 없으면 생성)가 가능해짐.
      CONSTRAINT `uk_goal_delinquency_state_goal`
          UNIQUE (`goal_id`),

      CONSTRAINT `fk_goal_delinquency_state_goal`
          FOREIGN KEY (`goal_id`)
              REFERENCES `goal` (`goal_id`)
              ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;