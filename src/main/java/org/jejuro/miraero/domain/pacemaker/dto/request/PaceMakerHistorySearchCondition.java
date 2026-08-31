package org.jejuro.miraero.domain.pacemaker.dto.request;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;

import lombok.Getter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Setter;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;

@Getter
@Setter
@ApiModel(description = "페이스메이커 이력 조회 조건")
public class PaceMakerHistorySearchCondition {

  @ApiModelProperty(value = "페이지 번호. 0부터 시작", example = "0")
  private Integer page = 0;
  @ApiModelProperty(value = "페이지당 항목 수", example = "10")
  private Integer size = 10;

  @ApiModelProperty(value = "조회할 월(yyyy-MM). 생략하면 이번 달", example = "2026-06")
  private String yearMonth;

  public void validate() {
    if (page == null) {
      page = 0;
    }

    if (size == null) {
      size = 10;
    }

    if (page < 0 || size <= 0) {
      throw new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE);
    }
  }

  public long getOffset() {
    return (long) page * size;
  }

  /**
   * 시연에서 지난 날짜를 재생하면 이번 달 밖의 이력을 봐야 한다.
   */
  public YearMonth resolveYearMonth() {
    if (yearMonth == null || yearMonth.isBlank()) {
      return YearMonth.now();
    }

    try {
      return YearMonth.parse(yearMonth);
    } catch (DateTimeParseException e) {
      throw new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE);
    }
  }
}
