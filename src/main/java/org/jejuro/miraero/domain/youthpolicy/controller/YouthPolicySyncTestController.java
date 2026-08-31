package org.jejuro.miraero.domain.youthpolicy.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.youthpolicy.service.YouthPolicySyncService;
import org.jejuro.miraero.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test/youth-policies")
@RequiredArgsConstructor
@Api(tags = "청년 정책 API 저장")
public class YouthPolicySyncTestController {

    private final YouthPolicySyncService youthPolicySyncService;

    @PostMapping("/sync")
    @ApiOperation(value = "청년 정책 수동 동기화", notes = "테스트 환경에서 청년정책 API를 즉시 호출해 정책 데이터를 저장하거나 갱신합니다.")
    public ResponseEntity<ApiResponse<Void>> syncYouthPolicies() {
        youthPolicySyncService.syncYouthPolicies();

        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
