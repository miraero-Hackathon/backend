package org.jejuro.miraero.domain.credit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;
import org.jejuro.miraero.domain.credit.domain.CreditScore;
import org.jejuro.miraero.domain.credit.domain.CreditScoreEvent;
import org.jejuro.miraero.domain.credit.domain.CreditScoreReasonCode;
import org.jejuro.miraero.domain.credit.dto.request.CreditScoreEventSearchCondition;
import org.jejuro.miraero.domain.credit.dto.response.CreditScoreEventResponse;
import org.jejuro.miraero.domain.credit.dto.response.CreditScoreResponse;
import org.jejuro.miraero.domain.credit.mapper.CreditScoreEventMapper;
import org.jejuro.miraero.domain.credit.mapper.CreditScoreMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.response.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreditScoreServiceImplTest {

    @Mock
    private CreditScoreMapper creditScoreMapper;

    @Mock
    private CreditScoreEventMapper creditScoreEventMapper;

    @InjectMocks
    private CreditScoreServiceImpl creditScoreService;

    // 테스트마다 반복되는 CreditScore 생성을 줄이기 위한 헬퍼
    private CreditScore scoreOf(Long userId, int currentScore) {
        return CreditScore.builder()
                .creditScoreId(1L)
                .userId(userId)
                .currentScore(currentScore)
                .build();
    }

    @Test
    @DisplayName("이미 신용점수 행이 있는 유저는 그대로 조회만 하고 초기화하지 않는다")
    void getCurrentScore_existingUser_doesNotInitialize() {
        // given
        Long userId = 1L;
        given(creditScoreMapper.findByUserId(userId))
                .willReturn(scoreOf(userId, 700));

        // when
        CreditScoreResponse response = creditScoreService.getCurrentScore(userId);

        // then
        assertEquals(700, response.getCurrentScore());
        verify(creditScoreMapper, never()).insertIfAbsent(any(), anyInt());
    }

    @Test
    @DisplayName("신용점수 행이 없는 유저는 665점으로 초기화한 뒤 조회한다")
    void getCurrentScore_newUser_initializesTo665() {
        // given
        Long userId = 2L;
        // 첫 조회는 없음(null) → insertIfAbsent 호출 → 두 번째 조회에서 665점 반환
        given(creditScoreMapper.findByUserId(userId))
                .willReturn(null, scoreOf(userId, 665));

        // when
        CreditScoreResponse response = creditScoreService.getCurrentScore(userId);

        // then
        assertEquals(665, response.getCurrentScore());
        verify(creditScoreMapper).insertIfAbsent(userId, 665);
    }

    @Test
    @DisplayName("이벤트 적용 시 delta를 반영하고, 반영 후 점수로 이력을 남긴다")
    void applyEvent_appliesDeltaAndSavesEventWithScoreAfter() {
        // given
        Long userId = 3L;
        LocalDateTime occurredAt = LocalDateTime.of(2026, 8, 31, 0, 0);

        // ensureCreditScoreExists용 첫 조회(존재함) → applyDelta 이후 재조회(반영된 값)
        given(creditScoreMapper.findByUserId(userId))
                .willReturn(scoreOf(userId, 665), scoreOf(userId, 565));

        // when
        creditScoreService.applyEvent(
                userId,
                -100,
                CreditScoreReasonCode.SHORT_TERM_OVERDUE,
                "단기 연체 테스트",
                occurredAt
        );

        // then
        verify(creditScoreMapper).applyDelta(userId, -100, 0, 1000);
        verify(creditScoreMapper, never()).insertIfAbsent(any(), anyInt());

        ArgumentCaptor<CreditScoreEvent> captor = ArgumentCaptor.forClass(CreditScoreEvent.class);
        verify(creditScoreEventMapper).save(captor.capture());

        CreditScoreEvent savedEvent = captor.getValue();
        assertEquals(userId, savedEvent.getUserId());
        assertEquals(CreditScoreReasonCode.SHORT_TERM_OVERDUE, savedEvent.getReasonCode());
        assertEquals(-100, savedEvent.getDelta());
        assertEquals(565, savedEvent.getScoreAfter()); // clamp 반영된 실제 값을 이력에 남김
        assertEquals("단기 연체 테스트", savedEvent.getDescription());
        assertEquals(occurredAt, savedEvent.getOccurredAt());
    }

    @Test
    @DisplayName("신용점수 행이 없는 유저에게 이벤트를 적용하면 먼저 665점으로 초기화한다")
    void applyEvent_newUser_initializesBeforeApplying() {
        // given
        Long userId = 4L;

        // 1) ensureCreditScoreExists 내부 조회: 없음(null)
        // 2) insertIfAbsent 이후 ensureCreditScoreExists가 재조회: 665점
        // 3) applyDelta 이후 재조회: 705점(가점 반영됨)
        given(creditScoreMapper.findByUserId(userId))
                .willReturn(null, scoreOf(userId, 665), scoreOf(userId, 705));

        // when
        creditScoreService.applyEvent(
                userId,
                40,
                CreditScoreReasonCode.CHECK_CARD_USAGE_STREAK,
                "체크카드 6개월 이상 연속 30만원 이상 사용",
                LocalDateTime.now()
        );

        // then
        verify(creditScoreMapper).insertIfAbsent(userId, 665);
        verify(creditScoreMapper).applyDelta(userId, 40, 0, 1000);

        ArgumentCaptor<CreditScoreEvent> captor = ArgumentCaptor.forClass(CreditScoreEvent.class);
        verify(creditScoreEventMapper).save(captor.capture());
        assertEquals(705, captor.getValue().getScoreAfter());
    }

    @Test
    @DisplayName("페이지 조건이 유효하면 offset을 변환해 이력을 조회한다")
    void getHistory_success_convertsPageToOffset() {
        // given
        Long userId = 5L;
        CreditScoreEventSearchCondition condition =
                CreditScoreEventSearchCondition.builder().page(2).size(10).build();

        CreditScoreEvent event = CreditScoreEvent.builder()
                .userId(userId)
                .reasonCode(CreditScoreReasonCode.UTILITY_PAYMENT_STREAK)
                .delta(3)
                .scoreAfter(668)
                .description("통신비 6개월 연속 납부")
                .occurredAt(LocalDateTime.now())
                .build();

        given(creditScoreEventMapper.findEventByUserId(eq(userId), any()))
                .willReturn(List.of(event));
        given(creditScoreEventMapper.countEventByUserId(userId)).willReturn(1L);

        // when
        PageResponse<CreditScoreEventResponse> response =
                creditScoreService.getHistory(userId, condition);

        // then
        // page=2, size=10 → offset은 (2-1)*10=10 이어야 함
        assertEquals(10L, condition.getOffset());
        assertEquals(1, response.getContent().size());
        assertEquals("UTILITY_PAYMENT_STREAK", response.getContent().get(0).getReasonCode());
        assertEquals(1L, response.getTotalElements());
    }

    @Test
    @DisplayName("page가 1보다 작으면 예외가 발생한다")
    void getHistory_invalidPage_throws() {
        // given
        Long userId = 6L;
        CreditScoreEventSearchCondition condition =
                CreditScoreEventSearchCondition.builder().page(0).size(10).build();

        // when & then
        assertThrows(
                BusinessException.class,
                () -> creditScoreService.getHistory(userId, condition)
        );
    }

    @Test
    @DisplayName("size가 최대치(100)를 넘으면 예외가 발생한다")
    void getHistory_sizeTooLarge_throws() {
        // given
        Long userId = 7L;
        CreditScoreEventSearchCondition condition =
                CreditScoreEventSearchCondition.builder().page(1).size(101).build();

        // when & then
        assertThrows(
                BusinessException.class,
                () -> creditScoreService.getHistory(userId, condition)
        );
    }
}
