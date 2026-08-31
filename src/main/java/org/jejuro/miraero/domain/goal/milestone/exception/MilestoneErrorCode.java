package org.jejuro.miraero.domain.goal.milestone.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MilestoneErrorCode implements ErrorCode {

    MILESTONE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "MILESTONE_001",
            "마일스톤을 찾을 수 없습니다."
    ),

    MILESTONE_REPORT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "MILESTONE_002",
            "마일스톤 리포트를 찾을 수 없습니다."
    ),

    MILESTONE_REPORT_GENERATION_FAILED(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "MILESTONE_003",
            "마일스톤 리포트 생성에 실패했습니다."
    ),

    MILESTONE_INVALID_REQUEST(
            HttpStatus.BAD_REQUEST,
            "MILESTONE_004",
            "마일스톤 요청 정보가 올바르지 않습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}