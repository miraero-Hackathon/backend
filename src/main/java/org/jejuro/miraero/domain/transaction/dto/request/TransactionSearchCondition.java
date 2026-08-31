package org.jejuro.miraero.domain.transaction.dto.request;

import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@ApiModel(description = "거래 내역 조회 조건")
public class TransactionSearchCondition {

    @ApiModelProperty(value = "조회 연도", example = "2026")
    private final Integer year;
    @ApiModelProperty(value = "조회 월(1~12)", example = "8")
    private final Integer month;
    @ApiModelProperty(value = "지출 카테고리 ID", example = "1")
    private final Long categoryId;
    @ApiModelProperty(value = "페이지 번호. 1부터 시작", example = "1")
    private final Integer page;
    @ApiModelProperty(value = "페이지당 항목 수", example = "20")
    private final Integer size;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Long offset;

    @Builder
    public TransactionSearchCondition(
            Integer year,
            Integer month,
            Long categoryId,
            Integer page,
            Integer size
    ) {
        this.year = year;
        this.month = month;
        this.categoryId = categoryId;
        this.page = page;
        this.size = size;
    }

    public void setQueryRange(LocalDateTime startDateTime, LocalDateTime endDateTime, long offset) {
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.offset = offset;
    }
}
