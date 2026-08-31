INSERT INTO financial_institution (
    financial_institution_code,
    financial_institution_name
) VALUES (
    '0010927',
    '국민은행'
) AS new
ON DUPLICATE KEY UPDATE
    financial_institution_name = new.financial_institution_name;

INSERT INTO deposit_product (
    financial_institution_id, product_code, product_name, join_method, join_target,
    join_restriction, special_condition, maturity_interest, max_limit, notice,
    disclosure_month, disclosure_start_date, disclosure_end_date, submitted_at
)
SELECT
    fi.financial_institution_id, product_data.product_code, product_data.product_name,
    product_data.join_method, product_data.join_target, product_data.join_restriction,
    product_data.special_condition, product_data.maturity_interest, product_data.max_limit,
    product_data.notice, product_data.disclosure_month, product_data.disclosure_start_date,
    product_data.disclosure_end_date, NULL
FROM financial_institution fi
CROSS JOIN (
    SELECT 'DP01000029' AS product_code, '국민수퍼정기예금' AS product_name,
           '영업점, 인터넷, 스마트폰' AS join_method, '제한 없음' AS join_target, '1' AS join_restriction,
           '금리우대쿠폰 적용 시 쿠폰 우대금리를 기본이율에 가산. 비과세가계저축 또는 중장기주택부금 만기계좌를 해지일로부터 2개월 이내에 본인, 배우자 또는 직계존비속 명의로 계약기간 1년 이상 가입하면 연 0.1%p 우대.' AS special_condition,
           '고정금리형: 만기 후 1개월 이내 약정이율의 50%, 만기 후 1개월 초과 3개월 이내 약정이율의 30%, 만기 후 3개월 초과 연 0.1%. 단위기간 금리연동형 및 CD금리연동형: 만기 후 3개월 이내 연 0.2%, 3개월 초과 연 0.1%.' AS maturity_interest,
           NULL AS max_limit,
           '가입금액 100만원 이상. 신규계좌 포함 최대 30회까지 추가입금 가능하며 건별 추가입금액은 10만원 이상. 고정금리형은 1개월 이상 36개월 이하, 단위기간 금리연동형은 12개월 이상 36개월 이하, CD금리연동형은 6개월·1년·2년·3년 가입 가능. 고정금리형은 조건 충족 시 분할해지 가능. 재예치 불가. 비과세종합저축 가입 가능. 예금자보호 대상.' AS notice,
           '202607' AS disclosure_month, NULL AS disclosure_start_date, NULL AS disclosure_end_date
    UNION ALL
    SELECT 'DP01001667', 'KB골든라이프연금예금', '영업점, 스마트폰, 고객센터', '실명의 개인', '1',
           '계약기간의 절반 이상 KB국민은행 계좌로 연금 수령 시 연 0.20%p 우대, 금리우대쿠폰 적용 가능',
           '만기 후 1개월 이내 약정금리의 50%, 1개월 초과 3개월 이내 30%, 3개월 초과 연 0.10%',
           NULL, '가입금액 100만원 이상, 가입기간 1개월~12개월, 추가입금·재예치·일부해지 불가, 판매한도 5천억원',
           '202607', NULL, NULL
    UNION ALL
    SELECT 'DP01001670', '공동구매정기예금', '영업점, 인터넷, 스마트폰, 고객센터', '실명의 개인', '1',
           '판매금액 1천억원 초과 시 우대금리 적용, KB청년도약계좌 해지 고객은 연 0.40%p 이벤트 금리 적용',
           '만기 후 1개월 이내 적용금리의 50%, 1개월 초과 3개월 이내 30%, 3개월 초과 연 0.10%',
           20000000, '가입금액 100만원 이상 2천만원 이하, 가입기간 6개월·12개월, 추가입금·재예치·일부해지 불가, 판매한도 소진 시 조기 종료',
           '202607', '2026-07-27', '2026-08-07'
    UNION ALL
    SELECT 'DP01000014', '일반정기예금', '영업점, 인터넷', '제한 없음', '1',
           '우대조건 없음',
           '만기 후 1개월 이내 만기이율의 50%, 1개월 초과 3개월 이내 30%, 3개월 초과 연 0.10%',
           NULL, '가입금액 10만원 이상, 가입기간 1개월~60개월, 추가입금·분할해지·재예치 불가, 만기 자동해지 가능',
           '202607', NULL, NULL
) product_data
WHERE fi.financial_institution_code = '0010927'
ON DUPLICATE KEY UPDATE
    product_name = VALUES(product_name),
    join_method = VALUES(join_method),
    join_target = VALUES(join_target),
    join_restriction = VALUES(join_restriction),
    special_condition = VALUES(special_condition),
    maturity_interest = VALUES(maturity_interest),
    max_limit = VALUES(max_limit),
    notice = VALUES(notice),
    disclosure_month = VALUES(disclosure_month),
    disclosure_start_date = VALUES(disclosure_start_date),
    disclosure_end_date = VALUES(disclosure_end_date);

