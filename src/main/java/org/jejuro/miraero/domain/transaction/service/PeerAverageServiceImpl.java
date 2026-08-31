package org.jejuro.miraero.domain.transaction.service;

import java.time.YearMonth;
import java.util.List;
import java.time.YearMonth;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.transaction.domain.PeerCategoryAverageQueryResult;
import org.jejuro.miraero.domain.transaction.dto.response.PeerAverageCategoryResponse;
import org.jejuro.miraero.domain.transaction.dto.response.PeerAverageResponse;
import org.jejuro.miraero.domain.transaction.mapper.PeerAverageMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PeerAverageServiceImpl implements PeerAverageService {

    private final PeerAverageMapper peerAverageMapper;

    @Override
    @Transactional(readOnly = true)
    public PeerAverageResponse getPeerAverages(Long userId) {
        validate(userId);

        YearMonth yearMonth = YearMonth.now();
        List<PeerAverageCategoryResponse> categories = peerAverageMapper.findCategoryPeerAverages(
                        userId,
                        yearMonth.atDay(1).atStartOfDay(),
                        yearMonth.plusMonths(1).atDay(1).atStartOfDay()
                ).stream()
                .map(PeerAverageCategoryResponse::from)
                .collect(Collectors.toList());

        return new PeerAverageResponse(categories);
    }

    private void validate(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
