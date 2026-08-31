package org.jejuro.miraero.domain.transaction.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jejuro.miraero.domain.transaction.domain.PeerCategoryAverageQueryResult;

@Getter
@AllArgsConstructor
public class PeerAverageCategoryResponse {

    private Long categoryId;
    private String categoryName;
    private Long peerAverageAmount;

    public static PeerAverageCategoryResponse from(PeerCategoryAverageQueryResult result) {
        return new PeerAverageCategoryResponse(
                result.getCategoryId(),
                result.getCategoryName(),
                result.getPeerAverageAmount()
        );
    }
}
