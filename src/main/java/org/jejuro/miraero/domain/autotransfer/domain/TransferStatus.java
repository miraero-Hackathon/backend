package org.jejuro.miraero.domain.autotransfer.domain;

/**
 * 적립 이력의 결과 상태. auto_saving_history.transfer_status와 값이 같아야 한다.
 */
public enum TransferStatus {
    SUCCESS,
    PARTIAL_LIMIT,
    FAILED_INSUFFICIENT_FUNDS
}
