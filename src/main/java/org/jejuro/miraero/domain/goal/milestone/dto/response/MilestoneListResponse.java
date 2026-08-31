package org.jejuro.miraero.domain.goal.milestone.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MilestoneListResponse {

    private List<MilestoneResponse> milestones;

    public static MilestoneListResponse of(
            List<MilestoneResponse> milestones
    ) {
        return MilestoneListResponse.builder()
                .milestones(milestones)
                .build();
    }
}