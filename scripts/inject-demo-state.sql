-- ---------------------------------------------------------------------------
-- 시연 계정 목표 상태 주입 (이슈 #172)
--
-- setup-demo-accounts.sh가 만든 계정은 전부 "오늘 막 시작한 0% 상태"다.
-- API로는 시작일이나 진행률을 지정할 수 없어(요청 필드 자체가 없다) 여기서
-- 직접 주입한다.
--
-- 실행:
--   docker compose exec -T mysql mysql -umiraero -pmiraero -D miraero \
--     < scripts/inject-demo-state.sql
--
-- 재실행 안전: 모든 문장이 UPDATE 또는 REPLACE라 몇 번을 돌려도 같은 상태가 된다.
--
-- 왜 start_date를 조작하는가
--   페이스는 MONTHS.between(start_date, 오늘)로 경과 개월을 구한다. now()가
--   고정이므로 여러 시점을 동시에 만들려면 시작일을 과거로 미는 수밖에 없다.
--   절대 날짜를 박으면 다음 달에 값이 어긋나므로 반드시 오늘 기준 역산으로 쓴다.
-- ---------------------------------------------------------------------------

SET @today = CURDATE();

-- ===========================================================================
-- 1. 목표 시작일 / 종료일
--    goal_date = start_date + 목표기간 (탁민주 24개월, 송승윤 6개월)
-- ===========================================================================

-- 탁민주: 전 계정 AHEAD. 월 저축액이 85~93만원으로 일정해 "꾸준히 모아온
--         사람"으로 보이도록 경과 개월을 진행률에 비례해 벌린다.
UPDATE goal g JOIN miraero_user u ON u.user_id = g.user_id
SET g.start_date = DATE_SUB(@today, INTERVAL 0 MONTH),
    g.goal_date  = LAST_DAY(DATE_ADD(DATE_SUB(@today, INTERVAL 0 MONTH), INTERVAL 24 MONTH))
WHERE u.email = 'minjoo1@naver.com';

UPDATE goal g JOIN miraero_user u ON u.user_id = g.user_id
SET g.start_date = DATE_SUB(@today, INTERVAL 6 MONTH),
    g.goal_date  = LAST_DAY(DATE_ADD(DATE_SUB(@today, INTERVAL 6 MONTH), INTERVAL 24 MONTH))
WHERE u.email = 'minjoo2@naver.com';

UPDATE goal g JOIN miraero_user u ON u.user_id = g.user_id
SET g.start_date = DATE_SUB(@today, INTERVAL 13 MONTH),
    g.goal_date  = LAST_DAY(DATE_ADD(DATE_SUB(@today, INTERVAL 13 MONTH), INTERVAL 24 MONTH))
WHERE u.email = 'minjoo3@naver.com';

UPDATE goal g JOIN miraero_user u ON u.user_id = g.user_id
SET g.start_date = DATE_SUB(@today, INTERVAL 19 MONTH),
    g.goal_date  = LAST_DAY(DATE_ADD(DATE_SUB(@today, INTERVAL 19 MONTH), INTERVAL 24 MONTH))
WHERE u.email = 'minjoo4@naver.com';

UPDATE goal g JOIN miraero_user u ON u.user_id = g.user_id
SET g.start_date = DATE_SUB(@today, INTERVAL 23 MONTH),
    g.goal_date  = LAST_DAY(DATE_ADD(DATE_SUB(@today, INTERVAL 23 MONTH), INTERVAL 24 MONTH))
WHERE u.email = 'minjoo5@naver.com';

-- 송승윤: BEHIND에서 AHEAD로 회복하는 곡선.
--         경과 2 → 3 → 4 → 5개월에 진행률 20 → 45 → 70 → 92%를 배치해
--         차이가 -67만 → -25만 → +17만 → +43만으로 움직이게 한다.
UPDATE goal g JOIN miraero_user u ON u.user_id = g.user_id
SET g.start_date = DATE_SUB(@today, INTERVAL 0 MONTH),
    g.goal_date  = LAST_DAY(DATE_ADD(DATE_SUB(@today, INTERVAL 0 MONTH), INTERVAL 6 MONTH))
WHERE u.email = 'lssyl1@naver.com';

UPDATE goal g JOIN miraero_user u ON u.user_id = g.user_id
SET g.start_date = DATE_SUB(@today, INTERVAL 2 MONTH),
    g.goal_date  = LAST_DAY(DATE_ADD(DATE_SUB(@today, INTERVAL 2 MONTH), INTERVAL 6 MONTH))
WHERE u.email = 'lssyl2@naver.com';

UPDATE goal g JOIN miraero_user u ON u.user_id = g.user_id
SET g.start_date = DATE_SUB(@today, INTERVAL 3 MONTH),
    g.goal_date  = LAST_DAY(DATE_ADD(DATE_SUB(@today, INTERVAL 3 MONTH), INTERVAL 6 MONTH))
WHERE u.email = 'lssyl3@naver.com';

UPDATE goal g JOIN miraero_user u ON u.user_id = g.user_id
SET g.start_date = DATE_SUB(@today, INTERVAL 4 MONTH),
    g.goal_date  = LAST_DAY(DATE_ADD(DATE_SUB(@today, INTERVAL 4 MONTH), INTERVAL 6 MONTH))
WHERE u.email = 'lssyl4@naver.com';

UPDATE goal g JOIN miraero_user u ON u.user_id = g.user_id
SET g.start_date = DATE_SUB(@today, INTERVAL 5 MONTH),
    g.goal_date  = LAST_DAY(DATE_ADD(DATE_SUB(@today, INTERVAL 5 MONTH), INTERVAL 6 MONTH))
WHERE u.email = 'lssyl5@naver.com';

-- ===========================================================================
-- 2. 저금통 잔액
--
-- 탁민주의 진행률은 목서버에서 동기화된 저축계좌 잔액이 결정하므로 여기서
-- 건드리지 않는다(건드려도 다음 동기화 때 덮어써진다). 페이스메이커 저금통만
-- 채워서 "모아둔 돈을 목표에 넣는" 라이브 연출 재료를 남긴다.
--
-- 송승윤은 목표 자산이 저금통이라 목표 저금통 잔액이 곧 진행률이다.
-- ===========================================================================

-- 탁민주 페이스메이커 저금통 (SAVING)
UPDATE money_box m JOIN miraero_user u ON u.user_id = m.user_id
SET m.balance = 0      WHERE u.email = 'minjoo1@naver.com' AND m.money_box_type = 'SAVING';
UPDATE money_box m JOIN miraero_user u ON u.user_id = m.user_id
SET m.balance = 180000 WHERE u.email = 'minjoo2@naver.com' AND m.money_box_type = 'SAVING';
UPDATE money_box m JOIN miraero_user u ON u.user_id = m.user_id
SET m.balance = 300000 WHERE u.email = 'minjoo3@naver.com' AND m.money_box_type = 'SAVING';
UPDATE money_box m JOIN miraero_user u ON u.user_id = m.user_id
SET m.balance = 900000 WHERE u.email = 'minjoo4@naver.com' AND m.money_box_type = 'SAVING';
-- minjoo5는 저축계좌가 98%(19,600,000)이고 페메 저금통에 400,000이 남아 있어,
-- 시연 중 목표 입금 버튼을 누르면 정확히 100%가 된다.
UPDATE money_box m JOIN miraero_user u ON u.user_id = m.user_id
SET m.balance = 400000 WHERE u.email = 'minjoo5@naver.com' AND m.money_box_type = 'SAVING';

-- 송승윤 목표 저금통 (GOAL) — 진행률 0 / 20 / 45 / 70 / 92%
UPDATE money_box m JOIN miraero_user u ON u.user_id = m.user_id
SET m.balance = 0       WHERE u.email = 'lssyl1@naver.com' AND m.money_box_type = 'GOAL';
UPDATE money_box m JOIN miraero_user u ON u.user_id = m.user_id
SET m.balance = 1000000 WHERE u.email = 'lssyl2@naver.com' AND m.money_box_type = 'GOAL';
UPDATE money_box m JOIN miraero_user u ON u.user_id = m.user_id
SET m.balance = 2250000 WHERE u.email = 'lssyl3@naver.com' AND m.money_box_type = 'GOAL';
UPDATE money_box m JOIN miraero_user u ON u.user_id = m.user_id
SET m.balance = 3500000 WHERE u.email = 'lssyl4@naver.com' AND m.money_box_type = 'GOAL';
UPDATE money_box m JOIN miraero_user u ON u.user_id = m.user_id
SET m.balance = 4600000 WHERE u.email = 'lssyl5@naver.com' AND m.money_box_type = 'GOAL';

-- 송승윤 페이스메이커 저금통 (SAVING)
UPDATE money_box m JOIN miraero_user u ON u.user_id = m.user_id
SET m.balance = 0      WHERE u.email IN ('lssyl1@naver.com', 'lssyl2@naver.com')
                         AND m.money_box_type = 'SAVING';
UPDATE money_box m JOIN miraero_user u ON u.user_id = m.user_id
SET m.balance = 200000 WHERE u.email = 'lssyl3@naver.com' AND m.money_box_type = 'SAVING';
UPDATE money_box m JOIN miraero_user u ON u.user_id = m.user_id
SET m.balance = 400000 WHERE u.email IN ('lssyl4@naver.com', 'lssyl5@naver.com')
                         AND m.money_box_type = 'SAVING';

-- ===========================================================================
-- 3. 페이스메이커 적립 이력
--
-- 대시보드의 월간 성공 횟수·연속 저축 기록을 채운다.
-- UNIQUE(money_box_id, transacted_at)이 있어 REPLACE로 넣으면 재실행해도
-- 중복되지 않는다.
--
-- 최근 N일에 하루 한 건씩 넣되, 계정별 적립일 수로 성실도를 표현한다.
--   탁민주  25~28일 / 실패 0건
--   lssyl2   1일    / 실패 3건   ← 여유자금 초과 지출로 적립이 안 되는 상태
--   lssyl3  12일    / 실패 1건
--   lssyl4  20일    / 실패 0건
--   lssyl5  26일    / 실패 0건
-- ===========================================================================

-- 날짜 생성용 숫자 테이블 (0~29)
DROP TEMPORARY TABLE IF EXISTS demo_days;
CREATE TEMPORARY TABLE demo_days (n INT PRIMARY KEY);
INSERT INTO demo_days (n) VALUES
 (0),(1),(2),(3),(4),(5),(6),(7),(8),(9),
 (10),(11),(12),(13),(14),(15),(16),(17),(18),(19),
 (20),(21),(22),(23),(24),(25),(26),(27),(28),(29);

-- 계정별 적립일 수 / 실패 건수 / 1회 적립액
DROP TEMPORARY TABLE IF EXISTS demo_history_plan;
-- miraero_user.email과 조인하므로 콜레이션을 맞춘다.
-- 임시 테이블은 서버 기본 콜레이션을 따라가 그대로 두면 조인이 실패한다.
CREATE TEMPORARY TABLE demo_history_plan (
  email       VARCHAR(50) COLLATE utf8mb4_0900_ai_ci PRIMARY KEY,
  saved_days  INT NOT NULL,
  failed_days INT NOT NULL,
  amount      BIGINT NOT NULL
);
INSERT INTO demo_history_plan VALUES
 ('minjoo2@naver.com', 25, 0,  7000),
 ('minjoo3@naver.com', 27, 0,  7200),
 ('minjoo4@naver.com', 26, 0,  5500),
 ('minjoo5@naver.com', 28, 0, 12800),
 ('lssyl2@naver.com',   1, 3,  3000),
 ('lssyl3@naver.com',  12, 1,  8000),
 ('lssyl4@naver.com',  20, 0, 21000),
 ('lssyl5@naver.com',  26, 0, 30000);

-- 성공 적립: 어제부터 saved_days일 동안 하루 한 건
REPLACE INTO auto_saving_history
  (money_box_id, auto_saving_id, amount, transfer_status, transacted_at)
SELECT m.money_box_id,
       a.auto_saving_id,
       p.amount,
       -- 상한(30,000)에 걸린 건은 PARTIAL_LIMIT으로 구분해 기록한다
       CASE WHEN p.amount >= 30000 THEN 'PARTIAL_LIMIT' ELSE 'SUCCESS' END,
       DATE_SUB(@today, INTERVAL (d.n + 1) DAY)
FROM demo_history_plan p
JOIN miraero_user u  ON u.email = p.email
JOIN money_box m     ON m.user_id = u.user_id AND m.money_box_type = 'SAVING'
JOIN auto_saving a   ON a.user_id = u.user_id
JOIN demo_days d     ON d.n < p.saved_days;

-- 잔액 부족 실패: 성공 구간 뒤쪽에 배치해 성공 이력과 날짜가 겹치지 않게 한다
REPLACE INTO auto_saving_history
  (money_box_id, auto_saving_id, amount, transfer_status, transacted_at)
SELECT m.money_box_id,
       a.auto_saving_id,
       0,
       'FAILED_INSUFFICIENT_FUNDS',
       DATE_SUB(@today, INTERVAL (p.saved_days + d.n + 1) DAY)
FROM demo_history_plan p
JOIN miraero_user u  ON u.email = p.email
JOIN money_box m     ON m.user_id = u.user_id AND m.money_box_type = 'SAVING'
JOIN auto_saving a   ON a.user_id = u.user_id
JOIN demo_days d     ON d.n < p.failed_days;

DROP TEMPORARY TABLE demo_days;
DROP TEMPORARY TABLE demo_history_plan;
