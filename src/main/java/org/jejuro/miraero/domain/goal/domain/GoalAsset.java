package org.jejuro.miraero.domain.goal.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class GoalAsset {
    private Long goalAssetId;
    private Long goalId;
    private AssetType assetType;
    private Long assetId;
    private LocalDateTime createdAt;

    @Builder
    public GoalAsset(Long goalAssetId, Long goalId, AssetType assetType, Long assetId) {
        this.goalAssetId = goalAssetId;
        this.goalId = goalId;
        this.assetType = assetType;
        this.assetId = assetId;
    }
}
