package org.jejuro.miraero.domain.product.domain;

import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;

public enum ReserveType {

    FIXED("S", "정액적립식"),
    FLEXIBLE("F", "자유적립식");

    private final String code;
    private final String displayName;

    ReserveType(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static ReserveType fromCode(String code) {
        for (ReserveType reserveType : values()) {
            if (reserveType.code.equals(code)) {
                return reserveType;
            }
        }
        throw new BusinessException(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }
}
