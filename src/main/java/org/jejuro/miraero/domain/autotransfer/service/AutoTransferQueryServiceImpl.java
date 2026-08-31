package org.jejuro.miraero.domain.autotransfer.service;

import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.autotransfer.mapper.AutoTransferMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AutoTransferQueryServiceImpl implements AutoTransferQueryService {

    private final AutoTransferMapper autoTransferMapper;
    @Override
    public Long getTargetGoalTransferAmount(Long goalId) {

        Long amount =
                autoTransferMapper.findTargetGoalAutoTransferAmount(goalId);

        return amount == null ? 0L : amount;
    }

    @Override
    public Long getOtherGoalTransferAmount(Long userId, Long goalId) {
        if (userId == null) return 0L;

        Long amount = autoTransferMapper.findOtherGoalAutoTransferAmount(userId, goalId);

        return amount == null ? 0L : amount;
    }

    @Override
    public Long getTotalTransferAmount(Long userId) {
        if (userId == null) return 0L;

        Long amount = autoTransferMapper.findTotalAutoTransferAmount(userId);

        return amount == null ? 0L : amount;
    }
}
