package org.jejuro.miraero.domain.product.mapper;

import org.jejuro.miraero.domain.product.domain.FinancialInstitution;

public interface FinancialInstitutionMapper {

    int upsert(FinancialInstitution financialInstitution);
}
