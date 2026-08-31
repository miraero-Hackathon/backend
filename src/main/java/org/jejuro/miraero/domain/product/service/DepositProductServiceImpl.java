package org.jejuro.miraero.domain.product.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.product.domain.DepositProductListQueryResult;
import org.jejuro.miraero.domain.product.domain.DepositProductDetailQueryResult;
import org.jejuro.miraero.domain.product.domain.DepositOption;
import org.jejuro.miraero.domain.product.domain.InterestRateType;
import org.jejuro.miraero.domain.product.dto.response.DepositOptionResponse;
import org.jejuro.miraero.domain.product.dto.response.DepositProductDetailResponse;
import org.jejuro.miraero.domain.product.dto.response.DepositProductListResponse;
import org.jejuro.miraero.domain.product.dto.response.DepositProductResponse;
import org.jejuro.miraero.domain.product.mapper.DepositProductMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DepositProductServiceImpl implements DepositProductService {

    private final DepositProductMapper depositProductMapper;

    @Override
    @Transactional(readOnly = true)
    public DepositProductListResponse getDepositProducts() {
        Map<Long, DepositProductResponse> productsById = new LinkedHashMap<>();

        for (DepositProductListQueryResult result : depositProductMapper.findDepositProductList()) {
            DepositProductResponse product = productsById.computeIfAbsent(
                    result.getDepositProductId(),
                    ignored -> new DepositProductResponse(
                            result.getDepositProductId(),
                            result.getFinancialInstitutionName(),
                            result.getProductName(),
                            result.getMaxInterestRate(),
                            new ArrayList<>(),
                            result.getMaxLimit(),
                            result.getJoinMethod(),
                            result.getHasJoinRestriction(),
                            result.getHasSpecialCondition()
                    )
            );
            product.getSaveTerms().add(result.getSaveTerm());
        }

        return new DepositProductListResponse(new ArrayList<>(productsById.values()));
    }

    @Override
    @Transactional(readOnly = true)
    public DepositProductDetailResponse getDepositProductDetail(Long depositProductId) {
        validateDepositProductId(depositProductId);

        DepositProductDetailQueryResult product = depositProductMapper
                .findDepositProductDetailById(depositProductId);
        if (product == null) {
            throw new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND);
        }

        List<DepositOption> options = depositProductMapper
                .findDepositOptionsByDepositProductId(depositProductId);

        return new DepositProductDetailResponse(
                product.getDepositProductId(),
                product.getFinancialInstitutionName(),
                product.getProductName(),
                product.getJoinMethod(),
                product.getJoinTarget(),
                product.getJoinRestriction(),
                product.getHasJoinRestriction(),
                product.getSpecialCondition(),
                product.getMaturityInterest(),
                product.getMaxLimit(),
                product.getNotice(),
                product.getDisclosureMonth(),
                product.getDisclosureStartDate(),
                product.getDisclosureEndDate(),
                product.getProductPageUrl(),
                toDepositOptionResponses(options)
        );
    }

    private void validateDepositProductId(Long depositProductId) {
        if (depositProductId == null || depositProductId <= 0) {
            throw new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private List<DepositOptionResponse> toDepositOptionResponses(List<DepositOption> options) {
        List<DepositOptionResponse> responses = new ArrayList<>();
        for (DepositOption option : options == null ? Collections.<DepositOption>emptyList() : options) {
            responses.add(new DepositOptionResponse(
                    option.getDepositOptionId(),
                    InterestRateType.fromCode(option.getInterestRateType()).getDisplayName(),
                    option.getSaveTerm(),
                    option.getBaseInterestRate(),
                    option.getMaxInterestRate()
            ));
        }
        return responses;
    }
}
