package org.jejuro.miraero.global.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jejuro.miraero.global.exception.ErrorCode;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

  private boolean success;
  private T data;
  private ApiError error;

  /**
   * 성공 응답
   */
  public static <T> ApiResponse<T> success(T data) {
    return ApiResponse.<T>builder().success(true).data(data).error(null).build();
  }

  /**
   * 실패 응답
   */
  public static <T> ApiResponse<T> error(String code, String message) {
    return ApiResponse.<T>builder().success(false).data(null).error(ApiError.of(code, message))
        .build();
  }

  /**
   * ErrorCode를 이용한 실패 응답
   */
  public static <T> ApiResponse<T> error(ErrorCode errorCode) {
    return ApiResponse.<T>builder().success(false).data(null)
        .error(ApiError.of(errorCode.getCode(), errorCode.getMessage())).build();
  }

  @Getter
  @AllArgsConstructor
  public static class ApiError {

    private String code;
    private String message;

    public static ApiError of(String code, String message) {
      return new ApiError(code, message);
    }
  }
}
