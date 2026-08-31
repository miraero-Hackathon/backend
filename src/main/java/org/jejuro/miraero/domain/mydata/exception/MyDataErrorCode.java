package org.jejuro.miraero.domain.mydata.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MyDataErrorCode implements ErrorCode {

  MYDATA_LINK_FAILED(
      HttpStatus.BAD_GATEWAY,
      "MYDATA_001",
      "금융기관 연동에 실패했습니다."
  ),

  MYDATA_SYNC_FAILED(
      HttpStatus.BAD_GATEWAY,
      "MYDATA_002",
      "금융 데이터 동기화에 실패했습니다."
  ),

  MYDATA_NOT_CONNECTED(
      HttpStatus.BAD_REQUEST,
      "MYDATA_003",
      "금융기관이 연동되어 있지 않습니다."
  ),

  MYDATA_TOKEN_EXPIRED(
      HttpStatus.UNAUTHORIZED,
      "MYDATA_004",
      "연동이 만료되었습니다. 다시 연동해 주세요."
  ),

  MYDATA_INSTITUTION_NOT_FOUND(
      HttpStatus.INTERNAL_SERVER_ERROR,
      "MYDATA_005",
      "등록되지 않은 금융기관 코드입니다."
  ),

  MYDATA_TRANSFER_FAILED(
      HttpStatus.BAD_GATEWAY,
      "MYDATA_006",
      "계좌 이체에 실패했습니다."
  );

  private final HttpStatus status;
  private final String code;
  private final String message;
}
