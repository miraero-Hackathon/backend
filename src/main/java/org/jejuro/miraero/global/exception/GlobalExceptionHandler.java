package org.jejuro.miraero.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.jejuro.miraero.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * 비즈니스 예외 처리
   */
  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ApiResponse<Void>> handleBusinessException(
      BusinessException exception
  ) {
    ErrorCode errorCode = exception.getErrorCode();

    return ResponseEntity
        .status(errorCode.getStatus())
        .body(ApiResponse.error(
            errorCode.getCode(),
            errorCode.getMessage()
        ));
  }

  /*
  형식이 옳지 않은 경우 예외 처리 (@Valid 검증 실패)
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException exception
  ) {
    ErrorCode errorCode = CommonErrorCode.INVALID_INPUT_VALUE;

    return ResponseEntity
        .status(errorCode.getStatus())
        .body(ApiResponse.error(errorCode));
  }


  /*
    PathVariable/RequestParam 타입 변환 실패 (예: accountId=abc)
   */
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatchException(
      MethodArgumentTypeMismatchException exception
  ) {
    ErrorCode errorCode = CommonErrorCode.INVALID_INPUT_VALUE;

    return ResponseEntity
        .status(errorCode.getStatus())
        .body(ApiResponse.error(errorCode));
  }

  /**
   * 처리되지 않은 서버 예외 처리
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleException(
      Exception exception
  ) {
    ErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;

    log.error("Unhandled exception occurred", exception);

    return ResponseEntity
        .status(errorCode.getStatus())
        .body(ApiResponse.error(
            errorCode.getCode(),
            errorCode.getMessage()
        ));
  }

  /*
    쿠키 누락
   */
  @ExceptionHandler(MissingRequestCookieException.class)
  public ResponseEntity<ApiResponse<Void>> handleMissingRequestCookieException(
      MissingRequestCookieException exception
  ) {
    ErrorCode errorCode = CommonErrorCode.INVALID_INPUT_VALUE;

    return ResponseEntity
        .status(errorCode.getStatus())
        .body(ApiResponse.error(errorCode));
  }
}
