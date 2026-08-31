package org.jejuro.miraero.domain.mydata.mapper;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.jejuro.miraero.global.config.RootConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig
@ContextConfiguration(classes = RootConfig.class)
class ReferenceDataMapperTest {

  @Autowired
  private ReferenceDataMapper referenceDataMapper;

  @Test
  @DisplayName("금융기관 코드로 ID를 조회한다")
  void findFinancialInstitutionIdByCode() {
    assertNotNull(referenceDataMapper.findFinancialInstitutionIdByCode("004"));
  }

  @Test
  @DisplayName("등록되지 않은 금융기관 코드는 null을 반환한다")
  void findFinancialInstitutionIdByCode_notFound() {
    assertNull(referenceDataMapper.findFinancialInstitutionIdByCode("999"));
  }

  @Test
  @DisplayName("지출 카테고리명으로 ID를 조회한다")
  void findExpenseCategoryIdByName() {
    assertNotNull(referenceDataMapper.findExpenseCategoryIdByName("카페"));
  }

  @Test
  @DisplayName("등록되지 않은 카테고리명은 null을 반환한다")
  void findExpenseCategoryIdByName_notFound() {
    assertNull(referenceDataMapper.findExpenseCategoryIdByName("존재하지않는카테고리"));
  }
}
