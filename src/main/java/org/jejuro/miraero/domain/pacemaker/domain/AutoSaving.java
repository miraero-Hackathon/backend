package org.jejuro.miraero.domain.pacemaker.domain;

import lombok.Getter;

@Getter
public class AutoSaving {

  private Long autoSavingId;
  private Long userId;
  private Long moneyBoxId;
  private Long accountId;
  private Long maxAmount;
  private String autoSavingStatus;
}
