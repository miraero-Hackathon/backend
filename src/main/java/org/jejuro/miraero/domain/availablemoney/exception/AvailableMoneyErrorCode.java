package org.jejuro.miraero.domain.availablemoney.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;


@Getter
@RequiredArgsConstructor
public enum AvailableMoneyErrorCode implements ErrorCode {
    SALARY_HISTORY_NOT_FOUND(
            HttpStatus.BAD_REQUEST,
            "AVAILABLE_MONEY_001",
            "급여 내역을 찾을 수 없습니다."
    );

    private final HttpStatus status;
    private final String Code;
    private final String message;
}
