package org.jejuro.miraero.domain.moneybox.controller;

import lombok.RequiredArgsConstructor;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.jejuro.miraero.domain.moneybox.dto.request.MoneyBoxCreateRequest;
import org.jejuro.miraero.domain.moneybox.dto.response.MoneyBoxCreateResponse;
import org.jejuro.miraero.domain.moneybox.service.MoneyBoxService;
import org.jejuro.miraero.global.response.ApiResponse;
import org.jejuro.miraero.global.security.AuthenticatedUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/money-boxes")
@Api(tags = "머니박스")
public class MoneyBoxController {

    private final MoneyBoxService moneyBoxService;

    @PostMapping
    @ApiOperation(value = "머니박스 생성", notes = "로그인 사용자의 머니박스를 생성합니다.")
    public ResponseEntity<ApiResponse<MoneyBoxCreateResponse>> createMoneyBox(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody MoneyBoxCreateRequest request
            ){

        Long userId = user.getUserId();

        MoneyBoxCreateResponse response =
                moneyBoxService.createMoneyBox(userId,request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
