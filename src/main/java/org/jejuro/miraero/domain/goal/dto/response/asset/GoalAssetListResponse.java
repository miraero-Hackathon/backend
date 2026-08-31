package org.jejuro.miraero.domain.goal.dto.response.asset;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalAssetListResponse {
    private List<GoalAssetResponse> assets;
}
