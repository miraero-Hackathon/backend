-- 성능 테스트용 사용자 500명 및 연관 데이터 생성
-- 대상: DEV/성능 테스트 DB 전용. 운영 DB에서는 실행하지 않는다.
--
-- 생성 데이터(사용자 1명 기준)
--   사용자 1, 마이데이터 연결 1, 계좌 2, 목표 2, 저금통 2,
--   자동이체 2, 자동저축 1, 급여 3건, 지출 180건, 자동저축 이력 30건
--
-- 로그인 비밀번호
--   @template_email 계정의 password_hash를 사용하므로 생성된 500개 계정은
--   모두 템플릿 계정과 같은 비밀번호로 로그인한다.
--
-- 재실행
--   perf001@miraero.test ~ perf500@miraero.test 중 이미 존재하는 사용자는 건너뛴다.

SET NAMES utf8mb4;
SET @template_email = 'minjoo1@naver.com';

DELIMITER $$

DROP PROCEDURE IF EXISTS seed_performance_users$$

CREATE PROCEDURE seed_performance_users()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE d INT DEFAULT 0;

    DECLARE v_email VARCHAR(30);
    DECLARE v_password_hash VARCHAR(255);
    DECLARE v_template_account_number VARBINARY(512);

    DECLARE v_institution_id BIGINT;
    DECLARE v_fixed_category_id BIGINT;
    DECLARE v_variable_category_id BIGINT;

    DECLARE v_user_id BIGINT;
    DECLARE v_checking_id BIGINT;
    DECLARE v_savings_id BIGINT;
    DECLARE v_primary_goal_id BIGINT;
    DECLARE v_secondary_goal_id BIGINT;
    DECLARE v_saving_money_box_id BIGINT;
    DECLARE v_goal_money_box_id BIGINT;
    DECLARE v_auto_saving_id BIGINT;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    IF NOT EXISTS (
        SELECT 1 FROM miraero_user WHERE email = @template_email
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '템플릿 사용자를 찾을 수 없습니다.';
    END IF;

    SELECT password_hash
    INTO v_password_hash
    FROM miraero_user
    WHERE email = @template_email;

    SELECT a.account_number
    INTO v_template_account_number
    FROM account a
        JOIN miraero_user u ON u.user_id = a.user_id
    WHERE u.email = @template_email
    ORDER BY a.account_id
    LIMIT 1;

    SELECT financial_institution_id
    INTO v_institution_id
    FROM financial_institution
    WHERE financial_institution_code = '004'
    LIMIT 1;

    SELECT expense_category_id
    INTO v_fixed_category_id
    FROM expense_category
    WHERE expense_type = 'FIXED'
    ORDER BY expense_category_id
    LIMIT 1;

    SELECT expense_category_id
    INTO v_variable_category_id
    FROM expense_category
    WHERE expense_type = 'VARIABLE'
    ORDER BY expense_category_id
    LIMIT 1;

    WHILE i <= 500 DO
        SET v_email = CONCAT('perf', LPAD(i, 3, '0'), '@miraero.test');

        IF NOT EXISTS (
            SELECT 1 FROM miraero_user WHERE email = v_email
        ) THEN
            START TRANSACTION;

            INSERT INTO miraero_user (
                name, birth_date, company_name, monthly_income,
                email, password_hash, created_at
            ) VALUES (
                CONCAT('성능사용자', LPAD(i, 3, '0')),
                DATE_ADD('1995-01-01', INTERVAL MOD(i * 37, 3000) DAY),
                CONCAT('테스트회사', MOD(i - 1, 10) + 1),
                2500000 + MOD(i - 1, 10) * 200000,
                v_email, v_password_hash, NOW()
            );
            SET v_user_id = LAST_INSERT_ID();

            INSERT INTO mydata_connection (
                user_id, financial_institution_id, connection_status,
                expires_at, synced_at, created_at, updated_at
            ) VALUES (
                v_user_id, v_institution_id, 'CONNECTED',
                DATE_ADD(NOW(), INTERVAL 1 YEAR), NOW(), NOW(), NOW()
            );

            INSERT INTO account (
                user_id, financial_institution_id, ex_account_id,
                account_type, account_name, account_number,
                account_number_hash, masked_account_number, balance,
                account_status, opened_at, maturity_at, interest_rate,
                monthly_payment_limit, created_at, updated_at, synced_at
            ) VALUES (
                v_user_id, v_institution_id, 900000000 + i * 10,
                'CHECKING', 'KB 성능테스트 입출금통장', v_template_account_number,
                SHA2(CONCAT('perf-checking-', i), 256),
                CONCAT('110-***-', LPAD(i, 4, '0')),
                5000000 + i * 10000,
                'ACTIVE', DATE_SUB(CURDATE(), INTERVAL 3 YEAR),
                NULL, NULL, NULL, NOW(), NOW(), NOW()
            );
            SET v_checking_id = LAST_INSERT_ID();

            INSERT INTO account (
                user_id, financial_institution_id, ex_account_id,
                account_type, account_name, account_number,
                account_number_hash, masked_account_number, balance,
                account_status, opened_at, maturity_at, interest_rate,
                monthly_payment_limit, created_at, updated_at, synced_at
            ) VALUES (
                v_user_id, v_institution_id, 900000001 + i * 10,
                'SAVINGS', 'KB 성능테스트 저축계좌', v_template_account_number,
                SHA2(CONCAT('perf-savings-', i), 256),
                CONCAT('220-***-', LPAD(i, 4, '0')),
                3000000 + i * 50000,
                'ACTIVE', DATE_SUB(CURDATE(), INTERVAL 1 YEAR),
                DATE_ADD(CURDATE(), INTERVAL 1 YEAR), 3.5000,
                1000000, NOW(), NOW(), NOW()
            );
            SET v_savings_id = LAST_INSERT_ID();

            -- 첫 번째 목표: k6 available-money-daily 조회 대상으로 사용한다.
            INSERT INTO goal (
                user_id, goal_name, goal_amount, start_amount,
                start_date, goal_date, goal_status, goal_type,
                is_collected, created_at
            ) VALUES (
                v_user_id, CONCAT('성능테스트 독립 목표 ', i),
                20000000, 0,
                DATE_SUB(CURDATE(), INTERVAL 6 MONTH),
                DATE_ADD(CURDATE(), INTERVAL 18 MONTH),
                'ACTIVE', 'INDEPENDENCE', FALSE, NOW()
            );
            SET v_primary_goal_id = LAST_INSERT_ID();

            INSERT INTO goal_asset (goal_id, asset_type, asset_id)
            VALUES (v_primary_goal_id, 'ACCOUNT', v_savings_id);

            INSERT INTO auto_transfer (
                withdrawal_account_id, deposit_account_id, money_box_id,
                transfer_amount, transfer_day, start_date,
                auto_transfer_status, created_at
            ) VALUES (
                v_checking_id, v_savings_id, NULL,
                500000, 25, DATE_SUB(CURDATE(), INTERVAL 6 MONTH),
                'ACTIVE', NOW()
            );

            -- 두 번째 목표: 다른 목표 자동이체 합산 쿼리도 실행되도록 구성한다.
            INSERT INTO money_box (
                user_id, account_id, balance, money_box_type,
                created_at, updated_at
            ) VALUES (
                v_user_id, v_checking_id,
                1000000 + i * 10000, 'GOAL', NOW(), NOW()
            );
            SET v_goal_money_box_id = LAST_INSERT_ID();

            INSERT INTO goal (
                user_id, goal_name, goal_amount, start_amount,
                start_date, goal_date, goal_status, goal_type,
                is_collected, created_at
            ) VALUES (
                v_user_id, CONCAT('성능테스트 비상금 목표 ', i),
                5000000, 0,
                DATE_SUB(CURDATE(), INTERVAL 3 MONTH),
                DATE_ADD(CURDATE(), INTERVAL 9 MONTH),
                'ACTIVE', 'EMERGENCY', FALSE, NOW()
            );
            SET v_secondary_goal_id = LAST_INSERT_ID();

            INSERT INTO goal_asset (goal_id, asset_type, asset_id)
            VALUES (v_secondary_goal_id, 'MONEY_BOX', v_goal_money_box_id);

            INSERT INTO auto_transfer (
                withdrawal_account_id, deposit_account_id, money_box_id,
                transfer_amount, transfer_day, start_date,
                auto_transfer_status, created_at
            ) VALUES (
                v_checking_id, NULL, v_goal_money_box_id,
                300000, 25, DATE_SUB(CURDATE(), INTERVAL 3 MONTH),
                'ACTIVE', NOW()
            );

            -- 페이스메이커 여유자금 저금통과 자동저축 설정
            INSERT INTO money_box (
                user_id, account_id, balance, money_box_type,
                created_at, updated_at
            ) VALUES (
                v_user_id, v_checking_id,
                MOD(i, 10) * 30000, 'SAVING', NOW(), NOW()
            );
            SET v_saving_money_box_id = LAST_INSERT_ID();

            INSERT INTO auto_saving (
                user_id, money_box_id, account_id,
                max_amount, auto_saving_status
            ) VALUES (
                v_user_id, v_saving_money_box_id, v_checking_id,
                300000, 'ACTIVE'
            );
            SET v_auto_saving_id = LAST_INSERT_ID();

            -- 최근 3회의 급여 입금 기록
            SET d = 0;
            WHILE d < 3 DO
                INSERT INTO `transaction` (
                    user_id, account_id, expense_category_id,
                    ex_transaction_id, transaction_type, amount,
                    balance_after, transacted_at, merchant_name
                ) VALUES (
                    v_user_id, v_checking_id, NULL,
                    i * 100000 + d,
                    'DEPOSIT', 2500000 + MOD(i - 1, 10) * 200000,
                    5000000,
                    DATE_SUB(
                        DATE_FORMAT(CURDATE(), '%Y-%m-25 09:00:00'),
                        INTERVAL (d + 1) MONTH
                    ),
                    '월급'
                );
                SET d = d + 1;
            END WHILE;

            -- 최근 90일 동안 고정지출과 변동지출을 하루 한 건씩 생성한다.
            SET d = 0;
            WHILE d < 90 DO
                INSERT INTO `transaction` (
                    user_id, account_id, expense_category_id,
                    ex_transaction_id, transaction_type, amount,
                    balance_after, transacted_at, merchant_name
                ) VALUES
                (
                    v_user_id, v_checking_id, v_fixed_category_id,
                    i * 100000 + 1000 + d,
                    'PAYMENT', 10000 + MOD(i * (d + 1), 30000),
                    4000000,
                    DATE_SUB(NOW(), INTERVAL d DAY),
                    '고정지출'
                ),
                (
                    v_user_id, v_checking_id, v_variable_category_id,
                    i * 100000 + 2000 + d,
                    'PAYMENT', 5000 + MOD(i * (d + 1), 50000),
                    4000000,
                    DATE_ADD(DATE_SUB(NOW(), INTERVAL d DAY), INTERVAL 1 HOUR),
                    '변동지출'
                );

                IF d < 30 THEN
                    INSERT INTO auto_saving_history (
                        money_box_id, auto_saving_id, amount,
                        transfer_status, transacted_at
                    ) VALUES (
                        v_saving_money_box_id, v_auto_saving_id,
                        5000 + MOD(i * (d + 1), 25000),
                        'SUCCESS', DATE_SUB(CURDATE(), INTERVAL d DAY)
                    );
                END IF;

                SET d = d + 1;
            END WHILE;

            COMMIT;
        END IF;

        SET i = i + 1;
    END WHILE;

END$$

DELIMITER ;

CALL seed_performance_users();
DROP PROCEDURE seed_performance_users;

-- 생성 결과 확인
SELECT COUNT(*) AS performance_users
FROM miraero_user
WHERE email LIKE 'perf%@miraero.test';

SELECT COUNT(*) AS performance_accounts
FROM account a
    JOIN miraero_user u ON u.user_id = a.user_id
WHERE u.email LIKE 'perf%@miraero.test';

SELECT COUNT(*) AS performance_goals
FROM goal g
    JOIN miraero_user u ON u.user_id = g.user_id
WHERE u.email LIKE 'perf%@miraero.test';

SELECT COUNT(*) AS performance_transactions
FROM `transaction` t
    JOIN miraero_user u ON u.user_id = t.user_id
WHERE u.email LIKE 'perf%@miraero.test';

-- k6 사용자 데이터 생성에 사용할 이메일/첫 번째 목표 ID
SELECT u.email,
       g.goal_id AS goalId
FROM miraero_user u
    JOIN goal g ON g.user_id = u.user_id
WHERE u.email LIKE 'perf%@miraero.test'
  AND g.goal_name LIKE '성능테스트 독립 목표 %'
ORDER BY u.email;
