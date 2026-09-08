package org.jejuro.miraero.domain.loansimulation.dto.response;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import org.jejuro.miraero.domain.goal.dto.response.GoalPaceResponse;

import java.time.LocalDate;

@Getter
@Builder
@ApiModel(description = "대출 시뮬레이션 상세 조회 응답")
public class LoanSimulationDetailResponse {

    @ApiModelProperty(value = "목표 ID", example = "1")
    private Long goalId;

    @ApiModelProperty(value = "목표(대출) 이름", example = "내 집 마련")
    private String goalName;

    @ApiModelProperty(value = "원금(미러링된 대출 원금) = 목표 금액", example = "30000000")
    private Long goalAmount;

    @ApiModelProperty(value = "현재까지 상환액 = 목표에 실제로 모인 금액", example = "11500000")
    private Long currentAmount;

    @ApiModelProperty(value = "대출 개시일 = 목표 시작일")
    private LocalDate startDate;

    @ApiModelProperty(value = "상환 진행률(%). GoalDetailResponse와 동일한 계산식", example = "38")
    private Integer progressRate;

    @ApiModelProperty(value = "약정 상환 페이스(월 필요액)", example = "3000000")
    private Long requiredMonthlyAmount;

    @ApiModelProperty(value = "이번 달(1일~말일)에 실제로 상환한 금액", example = "800000")
    private Long monthlyRepaidAmount;

    @ApiModelProperty(value = "목표 달성 페이스(기대치 대비 AHEAD/ON_TRACK/BEHIND)")
    private GoalPaceResponse pace;

    @ApiModelProperty(value = "부족(BEHIND) 상태 시작일. 정상이면 null")
    private LocalDate behindSince;

    @ApiModelProperty(value = "부족 상태 지속 일수. behindSince가 없으면 null", example = "12")
    private Integer daysBehind;

    @ApiModelProperty(value = "단기 연체(30일) 가점/차감이 이번 연체 기간에 이미 발생했는지")
    private boolean shortTermFired;

    @ApiModelProperty(value = "장기 연체(90일) 가점/차감이 이번 연체 기간에 이미 발생했는지")
    private boolean longTermFired;

    @ApiModelProperty(value = "단기 연체(30일) 임계값을 넘은 누적 횟수", example = "1")
    private int overdueCount;

    @ApiModelProperty(value = "정상(부족 아님) 상태 시작일. 연체 중이면 null")
    private LocalDate onTrackSince;

    @ApiModelProperty(value = "연속 정상 상환 지속 개월 수. onTrackSince가 없으면 null", example = "6")
    private Integer monthsOnTrack;

    @ApiModelProperty(value = "연속 상환(6개월) 가점이 이번 정상 기간에 이미 발생했는지")
    private boolean consecutiveFired;

    @ApiModelProperty(value = "단기 연체 판정 기준 일수", example = "30")
    private int shortTermOverdueDays;

    @ApiModelProperty(value = "장기 연체 판정 기준 일수", example = "90")
    private int longTermOverdueDays;

    @ApiModelProperty(value = "연속 상환 가점 판정 기준 개월 수", example = "6")
    private int consecutiveRepaymentMonths;
}
