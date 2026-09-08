package org.jejuro.miraero.domain.credit.service;


import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.credit.domain.CreditScore;
import org.jejuro.miraero.domain.credit.domain.CreditScoreEvent;
import org.jejuro.miraero.domain.credit.domain.CreditScoreReasonCode;
import org.jejuro.miraero.domain.credit.dto.request.CreditScoreEventSearchCondition;
import org.jejuro.miraero.domain.credit.dto.response.CreditScoreEventResponse;
import org.jejuro.miraero.domain.credit.dto.response.CreditScoreResponse;
import org.jejuro.miraero.domain.credit.mapper.CreditScoreEventMapper;
import org.jejuro.miraero.domain.credit.mapper.CreditScoreMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.jejuro.miraero.global.response.PageResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreditScoreServiceImpl implements CreditScoreService {

    private static final int INITIAL_SCORE = 665;
    private static final int MIN_SCORE = 0;
    private static final int MAX_SCORE = 1000;
    private static final int MAX_PAGE_SIZE = 100;

    private final CreditScoreMapper creditScoreMapper;
    private final CreditScoreEventMapper creditScoreEventMapper;

    @Override
    @Transactional // 없던 행을 만드는(쓰기) 경우가 있어서 readOnly=true를 못 씀
    public CreditScoreResponse getCurrentScore(Long userId) {
        CreditScore creditScore = ensureCreditScoreExists(userId);
        return CreditScoreResponse.of(creditScore);
    }

    @Override
    @Transactional(readOnly = true) // 순수 조회라 readOnly. DB가 쓰기 잠금을 덜 걸어서 더 빠름
    public PageResponse<CreditScoreEventResponse> getHistory(
            Long userId,
            CreditScoreEventSearchCondition condition
    ) {
        validatePaging(condition);

        // page는 API 계약상 1부터 시작하는데, SQL의 OFFSET은 0부터라서 변환이 필요함.
        // page=1 → offset=0, page=2 → offset=size ...
        long offset = (long) (condition.getPage() - 1) * condition.getSize();
        condition.setOffset(offset);

        List<CreditScoreEventResponse> events = creditScoreEventMapper
                .findEventByUserId(userId, condition)
                .stream()
                .map(CreditScoreEventResponse::of)
                .collect(Collectors.toList());

        long totalElements = creditScoreEventMapper.countEventByUserId(userId);

        // PageResponse.of가 0-based page를 기대해서 여기서도 -1 해서 넘김
        // (TransactionServiceImpl.getTransactions와 동일한 관례)
        return PageResponse.of(events, condition.getPage() - 1, condition.getSize(), totalElements);
    }

    @Override
    @Transactional
    public void applyEvent(
            Long userId,
            int delta,
            CreditScoreReasonCode reasonCode,
            String description,
            LocalDateTime occurredAt
    ) {
        // 이 유저가 한 번도 조회된 적 없어도(=credit_score 행이 아예 없어도) 이벤트를
        // 반영할 수 있어야 하므로, 반영 전에 반드시 행 존재를 보장한다.
        ensureCreditScoreExists(userId);

        // 1) DB에서 원자적으로 delta 반영 + 0~1000 clamp
        creditScoreMapper.applyDelta(userId, delta, MIN_SCORE, MAX_SCORE);

        // 2) clamp 적용된 "진짜 반영값"을 이력에 남기기 위해 다시 조회.
        //    같은 트랜잭션 안에서 방금 UPDATE한 값을 그대로 읽으므로 안전함.
        CreditScore updated = creditScoreMapper.findByUserId(userId);

        // 3) 이벤트 로그 기록 (append-only, 절대 수정 안 됨)
        creditScoreEventMapper.save(
                CreditScoreEvent.builder()
                        .userId(userId)
                        .reasonCode(reasonCode)
                        .delta(delta)
                        .scoreAfter(updated.getCurrentScore())
                        .description(description)
                        .occurredAt(occurredAt)
                        .build()
        );
    }

    // 신용점수 행이 없으면 665점으로 만들고, 있으면 그대로 조회.
    // INSERT IGNORE라서 두 요청이 동시에 들어와도 하나만 실제로 insert되고
    // 나머지는 조용히 무시됨(멱등) → 그다음 findByUserId는 항상 안전하게 값을 찾음.
    private CreditScore ensureCreditScoreExists(Long userId) {

        CreditScore existing = creditScoreMapper.findByUserId(userId);
        if (existing != null) {
            return existing;
        }
        creditScoreMapper.insertIfAbsent(userId, INITIAL_SCORE);
        return creditScoreMapper.findByUserId(userId);
    }

    private void validatePaging(CreditScoreEventSearchCondition condition) {
        if (condition.getPage() == null || condition.getPage() < 1
                || condition.getSize() == null || condition.getSize() < 1
                || condition.getSize() > MAX_PAGE_SIZE) {
            throw new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE);
        }
    }
    @Override
    @Transactional
    public void initializeCreditScore(Long userId) {
        ensureCreditScoreExists(userId);
    }
}
