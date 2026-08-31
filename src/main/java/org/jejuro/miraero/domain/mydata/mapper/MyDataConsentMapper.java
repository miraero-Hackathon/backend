package org.jejuro.miraero.domain.mydata.mapper;

import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.jejuro.miraero.domain.mydata.domain.MyDataConsent;
import org.jejuro.miraero.domain.mydata.dto.MyDataConnectionResponse;

public interface MyDataConsentMapper {

  int save(MyDataConsent consent);

  List<MyDataConnectionResponse> findConnectionsByUserId(@Param("userId") Long userId);

  // (user_id, financial_institution_id) UNIQUE 기준 upsert. 재연동(토큰 갱신) 시 synced_at은 건드리지 않는다
  int upsertConnection(
      @Param("userId") Long userId,
      @Param("financialInstitutionId") Long financialInstitutionId,
      @Param("connectionStatus") String connectionStatus,
      @Param("expiresAt") LocalDateTime expiresAt
  );

  int updateSyncedAt(
      @Param("userId") Long userId,
      @Param("financialInstitutionId") Long financialInstitutionId
  );
}
