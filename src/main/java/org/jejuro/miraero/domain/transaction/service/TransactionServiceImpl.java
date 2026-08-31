package org.jejuro.miraero.domain.transaction.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.transaction.dto.request.TransactionSearchCondition;
import org.jejuro.miraero.domain.transaction.dto.response.TransactionResponse;
import org.jejuro.miraero.domain.transaction.mapper.TransactionMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.jejuro.miraero.global.response.PageResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private static final int MIN_YEAR = 2000;
    private static final int MAX_PAGE_SIZE = 100;

    private final TransactionMapper transactionMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TransactionResponse> getTransactions(Long userId, TransactionSearchCondition condition) {
        validateUserId(userId);
        validateCondition(condition);
        setQueryRange(condition);

        List<TransactionResponse> transactions = transactionMapper.findTransactions(userId, condition)
                .stream()
                .map(transaction -> transaction.toResponse())
                .collect(Collectors.toList());
        long totalElements = transactionMapper.countTransactions(userId, condition);

        return PageResponse.of(
                transactions,
                condition.getPage() - 1,
                condition.getSize(),
                totalElements
        );
    }

    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private void validateCondition(TransactionSearchCondition condition) {
        if (condition == null
                || condition.getYear() == null
                || condition.getYear() < MIN_YEAR
                || condition.getYear() > Year.now().getValue() + 1
                || condition.getMonth() == null
                || condition.getMonth() < 1
                || condition.getMonth() > 12
                || condition.getPage() == null
                || condition.getPage() < 1
                || condition.getSize() == null
                || condition.getSize() < 1
                || condition.getSize() > MAX_PAGE_SIZE
                || (condition.getCategoryId() != null && condition.getCategoryId() <= 0)) {
            throw new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private void setQueryRange(TransactionSearchCondition condition) {
        LocalDate startDate = LocalDate.of(condition.getYear(), condition.getMonth(), 1);
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = startDate.plusMonths(1).atStartOfDay();
        long offset = (long) (condition.getPage() - 1) * condition.getSize();

        condition.setQueryRange(startDateTime, endDateTime, offset);
    }
}
