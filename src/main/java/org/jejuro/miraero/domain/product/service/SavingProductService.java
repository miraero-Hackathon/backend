package org.jejuro.miraero.domain.product.service;

import org.jejuro.miraero.domain.product.dto.response.SavingProductListResponse;
import org.jejuro.miraero.domain.product.dto.response.SavingProductDetailResponse;

public interface SavingProductService {

    SavingProductListResponse getSavingProducts();

    SavingProductDetailResponse getSavingProductDetail(Long savingProductId);
}
