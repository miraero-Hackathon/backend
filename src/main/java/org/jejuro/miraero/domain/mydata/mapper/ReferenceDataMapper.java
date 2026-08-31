package org.jejuro.miraero.domain.mydata.mapper;

import org.apache.ibatis.annotations.Param;

public interface ReferenceDataMapper {

  Long findFinancialInstitutionIdByCode(@Param("code") String code);

  Long findExpenseCategoryIdByName(@Param("name") String name);
}
