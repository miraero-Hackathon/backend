package org.jejuro.miraero.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {

  INVALID_REQUEST(
      HttpStatus.BAD_REQUEST,
      "COMMON_001",
      "잘못된 요청입니다."
  ),

  INVALID_INPUT_VALUE(
      HttpStatus.BAD_REQUEST,
      "COMMON_002",
      "입력값이 올바르지 않습니다."
  ),

  METHOD_NOT_ALLOWED(
      HttpStatus.METHOD_NOT_ALLOWED,
      "COMMON_003",
      "지원하지 않는 HTTP 메서드입니다."
  ),

  RESOURCE_NOT_FOUND(
      HttpStatus.NOT_FOUND,
      "COMMON_004",
      "요청한 리소스를 찾을 수 없습니다."
  ),

  ACCESS_DENIED(
      HttpStatus.FORBIDDEN,
      "COMMON_005",
      "접근 권한이 없습니다."
  ),

  INTERNAL_SERVER_ERROR(
      HttpStatus.INTERNAL_SERVER_ERROR,
      "COMMON_006",
      "서버 내부 오류가 발생했습니다."
  ),

  SERVICE_UNAVAILABLE(
      HttpStatus.SERVICE_UNAVAILABLE,
      "COMMON_007",
      "서비스를 일시적으로 사용할 수 없습니다."
  );

  private final HttpStatus status;
  private final String code;
  private final String message;
}
