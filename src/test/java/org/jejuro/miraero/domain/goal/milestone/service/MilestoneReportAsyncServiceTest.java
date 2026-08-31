package org.jejuro.miraero.domain.goal.milestone.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.jejuro.miraero.domain.goal.domain.Goal;
import org.jejuro.miraero.domain.goal.milestone.domain.Milestone;
import org.jejuro.miraero.domain.goal.milestone.dto.request.MilestoneReportAiRequest;
import org.jejuro.miraero.domain.goal.milestone.mapper.MilestoneMapper;
import org.jejuro.miraero.domain.goal.milestone.mapper.MilestoneReportMapper;
import org.jejuro.miraero.domain.goal.mapper.GoalMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
class MilestoneReportAsyncServiceTest {

    @Mock
    private GoalMapper goalMapper;

    @Mock
    private MilestoneMapper milestoneMapper;

    @Mock
    private MilestoneReportMapper milestoneReportMapper;

    @Mock
    private MilestoneReportDataService milestoneReportDataService;

    @Mock
    private MilestoneReportAiService milestoneReportAiService;

    @InjectMocks
    private MilestoneReportAsyncService milestoneReportAsyncService;

    private Goal goal;
    private Milestone milestone;
    private MilestoneReportAiRequest aiRequest;

    @BeforeEach
    void setUp() {

        goal = Goal.builder()
                .goalId(1L)
                .userId(1L)
                .goalName("여행 자금 모으기")
                .goalAmount(1_000_000L)
                .startAmount(0L)
                .startDate(LocalDate.of(2026, 1, 1))
                .goalDate(LocalDate.of(2026, 12, 31))
                .build();

        milestone = Milestone.builder()
                .milestoneId(1L)
                .goalId(1L)
                .milestonePercentage(25)
                .milestoneAmount(250_000L)
                .achievedAt(
                        LocalDateTime.of(
                                2026,
                                4,
                                1,
                                10,
                                0
                        )
                )
                .build();

        aiRequest = MilestoneReportAiRequest.builder()
                .build();
    }

    @Test
    void AI리포트_생성_성공() {

        // given
        when(goalMapper.findById(1L))
                .thenReturn(goal);

        when(milestoneMapper.findById(1L))
                .thenReturn(milestone);

        when(milestoneReportDataService.buildAiRequest(
                goal,
                milestone
        )).thenReturn(aiRequest);

        when(milestoneReportAiService.generateReport(
                aiRequest
        )).thenReturn(
                new MilestoneReportAiService.ParsedReport(
                        "지출 관리가 잘 되고 있어요",
                        "현재 목표 달성 과정에서 안정적으로 지출을 관리하고 있습니다."
                )
        );

        when(milestoneReportMapper.updateCompleted(
                eq(100L),
                eq("지출 관리가 잘 되고 있어요"),
                eq("현재 목표 달성 과정에서 안정적으로 지출을 관리하고 있습니다.")
        )).thenReturn(1);

        // when
        milestoneReportAsyncService.generate(
                1L,
                1L,
                100L
        );

        // then
        verify(goalMapper, times(1))
                .findById(1L);

        verify(milestoneMapper, times(1))
                .findById(1L);

        verify(milestoneReportDataService, times(1))
                .buildAiRequest(
                        goal,
                        milestone
                );

        verify(milestoneReportAiService, times(1))
                .generateReport(
                        aiRequest
                );

        verify(milestoneReportMapper, times(1))
                .updateCompleted(
                        eq(100L),
                        eq("지출 관리가 잘 되고 있어요"),
                        eq("현재 목표 달성 과정에서 안정적으로 지출을 관리하고 있습니다.")
                );

        verify(milestoneReportMapper, never())
                .updateFailed(100L);
    }

    @Test
    void AI리포트_생성_실패시_FAILED_처리() {

        // given
        when(goalMapper.findById(1L))
                .thenReturn(goal);

        when(milestoneMapper.findById(1L))
                .thenReturn(milestone);

        when(milestoneReportDataService.buildAiRequest(
                goal,
                milestone
        )).thenReturn(aiRequest);

        when(milestoneReportAiService.generateReport(
                aiRequest
        )).thenThrow(
                new RuntimeException("AI 생성 실패")
        );

        when(milestoneReportMapper.updateFailed(100L))
                .thenReturn(1);

        // when
        milestoneReportAsyncService.generate(
                1L,
                1L,
                100L
        );

        // then
        verify(milestoneReportAiService, times(1))
                .generateReport(aiRequest);

        verify(milestoneReportMapper, times(1))
                .updateFailed(100L);

        verify(milestoneReportMapper, never())
                .updateCompleted(
                        anyLong(),
                        anyString(),
                        anyString()
                );
    }

    @Test
    void 데이터_생성_실패시_FAILED_처리() {

        // given
        when(goalMapper.findById(1L))
                .thenReturn(goal);

        when(milestoneMapper.findById(1L))
                .thenReturn(milestone);

        when(milestoneReportDataService.buildAiRequest(
                goal,
                milestone
        )).thenThrow(
                new RuntimeException("데이터 생성 실패")
        );

        when(milestoneReportMapper.updateFailed(100L))
                .thenReturn(1);

        // when
        milestoneReportAsyncService.generate(
                1L,
                1L,
                100L
        );

        // then
        verify(milestoneReportDataService, times(1))
                .buildAiRequest(
                        goal,
                        milestone
                );

        verify(milestoneReportAiService, never())
                .generateReport(any());

        verify(milestoneReportMapper, times(1))
                .updateFailed(100L);
    }
}

