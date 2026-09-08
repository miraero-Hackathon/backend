package org.jejuro.miraero.domain.credit.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
public class CreditScoreEventSearchCondition {
    private final Integer page;
    private final Integer size;

    private Long offset;

    @Builder
    public CreditScoreEventSearchCondition(Integer page, Integer size) {
        this.page = page;
        this.size = size;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }
}