INSERT INTO saving_product (
    financial_institution_id, product_code, product_name, join_method, join_target,
    join_restriction, special_condition, maturity_interest, max_limit, notice,
    disclosure_month, disclosure_start_date, disclosure_end_date, submitted_at
)
SELECT
    fi.financial_institution_id, product_data.product_code, product_data.product_name,
    product_data.join_method, product_data.join_target, product_data.join_restriction,
    product_data.special_condition, product_data.maturity_interest, product_data.max_limit,
    product_data.notice, product_data.disclosure_month, product_data.disclosure_start_date,
    product_data.disclosure_end_date, NULL
FROM financial_institution fi
CROSS JOIN (
    SELECT 'DP01001656' AS product_code, 'KB청년미래적금' AS product_name,
           '영업점, 스마트폰, 고객센터' AS join_method,
           '만 19세~34세이며 개인소득·가구소득 등 가입요건을 충족한 실명의 개인' AS join_target,
           '3' AS join_restriction,
           '급여이체 연 1.0%p, 자동이체·카드결제 등 출금실적 연 0.8%p, 거래감사 연 0.5%p, 소득플러스 연 0.5%p, 청년재무상담 이수 연 0.2%p' AS special_condition,
           '만기 후 1개월 이내 기본금리의 50%, 1개월 초과 3개월 이내 30%, 3개월 초과 연 0.10%' AS maturity_interest,
           500000 AS max_limit,
           '가입기간 3년, 월 50만원·연 600만원 한도, 자유적립식, 전 금융기관 1인 1계좌, 중도해지 시 비과세·정부기여금 미적용, 재예치·일부인출 불가' AS notice,
           '202606' AS disclosure_month, NULL AS disclosure_start_date, NULL AS disclosure_end_date
    UNION ALL
    SELECT 'DP01000429-F', 'KB국민프리미엄적금(자유)', '영업점, 인터넷, 스마트폰, 고객센터', '실명의 개인', '1',
           '① 단체가입/나라사랑/쿠폰 우대이율: 1년 연 0.6%p, 2년 연 0.7%p, 3년 연 0.9%p, 5년 연 1.0%p (중복 적용 불가) ② 교차거래 우대이율: 연 0.3%p',
           '1개월 이내 기본이율의 50%, 1개월 초과 3개월 이내 기본이율의 30%, 3개월 초과 연 0.1%',
           3000000, '1인 1계좌', '202607', '2026-07-24', NULL
    UNION ALL
    SELECT 'DP01000038', 'KB상호부금', '영업점, 인터넷, 스마트폰, 고객센터', '제한 없음', '1',
           '자동이체 우대 연 0.1%p, 직장인우대종합통장 및 명품여성종합통장 보유 고객 우대 연 0.3%p',
           '만기 후 1개월 이내 기본이율의 50%, 1개월 초과 3개월 이내 기본이율의 30%, 3개월 초과 연 0.10%',
           5000000, '가입기간 6개월~72개월, 월 1만원 이상 500만원 이하, 정액적립식·자유적립식 선택 가능, 재예치·일부인출 불가',
           '202607', '2026-07-24', NULL
) product_data
WHERE fi.financial_institution_code = '0010927'
ON DUPLICATE KEY UPDATE
    product_name = VALUES(product_name),
    join_method = VALUES(join_method),
    join_target = VALUES(join_target),
    join_restriction = VALUES(join_restriction),
    special_condition = VALUES(special_condition),
    maturity_interest = VALUES(maturity_interest),
    max_limit = VALUES(max_limit),
    notice = VALUES(notice),
    disclosure_month = VALUES(disclosure_month),
    disclosure_start_date = VALUES(disclosure_start_date),
    disclosure_end_date = VALUES(disclosure_end_date);

