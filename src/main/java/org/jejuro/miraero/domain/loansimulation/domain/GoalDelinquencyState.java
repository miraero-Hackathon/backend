package org.jejuro.miraero.domain.loansimulation.domain;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalDelinquencyState {
    private Long goalDelinquencyStateId;
    private Long goalId;

    private LocalDate behindSince;
    private boolean shortTermFired;
    private boolean longTermFired;

    private LocalDate onTrackSince;
    private boolean consecutiveFired;

    public void setBehindSince(LocalDate behindSince) {
        this.behindSince = behindSince;
    }

    public void setShortTermFired(boolean shortTermFired) {
        this.shortTermFired = shortTermFired;
    }

    public void setLongTermFired(boolean longTermFired) {
        this.longTermFired = longTermFired;
    }

    public void setOnTrackSince(LocalDate onTrackSince) {
        this.onTrackSince = onTrackSince;
    }

    public void setConsecutiveFired(boolean consecutiveFired) {
        this.consecutiveFired = consecutiveFired;
    }
}
