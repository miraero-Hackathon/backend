package org.jejuro.miraero.domain.moneybox.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MoneyBoxErrorCode implements ErrorCode {

  MONEY_BOX_NOT_FOUND(
      HttpStatus.NOT_FOUND,
      "MONEY_BOX_001",
      "저금통을 찾을 수 없습니다."
  ),

  INVALID_MONEY_BOX_ACCOUNT(
      HttpStatus.BAD_REQUEST,
      "MONEY_BOX_002",
      "저금통은 입출금통장에만 만들 수 있습니다."
  ),

  AUTO_TRANSFER_NOT_SALARY_ACCOUNT(
      HttpStatus.BAD_REQUEST,
      "MONEY_BOX_003",
      "자동이체는 급여가 입금되는 통장의 저금통에만 설정할 수 있습니다."
  ),

  SAVING_TYPE_NOT_ALLOWED(
      HttpStatus.BAD_REQUEST,
      "MONEY_BOX_004",
      "페이스메이커 저금통은 페이스메이커 개설 API로 만들어야 합니다."
  );

  private final HttpStatus status;
  private final String code;
  private final String message;
}
