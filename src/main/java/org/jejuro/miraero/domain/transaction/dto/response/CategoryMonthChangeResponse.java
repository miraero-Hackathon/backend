package org.jejuro.miraero.domain.transaction.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CategoryMonthChangeResponse {

    private Long categoryId;
    private String categoryName;
    private Long previousMonthAmount;
    private Long currentMonthAmount;
    private Long changeAmount;
}
