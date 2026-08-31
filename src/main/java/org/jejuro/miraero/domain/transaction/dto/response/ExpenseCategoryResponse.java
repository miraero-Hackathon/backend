package org.jejuro.miraero.domain.transaction.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@ApiModel(description = "지출 카테고리")
public class ExpenseCategoryResponse {

    @ApiModelProperty(value = "카테고리 ID", example = "1")
    private Long categoryId;
    @ApiModelProperty(value = "카테고리명", example = "식비")
    private String categoryName;

    public static ExpenseCategoryResponse of(Long categoryId, String categoryName) {
        return new ExpenseCategoryResponse(categoryId, categoryName);
    }
}
