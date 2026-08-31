package org.jejuro.miraero.domain.goal.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GoalErrorCode implements ErrorCode {

    // Goal
    GOAL_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "GOAL_001",
            "목표를 찾을 수 없습니다."
    ),

    GOAL_NOT_COMPLETED(
            HttpStatus.BAD_REQUEST,
            "GOAL_002",
            "완료된 목표만 컬렉션에 저장할 수 있습니다."
    ),

    GOAL_ASSET_ALREADY_CONNECTED(
            HttpStatus.CONFLICT,
            "GOAL_003",
                    "이미 연결된 자산입니다."
    ),

    GOAL_ASSET_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "GOAL_004",
            "연결된 목표 자산을 찾을 수 없습니다."
    ),

    GOAL_COMPLETED(
            HttpStatus.BAD_REQUEST,
        "GOAL_005",
        "완료된 목표는 상태를 변경할 수 없습니다."
    ),

    INVALID_STATUS_CHANGE(
            HttpStatus.BAD_REQUEST,
        "GOAL_006",
        "변경할 수 없는 목표 상태입니다."
    ),

    GOAL_ACCESS_DENIED(
            HttpStatus.FORBIDDEN,
            "GOAL_007",
                    "다른 사용자의 목표입니다."
    ),

    PULL_SOURCE_ACCOUNT_LINKED(
            HttpStatus.BAD_REQUEST,
        "GOAL_008",
        "이미 목표에 연결된 계좌는 끌어쓰기 출처로 쓸 수 없습니다."
    ),

    PULL_INSUFFICIENT_BALANCE(
            HttpStatus.BAD_REQUEST,
        "GOAL_009",
        "출처 계좌 잔액이 부족합니다."
    ),

    PULL_TARGET_NOT_SUPPORTED(
            HttpStatus.BAD_REQUEST,
        "GOAL_010",
        "끌어올 수 있는 목표 자산(예적금, 저금통)이 연결되어 있지 않습니다."
    ),
    INVALID_GOAL_AMOUNT(
            HttpStatus.BAD_REQUEST,
            "GOAL_011",
            "목표 금액은 0보다 커야 합니다."
    ),

    INVALID_START_AMOUNT(
            HttpStatus.BAD_REQUEST,
            "GOAL_012",
            "시작 금액은 0 이상이어야 합니다."
    ),

    INVALID_GOAL_MONTHS(
            HttpStatus.BAD_REQUEST,
            "GOAL_013",
            "목표 기간은 1개월 이상이어야 합니다."
    ),

    INVALID_GOAL_NAME(
            HttpStatus.BAD_REQUEST,
            "GOAL_014",
            "목표 이름은 비어 있을 수 없습니다."
    ),

    START_AMOUNT_EXCEEDS_GOAL_AMOUNT(
            HttpStatus.BAD_REQUEST,
            "GOAL_015",
            "시작 금액은 목표 금액보다 클 수 없습니다."
    ),

    INVALID_GOAL_ASSET(
            HttpStatus.BAD_REQUEST,
            "GOAL_016",
            "연결할 수 없는 자산입니다."
    ),
    INVALID_GOAL_TYPE(
            HttpStatus.BAD_REQUEST,
            "GOAL_017",
            "목표 유형은 INDEPENDENCE, EMERGENCY, WEDDING, LOAN만 가능합니다."
    ),

    PULL_SAME_ACCOUNT(
            HttpStatus.BAD_REQUEST,
            "GOAL_018",
            "목표 자산이 있는 계좌에서는 끌어쓸 수 없습니다."
    );




    private final HttpStatus status;
    private final String Code;
    private final String message;
}