INSERT INTO deposit_option (
    deposit_product_id, interest_rate_type, save_term, base_interest_rate, max_interest_rate
)
SELECT dp.deposit_product_id, option_data.interest_rate_type, option_data.save_term,
       option_data.base_interest_rate, option_data.max_interest_rate
FROM deposit_product dp
JOIN financial_institution fi ON fi.financial_institution_id = dp.financial_institution_id
JOIN (
    SELECT 'DP01000029' AS product_code, 'S' AS interest_rate_type, 1 AS save_term, 1.90 AS base_interest_rate, 2.00 AS max_interest_rate
    UNION ALL SELECT 'DP01000029', 'S', 3, 2.25, 2.35
    UNION ALL SELECT 'DP01000029', 'S', 6, 2.25, 2.35
    UNION ALL SELECT 'DP01000029', 'S', 12, 2.30, 2.40
    UNION ALL SELECT 'DP01000029', 'S', 24, 2.35, 2.45
    UNION ALL SELECT 'DP01000029', 'S', 36, 2.45, 2.55
    UNION ALL SELECT 'DP01000029', 'M', 12, 2.20, 2.30
    UNION ALL SELECT 'DP01000029', 'M', 24, 2.25, 2.35
    UNION ALL SELECT 'DP01000029', 'M', 36, 2.35, 2.45
    UNION ALL SELECT 'DP01001667', 'S', 1, 2.75, 2.95
    UNION ALL SELECT 'DP01001667', 'S', 3, 3.05, 3.25
    UNION ALL SELECT 'DP01001667', 'S', 6, 3.15, 3.35
    UNION ALL SELECT 'DP01001667', 'S', 12, 3.20, 3.40
    UNION ALL SELECT 'DP01001670', 'S', 6, 2.70, 3.20
    UNION ALL SELECT 'DP01001670', 'S', 12, 2.80, 3.30
    UNION ALL SELECT 'DP01000014', 'S', 1, 1.65, 1.65
    UNION ALL SELECT 'DP01000014', 'S', 3, 2.00, 2.00
    UNION ALL SELECT 'DP01000014', 'S', 6, 2.25, 2.25
    UNION ALL SELECT 'DP01000014', 'S', 12, 2.50, 2.50
    UNION ALL SELECT 'DP01000014', 'S', 24, 2.35, 2.35
    UNION ALL SELECT 'DP01000014', 'S', 36, 2.45, 2.45
    UNION ALL SELECT 'DP01000014', 'S', 60, 2.45, 2.45
) option_data ON option_data.product_code = dp.product_code
WHERE fi.financial_institution_code = '0010927'
ON DUPLICATE KEY UPDATE
    base_interest_rate = VALUES(base_interest_rate),
    max_interest_rate = VALUES(max_interest_rate);

INSERT INTO saving_option (
    saving_product_id, interest_rate_type, reserve_type, save_term, base_interest_rate, max_interest_rate
)
SELECT sp.saving_product_id, option_data.interest_rate_type, option_data.reserve_type,
       option_data.save_term, option_data.base_interest_rate, option_data.max_interest_rate
