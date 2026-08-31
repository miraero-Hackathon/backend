package org.jejuro.miraero.domain.product.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.product.service.ProductSyncService;
import org.jejuro.miraero.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test/products")
@RequiredArgsConstructor
@Api(tags = "금융상품 API 테스트")
public class ProductSyncTestController {

    private final ProductSyncService productSyncService;

    @PostMapping("/sync")
    @ApiOperation(
            value = "예적금 상품 수동 동기화",
            notes = "테스트 환경에서 금융감독원 API를 즉시 호출해 예금·적금 상품과 옵션 데이터를 저장하거나 갱신합니다."
    )
    public ResponseEntity<ApiResponse<Void>> syncProducts() {
        productSyncService.syncAllProducts();

        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
