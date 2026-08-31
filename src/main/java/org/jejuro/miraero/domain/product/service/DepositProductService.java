package org.jejuro.miraero.domain.product.service;

import org.jejuro.miraero.domain.product.dto.response.DepositProductListResponse;
import org.jejuro.miraero.domain.product.dto.response.DepositProductDetailResponse;

public interface DepositProductService {

    DepositProductListResponse getDepositProducts();

    DepositProductDetailResponse getDepositProductDetail(Long depositProductId);
}
