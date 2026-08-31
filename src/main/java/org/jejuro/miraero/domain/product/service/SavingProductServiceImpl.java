package org.jejuro.miraero.domain.product.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.product.domain.ReserveType;
import org.jejuro.miraero.domain.product.domain.InterestRateType;
import org.jejuro.miraero.domain.product.domain.SavingOption;
import org.jejuro.miraero.domain.product.domain.SavingProductDetailQueryResult;
import org.jejuro.miraero.domain.product.domain.SavingProductListQueryResult;
import org.jejuro.miraero.domain.product.dto.response.SavingOptionResponse;
import org.jejuro.miraero.domain.product.dto.response.SavingProductDetailResponse;
import org.jejuro.miraero.domain.product.dto.response.SavingProductListResponse;
import org.jejuro.miraero.domain.product.dto.response.SavingProductResponse;
import org.jejuro.miraero.domain.product.mapper.SavingProductMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SavingProductServiceImpl implements SavingProductService {

    private final SavingProductMapper savingProductMapper;

    @Override
    @Transactional(readOnly = true)
    public SavingProductListResponse getSavingProducts() {
        Map<Long, SavingProductResponse> productsById = new LinkedHashMap<>();
        Map<Long, EnumSet<ReserveType>> reserveTypesByProductId = new LinkedHashMap<>();

        for (SavingProductListQueryResult result : savingProductMapper.findSavingProductList()) {
            SavingProductResponse product = productsById.computeIfAbsent(
                    result.getSavingProductId(),
                    ignored -> new SavingProductResponse(
                            result.getSavingProductId(),
                            result.getFinancialInstitutionName(),
                            result.getProductName(),
                            result.getHighestInterestRate(),
                            new ArrayList<>(),
                            new ArrayList<>(),
                            result.getMaxLimit(),
                            result.getJoinMethod(),
                            result.getHasJoinRestriction(),
                            result.getHasSpecialCondition()
                    )
            );
            if (!product.getSaveTerms().contains(result.getSaveTerm())) {
                product.getSaveTerms().add(result.getSaveTerm());
            }
            reserveTypesByProductId
                    .computeIfAbsent(result.getSavingProductId(), ignored -> EnumSet.noneOf(ReserveType.class))
                    .add(ReserveType.fromCode(result.getReserveType()));
        }

        for (Map.Entry<Long, SavingProductResponse> entry : productsById.entrySet()) {
            EnumSet<ReserveType> reserveTypes = reserveTypesByProductId.get(entry.getKey());
            for (ReserveType reserveType : ReserveType.values()) {
                if (reserveTypes.contains(reserveType)) {
                    entry.getValue().getReserveTypes().add(reserveType.getDisplayName());
                }
            }
        }

        return new SavingProductListResponse(new ArrayList<>(productsById.values()));
    }

    @Override
    @Transactional(readOnly = true)
    public SavingProductDetailResponse getSavingProductDetail(Long savingProductId) {
        validateSavingProductId(savingProductId);

        SavingProductDetailQueryResult product = savingProductMapper
                .findSavingProductDetailById(savingProductId);
        if (product == null) {
            throw new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND);
        }

        List<SavingOption> options = savingProductMapper
                .findSavingOptionsBySavingProductId(savingProductId);

        return new SavingProductDetailResponse(
                product.getSavingProductId(),
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
                toSavingOptionResponses(options)
        );
    }

    private void validateSavingProductId(Long savingProductId) {
        if (savingProductId == null || savingProductId <= 0) {
            throw new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private List<SavingOptionResponse> toSavingOptionResponses(List<SavingOption> options) {
        List<SavingOptionResponse> responses = new ArrayList<>();
        for (SavingOption option : options == null ? Collections.<SavingOption>emptyList() : options) {
            responses.add(new SavingOptionResponse(
                    option.getSavingOptionId(),
                    InterestRateType.fromCode(option.getInterestRateType()).getDisplayName(),
                    ReserveType.fromCode(option.getReserveType()).getDisplayName(),
                    option.getSaveTerm(),
                    option.getBaseInterestRate(),
                    option.getMaxInterestRate()
            ));
        }
        return responses;
    }
}
