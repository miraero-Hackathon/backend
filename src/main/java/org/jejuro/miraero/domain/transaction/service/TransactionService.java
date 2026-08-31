package org.jejuro.miraero.domain.transaction.service;

import org.jejuro.miraero.domain.transaction.dto.request.TransactionSearchCondition;
import org.jejuro.miraero.domain.transaction.dto.response.TransactionResponse;
import org.jejuro.miraero.global.response.PageResponse;

public interface TransactionService {

    PageResponse<TransactionResponse> getTransactions(Long userId, TransactionSearchCondition condition);
}
