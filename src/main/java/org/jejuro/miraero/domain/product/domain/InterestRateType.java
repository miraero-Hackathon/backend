package org.jejuro.miraero.domain.product.domain;

import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;

public enum InterestRateType {

    SIMPLE("S", "단리"),
    COMPOUND("M", "복리");

    private final String code;
    private final String displayName;

    InterestRateType(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static InterestRateType fromCode(String code) {
        for (InterestRateType interestRateType : values()) {
            if (interestRateType.code.equals(code)) {
                return interestRateType;
            }
        }
        throw new BusinessException(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }
}
