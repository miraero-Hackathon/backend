-- 국민은행 금융기관 데이터 추가
INSERT IGNORE INTO `financial_institution` (`financial_institution_code`, `financial_institution_name`)
VALUES ('004', '국민은행');

-- 지출 유형(FIXED/VARIABLE) 제약 조건 추가
ALTER TABLE `expense_category`
    ADD CONSTRAINT `ck_expense_category_expense_type`
        CHECK (`expense_type` IN ('FIXED', 'VARIABLE'));

-- 기본 지출 카테고리 일괄 등록
INSERT IGNORE INTO `expense_category` (`category_name`, `expense_type`)
VALUES ('주거',     'FIXED'),
       ('통신',     'FIXED'),
       ('보험',     'FIXED'),
       ('구독',     'FIXED'),
       ('대출상환', 'FIXED'),
       ('저축',     'FIXED'),
       ('투자',     'FIXED'),
       ('식비',     'VARIABLE'),
       ('카페',     'VARIABLE'),
       ('교통',     'VARIABLE'),
       ('쇼핑',     'VARIABLE'),
       ('문화',     'VARIABLE'),
       ('의료',     'VARIABLE'),
       ('기타',     'VARIABLE');
