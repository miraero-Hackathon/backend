package org.jejuro.miraero.domain.transaction.service;

import org.jejuro.miraero.domain.transaction.dto.response.PeerAverageResponse;

public interface PeerAverageService {

    PeerAverageResponse getPeerAverages(Long userId);
}
