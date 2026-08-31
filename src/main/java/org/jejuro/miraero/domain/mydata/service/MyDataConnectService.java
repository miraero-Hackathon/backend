package org.jejuro.miraero.domain.mydata.service;

import org.jejuro.miraero.domain.mydata.dto.response.MyDataConnectResponse;

public interface MyDataConnectService {

  MyDataConnectResponse connect(Long userId);

  void sync(Long userId);
}
