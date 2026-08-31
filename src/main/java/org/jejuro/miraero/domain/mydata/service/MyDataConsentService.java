package org.jejuro.miraero.domain.mydata.service;

import org.jejuro.miraero.domain.mydata.dto.MyDataConnectionListResponse;

public interface MyDataConsentService {

  void createInitialConsent(Long userId);

  MyDataConnectionListResponse getConnections(Long userId);

}
