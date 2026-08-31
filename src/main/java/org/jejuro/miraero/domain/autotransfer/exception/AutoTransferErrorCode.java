package org.jejuro.miraero.domain.autotransfer.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AutoTransferErrorCode implements ErrorCode {

    FUTURE_EXECUTION_DATE(
            HttpStatus.BAD_REQUEST,
            "AUTO_TRANSFER_001",
            "아직 오지 않은 날짜의 자동이체는 실행할 수 없습니다."
    ),

    INVALID_TRANSFER_AMOUNT(
            HttpStatus.BAD_REQUEST,
            "AUTO_TRANSFER_002",
            "자동이체 금액은 0보다 커야 합니다."
    ),

    INVALID_TRANSFER_DAY(
            HttpStatus.BAD_REQUEST,
            "AUTO_TRANSFER_003",
            "자동이체일은 1일부터 31일 사이여야 합니다."
    ),

    AUTO_TRANSFER_ALREADY_EXISTS(
            HttpStatus.CONFLICT,
            "AUTO_TRANSFER_004",
            "해당 저금통에는 이미 자동이체가 설정되어 있습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
