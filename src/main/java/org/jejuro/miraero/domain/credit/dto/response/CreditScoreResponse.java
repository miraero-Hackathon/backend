package org.jejuro.miraero.domain.credit.dto.response;


import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import org.jejuro.miraero.domain.credit.domain.CreditScore;


@Getter
@ApiModel(description = "신용점수 조회 응답")
public class CreditScoreResponse {
    @ApiModelProperty(value = "현재 신용점수", example = "665")
    private final int currentScore;

    @ApiModelProperty(value = "마지막 변경 시각")
    private final LocalDateTime updatedAt;

    private CreditScoreResponse(int currentScore, LocalDateTime updatedAt) {
        this.currentScore = currentScore;
        this.updatedAt = updatedAt;
    }

    public static CreditScoreResponse of(CreditScore creditScore) {
        return new CreditScoreResponse(creditScore.getCurrentScore(), creditScore.getUpdatedAt());
    }

}
