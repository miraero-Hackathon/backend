CREATE TABLE financial_product_link (
    financial_product_link_id BIGINT NOT NULL AUTO_INCREMENT,
    financial_institution_id BIGINT NOT NULL,
    product_type VARCHAR(20) NOT NULL COMMENT 'DEPOSIT, SAVING',
    product_code VARCHAR(50) NOT NULL,
    product_page_url VARCHAR(2048) NOT NULL,
    link_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
        COMMENT 'ACTIVE, BROKEN',
    verified_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

                                        PRIMARY KEY (financial_product_link_id),

                                        CONSTRAINT uk_financial_product_link
                                            UNIQUE (
                                                    financial_institution_id,
                                                    product_type,
                                                    product_code
                                                ),

                                        CONSTRAINT fk_financial_product_link_financial_institution
                                            FOREIGN KEY (financial_institution_id)
                                                REFERENCES financial_institution(financial_institution_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;