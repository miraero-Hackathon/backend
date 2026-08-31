package org.jejuro.miraero.domain.goal.domain;

import org.jejuro.miraero.domain.goal.exception.GoalErrorCode;
import org.jejuro.miraero.global.exception.BusinessException;

public enum GoalType {
    INDEPENDENCE,
    EMERGENCY,
    WEDDING,
    LOAN;

    public static GoalType from(String value) {
        if (value == null) {
            throw new BusinessException(GoalErrorCode.INVALID_GOAL_TYPE);
        }

        try {
            return GoalType.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(GoalErrorCode.INVALID_GOAL_TYPE);
        }
    }
}
