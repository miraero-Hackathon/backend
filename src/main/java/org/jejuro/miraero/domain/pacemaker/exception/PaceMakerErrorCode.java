package org.jejuro.miraero.domain.pacemaker.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PaceMakerErrorCode implements ErrorCode {

  NOT_REGISTERED(
      HttpStatus.BAD_REQUEST,
      "PACEMAKER_001",
      "Pace maker is not registered."
  ),

  INSUFFICIENT_BALANCE(
      HttpStatus.BAD_REQUEST,
      "PACEMAKER_002",
      "Pace maker balance is insufficient."
  ),

  GOAL_WITHDRAWAL_ACCOUNT_NOT_CONNECTED(
      HttpStatus.BAD_REQUEST,
      "PACEMAKER_003",
      "Goal withdrawal account is not connected."
  ),

  INVALID_DEPOSIT_AMOUNT(
      HttpStatus.BAD_REQUEST,
      "PACEMAKER_004",
      "Deposit amount must be greater than zero."
  ),

  FORBIDDEN_GOAL_ACCOUNT(
      HttpStatus.FORBIDDEN,
      "PACEMAKER_005",
      "Asset is not connected to user's goal, or is not a valid deposit target."
  ),

  ALREADY_REGISTERED(
      HttpStatus.CONFLICT,
      "PACEMAKER_006",
      "페이스메이커는 사용자당 하나만 개설할 수 있습니다."
  ),

  SALARY_ACCOUNT_NOT_FOUND(
      HttpStatus.BAD_REQUEST,
      "PACEMAKER_007",
      "급여 통장을 찾을 수 없습니다. 저금통을 만들 계좌를 직접 선택해 주세요."
  );

  private final HttpStatus status;
  private final String code;
  private final String message;
}
