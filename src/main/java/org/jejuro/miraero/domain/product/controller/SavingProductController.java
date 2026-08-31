package org.jejuro.miraero.domain.product.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.product.dto.response.SavingProductDetailResponse;
import org.jejuro.miraero.domain.product.dto.response.SavingProductListResponse;
import org.jejuro.miraero.domain.product.service.SavingProductService;
import org.jejuro.miraero.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/savings")
@Api(tags = "금융상품 - 적금")
public class SavingProductController {

    private final SavingProductService savingProductService;

    @GetMapping
    @ApiOperation(value = "적금 상품 목록 조회", notes = "금융감독원 공시 기반 적금 상품 목록을 조회합니다. 각 상품에는 최고 금리, 납입 방식, 가입 기간, 한도와 가입 제한 여부가 포함됩니다.")
    public ResponseEntity<ApiResponse<SavingProductListResponse>> getSavingProducts() {
        return ResponseEntity.ok(ApiResponse.success(savingProductService.getSavingProducts()));
    }

    @GetMapping("/{savingProductId}")
    @ApiOperation(value = "적금 상품 상세 조회", notes = "상품의 가입 조건, 공시 기간, 상품 페이지 URL 및 납입 방식·기간별 금리 옵션을 조회합니다.")
    public ResponseEntity<ApiResponse<SavingProductDetailResponse>> getSavingProductDetail(
            @ApiParam(value = "적금 상품 ID", example = "1", required = true) @PathVariable Long savingProductId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                savingProductService.getSavingProductDetail(savingProductId)
        ));
    }
}