FROM saving_product sp
JOIN financial_institution fi ON fi.financial_institution_id = sp.financial_institution_id
JOIN (
    SELECT 'DP01001656' AS product_code, 'S' AS interest_rate_type, 'F' AS reserve_type, 36 AS save_term, 5.00 AS base_interest_rate, 8.00 AS max_interest_rate
    UNION ALL SELECT 'DP01000429-F', 'S', 'F', 12, 2.55, 3.45
    UNION ALL SELECT 'DP01000429-F', 'S', 'F', 24, 2.65, 3.65
    UNION ALL SELECT 'DP01000429-F', 'S', 'F', 36, 2.95, 4.15
    UNION ALL SELECT 'DP01000429-F', 'S', 'F', 60, 3.15, 4.45
    UNION ALL SELECT 'DP01000038', 'S', 'S', 6, 2.35, 2.75
    UNION ALL SELECT 'DP01000038', 'S', 'S', 12, 2.55, 2.95
    UNION ALL SELECT 'DP01000038', 'S', 'S', 24, 2.95, 3.35
    UNION ALL SELECT 'DP01000038', 'S', 'S', 36, 3.25, 3.65
    UNION ALL SELECT 'DP01000038', 'S', 'F', 12, 2.45, 2.85
    UNION ALL SELECT 'DP01000038', 'S', 'F', 24, 2.90, 3.30
    UNION ALL SELECT 'DP01000038', 'S', 'F', 36, 3.15, 3.55
    UNION ALL SELECT 'DP01000038', 'S', 'F', 72, 3.15, 3.55
) option_data ON option_data.product_code = sp.product_code
WHERE fi.financial_institution_code = '0010927'
ON DUPLICATE KEY UPDATE
    base_interest_rate = VALUES(base_interest_rate),
    max_interest_rate = VALUES(max_interest_rate);

INSERT INTO financial_product_link (
    financial_institution_id, product_type, product_code, product_page_url, link_status, verified_at
)
SELECT fi.financial_institution_id, link_data.product_type, link_data.product_code,
       link_data.product_page_url, 'ACTIVE', NOW()
FROM financial_institution fi
CROSS JOIN (
    SELECT 'DEPOSIT' AS product_type, '010300100335' AS product_code, 'https://obank.kbstar.com/quics?page=C016613&cc=b061496:b061645&isNew=N&prcode=DP01000938' AS product_page_url
    UNION ALL SELECT 'SAVING', '010200100070', 'https://obank.kbstar.com/quics?page=C016613&cc=b061496:b061645&isNew=N&prcode=DP01000821'
    UNION ALL SELECT 'SAVING', '010200100084', 'https://obank.kbstar.com/quics?page=C016613&cc=b061496:b061645&isNew=N&prcode=DP01000942'
    UNION ALL SELECT 'SAVING', '010200100104', 'https://obank.kbstar.com/quics?page=C016613&cc=b061496:b061645&isNew=N&prcode=DP01001566'
    UNION ALL SELECT 'SAVING', '010200100051', 'https://obank.kbstar.com/quics?page=C016613&cc=b061496:b061645&isNew=N&prcode=DP01000428'
    UNION ALL SELECT 'DEPOSIT', 'DP01000029', 'https://obank.kbstar.com/quics?page=C016613&cc=b061496:b061645&isNew=N&prcode=DP01000029'
    UNION ALL SELECT 'DEPOSIT', 'DP01001667', 'https://obank.kbstar.com/quics?page=C016613&cc=b061496:b061645&isNew=Y&prcode=DP01001667'
    UNION ALL SELECT 'DEPOSIT', 'DP01001670', 'https://obank.kbstar.com/quics?page=C016613&cc=b061496:b061645&isNew=Y&prcode=DP01001670'
    UNION ALL SELECT 'DEPOSIT', 'DP01000014', 'https://obank.kbstar.com/quics?page=C016613&cc=b061496:b061645&isNew=N&prcode=DP01000014'
    UNION ALL SELECT 'SAVING', 'DP01001656', 'https://obank.kbstar.com/quics?page=C016613&cc=b061496:b061645&isNew=Y&prcode=DP01001656'
    UNION ALL SELECT 'SAVING', 'DP01000429-F', 'https://obank.kbstar.com/quics?page=C016613&cc=b061496:b061645&isNew=N&prcode=DP01000429'
    UNION ALL SELECT 'SAVING', 'DP01000038', 'https://obank.kbstar.com/quics?page=C016613&cc=b061496:b061645&isNew=N&prcode=DP01000038'
) link_data
WHERE fi.financial_institution_code = '0010927'
ON DUPLICATE KEY UPDATE
    product_page_url = VALUES(product_page_url),
    link_status = VALUES(link_status),
    verified_at = VALUES(verified_at);
