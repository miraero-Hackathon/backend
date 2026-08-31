package org.jejuro.miraero.domain.goal.dto.response.asset;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jejuro.miraero.domain.autotransfer.dto.response.AutoTransferResponse;
import org.jejuro.miraero.domain.goal.domain.AssetType;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalAssetResponse {
    private AssetType assetType;
    private Long assetId;
    private String assetName;
    private String bankName;
    private String accountNumberMasked;
    private Long balance;

    private AssetDetailResponse assetDetail;
    private AutoTransferResponse autoTransfer;
}
