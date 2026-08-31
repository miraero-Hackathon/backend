package org.jejuro.miraero.domain.goal.service;

import org.jejuro.miraero.domain.account.dto.response.AccountResponse;
import org.jejuro.miraero.domain.account.mapper.AccountMapper;
import org.jejuro.miraero.domain.autotransfer.domain.AutoTransfer;
import org.jejuro.miraero.domain.autotransfer.mapper.AutoTransferMapper;
import org.jejuro.miraero.domain.goal.domain.AssetType;
import org.jejuro.miraero.domain.goal.domain.Goal;
import org.jejuro.miraero.domain.goal.domain.GoalAsset;
import org.jejuro.miraero.domain.goal.dto.request.GoalAssetRequest;
import org.jejuro.miraero.domain.goal.dto.response.asset.GoalAssetResponse;
import org.jejuro.miraero.domain.goal.mapper.GoalAssetMapper;
import org.jejuro.miraero.domain.goal.mapper.GoalMapper;
import org.jejuro.miraero.domain.moneybox.domain.MoneyBox;
import org.jejuro.miraero.domain.moneybox.mapper.MoneyBoxMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoalAssetServiceImplTest {

    private static final Long GOAL_ID = 1L;
    private static final Long USER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;

    @Mock
    private GoalAssetMapper goalAssetMapper;

    @Mock
    private GoalMapper goalMapper;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private AutoTransferMapper autoTransferMapper;

    @Mock
    private MoneyBoxMapper moneyBoxMapper;

    @Mock
    private org.jejuro.miraero.domain.mydata.service.AccountTransferService accountTransferService;

    @InjectMocks
    private GoalAssetServiceImpl goalAssetService;


    @Test
    @DisplayName("목표 연결 자산 저장 성공")
    void saveGoalAssets_success() {

        List<GoalAssetRequest> assets = List.of(
                GoalAssetRequest.builder()
                        .assetId(10L)
                        .assetType(AssetType.ACCOUNT)
                        .build()
        );

        given(goalMapper.findByIdAndUserId(USER_ID, GOAL_ID))
                .willReturn(Goal.builder().goalId(GOAL_ID).userId(USER_ID).build());
        when(accountMapper.findResponseByIdAndUserId(10L, USER_ID))
                .thenReturn(AccountResponse.builder().accountId(10L).accountType("SAVINGS").build());

        goalAssetService.saveGoalAssets(
                USER_ID,
                GOAL_ID,
                assets
        );

        verify(goalAssetMapper)
                .saveAll(GOAL_ID, assets);
    }

    @Test
    @DisplayName("연결하려는 계좌가 내 소유가 아니면 예외 발생")
    void saveGoalAssets_accountNotOwned_fail() {

        List<GoalAssetRequest> assets = List.of(
                GoalAssetRequest.builder()
                        .assetId(10L)
                        .assetType(AssetType.ACCOUNT)
                        .build()
        );

        given(goalMapper.findByIdAndUserId(USER_ID, GOAL_ID))
                .willReturn(Goal.builder().goalId(GOAL_ID).userId(USER_ID).build());
        when(accountMapper.findResponseByIdAndUserId(10L, USER_ID))
                .thenReturn(null);

        assertThrows(
                BusinessException.class,
                () -> goalAssetService.saveGoalAssets(USER_ID, GOAL_ID, assets)
        );

        verify(goalAssetMapper, never())
                .saveAll(anyLong(), anyList());
    }

    @Test
    @DisplayName("연결된 자산의 현재 금액을 계산한다")
    void calculateCurrentAmount_success() {

        List<GoalAsset> assets = List.of(
                GoalAsset.builder()
                        .goalId(GOAL_ID)
                        .assetType(AssetType.ACCOUNT)
                        .assetId(10L)
                        .build()
        );

        when(goalAssetMapper.findByGoalId(GOAL_ID))
                .thenReturn(assets);
        when(accountMapper.findResponseById(10L))
                .thenReturn(AccountResponse.builder().accountId(10L).balance(500_000L).build());

        Long result =
                goalAssetService.calculateCurrentAmount(1L,GOAL_ID);

        assertEquals(500_000L, result);
    }

    @Test
    @DisplayName("이미 연결된 자산이면 예외 발생")
    void saveGoalAssets_duplicate_fail() {

        List<GoalAssetRequest> assets = List.of(
                GoalAssetRequest.builder()
                        .assetId(10L)
                        .assetType(AssetType.ACCOUNT)
                        .build()
        );

        given(goalMapper.findByIdAndUserId(USER_ID, GOAL_ID))
                .willReturn(Goal.builder().goalId(GOAL_ID).userId(USER_ID).build());
        when(accountMapper.findResponseByIdAndUserId(10L, USER_ID))
                .thenReturn(AccountResponse.builder().accountId(10L).accountType("SAVINGS").build());
        when(goalAssetMapper.existsByAsset(
                AssetType.ACCOUNT,
                10L
        )).thenReturn(true);

        assertThrows(
                BusinessException.class,
                () -> goalAssetService.saveGoalAssets(USER_ID, GOAL_ID, assets)
        );

        verify(goalAssetMapper, never())
                .saveAll(anyLong(), anyList());
    }

    @Test
    @DisplayName("ACCOUNT 자산 조회 시 계좌 상세정보와 자동이체 정보를 채워서 반환한다")
    void getGoalAssets_account_fillsAccountDetailAndAutoTransfer() {

        List<GoalAsset> assets = List.of(
                GoalAsset.builder()
                        .goalId(GOAL_ID)
                        .assetType(AssetType.ACCOUNT)
                        .assetId(10L)
                        .build()
        );

        given(goalMapper.findById(GOAL_ID))
                .willReturn(Goal.builder().goalId(GOAL_ID).userId(USER_ID).build());
        when(goalAssetMapper.findByGoalId(GOAL_ID))
                .thenReturn(assets);
        when(accountMapper.findResponseById(10L))
                .thenReturn(AccountResponse.builder()
                        .accountId(10L)
                        .accountName("KB 독립적금")
                        .institutionName("국민은행")
                        .maskedAccountNumber("123*****90")
                        .balance(500_000L)
                        .build());
        when(autoTransferMapper.findByAsset(AssetType.ACCOUNT, 10L))
                .thenReturn(AutoTransfer.builder()
                        .withdrawalAccountId(20L)
                        .transferAmount(100_000L)
                        .transferDay(10)
                        .build());
        when(accountMapper.findResponseById(20L))
                .thenReturn(AccountResponse.builder()
                        .accountId(20L)
                        .institutionName("국민은행")
                        .maskedAccountNumber("456*****12")
                        .build());

        var response = goalAssetService.getGoalAssets(USER_ID, GOAL_ID);

        assertEquals(1, response.getAssets().size());
        GoalAssetResponse asset = response.getAssets().get(0);
        assertEquals("KB 독립적금", asset.getAssetName());
        assertEquals("국민은행", asset.getBankName());
        assertEquals(500_000L, asset.getBalance());
        assertEquals(100_000L, asset.getAutoTransfer().getAmount());
        assertEquals(10, asset.getAutoTransfer().getTransferDay());
        assertEquals("456*****12", asset.getAutoTransfer().getWithdrawalAccount().getAccountNumberMasked());
    }

    @Test
    @DisplayName("MONEY_BOX 자산은 자동이체 설정이 없으면 autoTransfer가 null이다")
    void getGoalAssets_moneyBox_noAutoTransfer_isNull() {

        List<GoalAsset> assets = List.of(
                GoalAsset.builder()
                        .goalId(GOAL_ID)
                        .assetType(AssetType.MONEY_BOX)
                        .assetId(30L)
                        .build()
        );

        given(goalMapper.findById(GOAL_ID))
                .willReturn(Goal.builder().goalId(GOAL_ID).userId(USER_ID).build());
        when(goalAssetMapper.findByGoalId(GOAL_ID))
                .thenReturn(assets);
        when(moneyBoxMapper.findById(30L))
                .thenReturn(MoneyBox.builder().moneyBoxId(30L).accountId(1L).balance(500_000L).build());
        when(autoTransferMapper.findByAsset(AssetType.MONEY_BOX, 30L))
                .thenReturn(null);

        var response = goalAssetService.getGoalAssets(USER_ID, GOAL_ID);

        assertEquals(1, response.getAssets().size());
        assertNull(response.getAssets().get(0).getAutoTransfer());
    }

    @Test
    @DisplayName("MONEY_BOX 자산은 잔액과 소속 통장 정보, 자동이체를 함께 채운다")
    void getGoalAssets_moneyBox_withAutoTransfer_fillsBalanceAndOwnerAccount() {

        List<GoalAsset> assets = List.of(
                GoalAsset.builder()
                        .goalId(GOAL_ID)
                        .assetType(AssetType.MONEY_BOX)
                        .assetId(30L)
                        .build()
        );

        given(goalMapper.findById(GOAL_ID))
                .willReturn(Goal.builder().goalId(GOAL_ID).userId(USER_ID).build());
        when(goalAssetMapper.findByGoalId(GOAL_ID))
                .thenReturn(assets);
        when(moneyBoxMapper.findById(30L))
                .thenReturn(MoneyBox.builder().moneyBoxId(30L).accountId(1L).balance(500_000L).build());
        when(autoTransferMapper.findByAsset(AssetType.MONEY_BOX, 30L))
                .thenReturn(AutoTransfer.builder()
                        .withdrawalAccountId(20L)
                        .transferAmount(50_000L)
                        .transferDay(25)
                        .build());
        when(accountMapper.findResponseById(20L))
                .thenReturn(AccountResponse.builder()
                        .accountId(20L)
                        .institutionName("국민은행")
                        .maskedAccountNumber("456*****12")
                        .build());
        when(accountMapper.findResponseById(1L))
                .thenReturn(AccountResponse.builder()
                        .accountId(1L)
                        .institutionName("국민은행")
                        .maskedAccountNumber("123*****90")
                        .build());

        var response = goalAssetService.getGoalAssets(USER_ID, GOAL_ID);

        assertEquals(1, response.getAssets().size());
        GoalAssetResponse asset = response.getAssets().get(0);
        assertEquals(500_000L, asset.getBalance());
        assertEquals("123*****90", asset.getAccountNumberMasked());
        assertEquals(50_000L, asset.getAutoTransfer().getAmount());
        assertEquals(25, asset.getAutoTransfer().getTransferDay());
        assertEquals("456*****12", asset.getAutoTransfer().getWithdrawalAccount().getAccountNumberMasked());
    }

    @Test
    @DisplayName("목표 자체가 없으면 404에 해당하는 예외를 던진다")
    void getGoalAssets_notFound_throws() {

        given(goalMapper.findById(GOAL_ID))
                .willReturn(null);

        assertThrows(
                BusinessException.class,
                () -> goalAssetService.getGoalAssets(USER_ID, GOAL_ID)
        );

        verify(goalAssetMapper, never()).findByGoalId(anyLong());
    }

    @Test
    @DisplayName("목표는 존재하지만 다른 사용자 소유면 403에 해당하는 예외를 던진다")
    void getGoalAssets_notOwned_throws() {

        given(goalMapper.findById(GOAL_ID))
                .willReturn(Goal.builder().goalId(GOAL_ID).userId(OTHER_USER_ID).build());

        assertThrows(
                BusinessException.class,
                () -> goalAssetService.getGoalAssets(USER_ID, GOAL_ID)
        );

        verify(goalAssetMapper, never()).findByGoalId(anyLong());
    }

    @Test
    @DisplayName("목표 자산 연결 해제 성공")
    void deleteGoalAsset_success() {

        Long assetId = 10L;
        AssetType assetType = AssetType.ACCOUNT;

        Goal goal = Goal.builder()
                .goalId(GOAL_ID)
                .userId(USER_ID)
                .build();

        given(goalMapper.findByIdAndUserId(
                USER_ID,
                GOAL_ID
        ))
                .willReturn(goal);

        given(goalAssetMapper.existsByGoalIdAndAsset(
                GOAL_ID,
                assetType,
                assetId
        ))
                .willReturn(true);

        goalAssetService.deleteGoalAsset(
                USER_ID,
                GOAL_ID,
                assetType,
                assetId
        );

        verify(goalAssetMapper)
                .delete(
                        GOAL_ID,
                        assetType,
                        assetId
                );
    }

    @Test
    @DisplayName("입출금통장은 목표 자산으로 연결할 수 없다")
    void saveGoalAssets_checkingAccount_fail() {

        List<GoalAssetRequest> assets = List.of(
                GoalAssetRequest.builder()
                        .assetId(10L)
                        .assetType(AssetType.ACCOUNT)
                        .build()
        );

        given(goalMapper.findByIdAndUserId(USER_ID, GOAL_ID))
                .willReturn(Goal.builder().goalId(GOAL_ID).userId(USER_ID).build());
        when(accountMapper.findResponseByIdAndUserId(10L, USER_ID))
                .thenReturn(AccountResponse.builder().accountId(10L).accountType("CHECKING").build());

        assertThrows(
                BusinessException.class,
                () -> goalAssetService.saveGoalAssets(USER_ID, GOAL_ID, assets)
        );

        verify(goalAssetMapper, never()).saveAll(anyLong(), anyList());
    }

    @Test
    @DisplayName("내 저금통이 아니면 목표에 연결할 수 없다")
    void saveGoalAssets_moneyBoxNotOwned_fail() {

        List<GoalAssetRequest> assets = List.of(
                GoalAssetRequest.builder()
                        .assetId(30L)
                        .assetType(AssetType.MONEY_BOX)
                        .build()
        );

        given(goalMapper.findByIdAndUserId(USER_ID, GOAL_ID))
                .willReturn(Goal.builder().goalId(GOAL_ID).userId(USER_ID).build());
        when(moneyBoxMapper.existsByIdAndUserId(30L, USER_ID))
                .thenReturn(false);

        assertThrows(
                BusinessException.class,
                () -> goalAssetService.saveGoalAssets(USER_ID, GOAL_ID, assets)
        );

        verify(goalAssetMapper, never()).saveAll(anyLong(), anyList());
    }

    @Test
    @DisplayName("진행률은 연결된 저금통 잔액을 합산한다")
    void calculateCurrentAmount_includesMoneyBoxBalance() {

        when(goalAssetMapper.findByGoalId(GOAL_ID))
                .thenReturn(List.of(
                        GoalAsset.builder().goalId(GOAL_ID).assetType(AssetType.MONEY_BOX).assetId(30L).build()
                ));
        when(moneyBoxMapper.findBalanceById(30L))
                .thenReturn(700_000L);

        assertEquals(700_000L, goalAssetService.calculateCurrentAmount(1L,GOAL_ID));
    }

    @Test
    @DisplayName("시작 금액을 연결된 저금통 잔액에 반영한다")
    void applyStartAmount_depositsIntoMoneyBox() {

        when(goalAssetMapper.findByGoalId(GOAL_ID))
                .thenReturn(List.of(
                        GoalAsset.builder().goalId(GOAL_ID).assetType(AssetType.MONEY_BOX).assetId(30L).build()
                ));

        goalAssetService.applyStartAmount(GOAL_ID, 3_000_000L);

        verify(moneyBoxMapper).increaseBalance(30L, 3_000_000L);
    }

    @Test
    @DisplayName("시작 금액이 0이면 저금통을 건드리지 않는다")
    void applyStartAmount_zero_doesNothing() {

        goalAssetService.applyStartAmount(GOAL_ID, 0L);

        verify(moneyBoxMapper, never()).increaseBalance(anyLong(), anyLong());
    }

    @Test
    @DisplayName("목표 삭제 시 연결된 저금통과 자동이체를 함께 정리한다")
    void releaseGoalAssets_deletesMoneyBoxAndAutoTransfer() {

        when(goalAssetMapper.findByGoalId(GOAL_ID))
                .thenReturn(List.of(
                        GoalAsset.builder().goalId(GOAL_ID).assetType(AssetType.MONEY_BOX).assetId(30L).build(),
                        GoalAsset.builder().goalId(GOAL_ID).assetType(AssetType.ACCOUNT).assetId(10L).build()
                ));

        goalAssetService.releaseGoalAssets(GOAL_ID);

        verify(autoTransferMapper).deleteByMoneyBoxId(30L);
        verify(moneyBoxMapper).deleteById(30L);
        // 연결된 예적금 계좌는 사용자 자산이므로 삭제 대상이 아니다
        verify(moneyBoxMapper, never()).deleteById(10L);
    }

    @Test
    @DisplayName("저금통이 연결된 목표는 그 저금통이 속한 계좌로 끌어쓴다")
    void pullFunds_toMoneyBox() {
        Long sourceAccountId = 50L;
        MoneyBox moneyBox = MoneyBox.builder().moneyBoxId(30L).accountId(60L).build();
        org.jejuro.miraero.domain.goal.dto.request.GoalPullFundsRequest request =
                pullRequest(sourceAccountId, 100_000L);

        given(goalMapper.findByIdAndUserId(USER_ID, GOAL_ID))
                .willReturn(Goal.builder().goalId(GOAL_ID).userId(USER_ID).build());
        given(accountMapper.findResponseByIdAndUserId(sourceAccountId, USER_ID))
                .willReturn(AccountResponse.builder().accountId(sourceAccountId).balance(500_000L).build());
        given(goalAssetMapper.existsByAsset(AssetType.ACCOUNT, sourceAccountId)).willReturn(false);
        given(goalAssetMapper.findByGoalId(GOAL_ID))
                .willReturn(List.of(
                        GoalAsset.builder().goalId(GOAL_ID).assetType(AssetType.MONEY_BOX).assetId(30L).build()));
        given(moneyBoxMapper.findById(30L)).willReturn(moneyBox);
        given(moneyBoxMapper.findBalanceById(30L)).willReturn(200_000L);

        var response = goalAssetService.pullFunds(USER_ID, GOAL_ID, request);

        assertEquals(100_000L, response.getPulledAmount());
        verify(accountMapper).decreaseBalance(sourceAccountId, USER_ID, 100_000L);
        verify(accountTransferService).transfer(USER_ID, sourceAccountId, 60L, 100_000L);
        verify(moneyBoxMapper).increaseBalance(30L, 100_000L);
    }

    @Test
    @DisplayName("목표 저금통이 있는 계좌를 출처로 지정하면 끌어쓸 수 없다")
    void pullFunds_sameAccount_throws() {
        Long sharedAccountId = 60L;
        org.jejuro.miraero.domain.goal.dto.request.GoalPullFundsRequest request =
                pullRequest(sharedAccountId, 100_000L);

        given(goalMapper.findByIdAndUserId(USER_ID, GOAL_ID))
                .willReturn(Goal.builder().goalId(GOAL_ID).userId(USER_ID).build());
        given(accountMapper.findResponseByIdAndUserId(sharedAccountId, USER_ID))
                .willReturn(AccountResponse.builder().accountId(sharedAccountId).balance(500_000L).build());
        given(goalAssetMapper.existsByAsset(AssetType.ACCOUNT, sharedAccountId)).willReturn(false);
        given(goalAssetMapper.findByGoalId(GOAL_ID))
                .willReturn(List.of(
                        GoalAsset.builder().goalId(GOAL_ID).assetType(AssetType.MONEY_BOX).assetId(30L).build()));
        // 목표 저금통이 출처와 같은 계좌에 있는 상황
        given(moneyBoxMapper.findById(30L))
                .willReturn(MoneyBox.builder().moneyBoxId(30L).accountId(sharedAccountId).build());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> goalAssetService.pullFunds(USER_ID, GOAL_ID, request));

        assertEquals(org.jejuro.miraero.domain.goal.exception.GoalErrorCode.PULL_SAME_ACCOUNT,
                exception.getErrorCode());
        verify(accountMapper, never()).decreaseBalance(anyLong(), anyLong(), anyLong());
        verify(accountTransferService, never()).transfer(anyLong(), anyLong(), anyLong(), anyLong());
        verify(moneyBoxMapper, never()).increaseBalance(anyLong(), anyLong());
    }

    @Test
    @DisplayName("예적금이 연결된 목표는 그 계좌로 바로 끌어쓴다")
    void pullFunds_toAccount() {
        Long sourceAccountId = 50L;
        Long targetAccountId = 70L;
        org.jejuro.miraero.domain.goal.dto.request.GoalPullFundsRequest request =
                pullRequest(sourceAccountId, 100_000L);

        given(goalMapper.findByIdAndUserId(USER_ID, GOAL_ID))
                .willReturn(Goal.builder().goalId(GOAL_ID).userId(USER_ID).build());
        given(accountMapper.findResponseByIdAndUserId(sourceAccountId, USER_ID))
                .willReturn(AccountResponse.builder().accountId(sourceAccountId).balance(500_000L).build());
        given(goalAssetMapper.existsByAsset(AssetType.ACCOUNT, sourceAccountId)).willReturn(false);
        given(goalAssetMapper.findByGoalId(GOAL_ID))
                .willReturn(List.of(
                        GoalAsset.builder().goalId(GOAL_ID).assetType(AssetType.ACCOUNT).assetId(targetAccountId).build()));
        given(accountMapper.findResponseById(targetAccountId))
                .willReturn(AccountResponse.builder().accountId(targetAccountId).balance(1_000_000L).build());

        goalAssetService.pullFunds(USER_ID, GOAL_ID, request);

        verify(accountTransferService).transfer(USER_ID, sourceAccountId, targetAccountId, 100_000L);
        verify(accountMapper).increaseBalance(targetAccountId, USER_ID, 100_000L);
    }

    @Test
    @DisplayName("이미 다른 목표에 연결된 계좌는 끌어쓰기 출처로 쓸 수 없다")
    void pullFunds_sourceAlreadyLinked_throws() {
        Long sourceAccountId = 50L;
        org.jejuro.miraero.domain.goal.dto.request.GoalPullFundsRequest request =
                pullRequest(sourceAccountId, 100_000L);

        given(goalMapper.findByIdAndUserId(USER_ID, GOAL_ID))
                .willReturn(Goal.builder().goalId(GOAL_ID).userId(USER_ID).build());
        given(accountMapper.findResponseByIdAndUserId(sourceAccountId, USER_ID))
                .willReturn(AccountResponse.builder().accountId(sourceAccountId).balance(500_000L).build());
        given(goalAssetMapper.existsByAsset(AssetType.ACCOUNT, sourceAccountId)).willReturn(true);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> goalAssetService.pullFunds(USER_ID, GOAL_ID, request));

        assertEquals(org.jejuro.miraero.domain.goal.exception.GoalErrorCode.PULL_SOURCE_ACCOUNT_LINKED,
                exception.getErrorCode());
        verify(accountTransferService, never()).transfer(anyLong(), anyLong(), anyLong(), anyLong());
    }

    @Test
    @DisplayName("출처 계좌 잔액이 부족하면 끌어쓸 수 없다")
    void pullFunds_insufficientBalance_throws() {
        Long sourceAccountId = 50L;
        org.jejuro.miraero.domain.goal.dto.request.GoalPullFundsRequest request =
                pullRequest(sourceAccountId, 100_000L);

        given(goalMapper.findByIdAndUserId(USER_ID, GOAL_ID))
                .willReturn(Goal.builder().goalId(GOAL_ID).userId(USER_ID).build());
        given(accountMapper.findResponseByIdAndUserId(sourceAccountId, USER_ID))
                .willReturn(AccountResponse.builder().accountId(sourceAccountId).balance(50_000L).build());
        given(goalAssetMapper.existsByAsset(AssetType.ACCOUNT, sourceAccountId)).willReturn(false);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> goalAssetService.pullFunds(USER_ID, GOAL_ID, request));

        assertEquals(org.jejuro.miraero.domain.goal.exception.GoalErrorCode.PULL_INSUFFICIENT_BALANCE,
                exception.getErrorCode());
        verify(accountTransferService, never()).transfer(anyLong(), anyLong(), anyLong(), anyLong());
    }

    private org.jejuro.miraero.domain.goal.dto.request.GoalPullFundsRequest pullRequest(
            Long sourceAccountId, Long amount) {
        org.jejuro.miraero.domain.goal.dto.request.GoalPullFundsRequest request =
                new org.jejuro.miraero.domain.goal.dto.request.GoalPullFundsRequest();
        org.springframework.test.util.ReflectionTestUtils.setField(request, "sourceAccountId", sourceAccountId);
        org.springframework.test.util.ReflectionTestUtils.setField(request, "amount", amount);
        return request;
    }
}
