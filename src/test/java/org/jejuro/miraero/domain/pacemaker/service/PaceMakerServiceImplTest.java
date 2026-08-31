package org.jejuro.miraero.domain.pacemaker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.jejuro.miraero.domain.account.mapper.AccountMapper;
import org.jejuro.miraero.domain.moneybox.domain.MoneyBox;
import org.jejuro.miraero.domain.moneybox.domain.MoneyBoxType;
import org.jejuro.miraero.domain.moneybox.mapper.MoneyBoxMapper;
import org.jejuro.miraero.domain.moneybox.service.MoneyBoxService;
import org.jejuro.miraero.domain.mydata.service.AccountTransferService;
import org.jejuro.miraero.domain.pacemaker.domain.AutoSaving;
import org.jejuro.miraero.domain.pacemaker.dto.request.PaceMakerCreateRequest;
import org.jejuro.miraero.domain.goal.domain.AssetType;
import org.jejuro.miraero.domain.pacemaker.dto.request.PaceMakerGoalDepositRequest;
import org.jejuro.miraero.domain.pacemaker.dto.request.PaceMakerHistorySearchCondition;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerCreateResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerDashboardResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerDashboardSummaryResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerGoalDepositAssetRowResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerGoalDepositResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerGoalListResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerGoalSummaryResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerGoalWithdrawalAccountRowResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerHistoryResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerMaxAmountUpdateResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerResponse;
import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerWeeklyStreakResponse;
import org.jejuro.miraero.domain.moneybox.exception.MoneyBoxErrorCode;
import org.jejuro.miraero.domain.pacemaker.exception.PaceMakerErrorCode;
import org.jejuro.miraero.domain.pacemaker.mapper.PaceMakerMapper;
import org.jejuro.miraero.domain.transaction.service.TransactionQueryService;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.jejuro.miraero.global.response.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PaceMakerServiceImplTest {

    private static final Long USER_ID = 10L;

    @Mock
    private PaceMakerMapper paceMakerMapper;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private MoneyBoxMapper moneyBoxMapper;

    @Mock
    private MoneyBoxService moneyBoxService;

    @Mock
    private TransactionQueryService transactionQueryService;

    @Mock
    private AccountTransferService accountTransferService;

    private PaceMakerService paceMakerService;

    @BeforeEach
    void setUp() {
        paceMakerService = new PaceMakerServiceImpl(
                paceMakerMapper,
                moneyBoxMapper,
                accountMapper,
                moneyBoxService,
                transactionQueryService,
                accountTransferService
        );
    }

    @Test
    @DisplayName("페이스메이커 개설 시 저금통과 자동 적립 설정을 함께 만든다")
    void createPaceMaker_createsMoneyBoxAndAutoSaving() {
        when(paceMakerMapper.findByUserId(USER_ID))
                .thenReturn(null)
                .thenReturn(createAutoSaving(21L, "ACTIVE"));
        when(moneyBoxService.createMoneyBox(USER_ID, 10L, MoneyBoxType.SAVING)).thenReturn(55L);

        PaceMakerCreateResponse response =
                paceMakerService.createPaceMaker(USER_ID, createRequest(10L, 30_000L));

        assertEquals(21L, response.getAutoSavingId());
        verify(moneyBoxService).validateAutoWithdrawalAccount(USER_ID, 10L);
        verify(paceMakerMapper).insertAutoSaving(USER_ID, 55L, 10L, 30_000L);
    }

    @Test
    @DisplayName("이미 페이스메이커가 있으면 개설할 수 없다")
    void createPaceMaker_alreadyRegistered() {
        when(paceMakerMapper.findByUserId(USER_ID)).thenReturn(createAutoSaving(21L, "ACTIVE"));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> paceMakerService.createPaceMaker(USER_ID, createRequest(10L, null)));

        assertEquals(PaceMakerErrorCode.ALREADY_REGISTERED, exception.getErrorCode());
        verify(paceMakerMapper, never()).insertAutoSaving(anyLong(), anyLong(), anyLong(), any());
    }

    @Test
    @DisplayName("급여 통장이 아니면 저금통을 만들지 않는다")
    void createPaceMaker_notSalaryAccount() {
        when(paceMakerMapper.findByUserId(USER_ID)).thenReturn(null);
        org.mockito.Mockito.doThrow(new BusinessException(MoneyBoxErrorCode.AUTO_TRANSFER_NOT_SALARY_ACCOUNT))
                .when(moneyBoxService).validateAutoWithdrawalAccount(USER_ID, 10L);

        assertThrows(BusinessException.class,
                () -> paceMakerService.createPaceMaker(USER_ID, createRequest(10L, null)));

        verify(moneyBoxService, never()).createMoneyBox(anyLong(), anyLong(), any());
        verify(paceMakerMapper, never()).insertAutoSaving(anyLong(), anyLong(), anyLong(), any());
    }

    @Test
    @DisplayName("계좌를 생략하면 급여 통장에 개설한다")
    void createPaceMaker_noAccountId_usesSalaryAccount() {
        when(paceMakerMapper.findByUserId(USER_ID))
                .thenReturn(null)
                .thenReturn(createAutoSaving(21L, "ACTIVE"));
        when(transactionQueryService.getSalaryAccountId(USER_ID)).thenReturn(77L);
        when(moneyBoxService.createMoneyBox(USER_ID, 77L, MoneyBoxType.SAVING)).thenReturn(55L);

        paceMakerService.createPaceMaker(USER_ID, createRequest(null, 30_000L));

        verify(moneyBoxService).validateAutoWithdrawalAccount(USER_ID, 77L);
        verify(paceMakerMapper).insertAutoSaving(USER_ID, 55L, 77L, 30_000L);
    }

    @Test
    @DisplayName("계좌를 생략했는데 급여 통장을 찾을 수 없으면 개설할 수 없다")
    void createPaceMaker_noSalaryAccount() {
        when(paceMakerMapper.findByUserId(USER_ID)).thenReturn(null);
        when(transactionQueryService.getSalaryAccountId(USER_ID)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> paceMakerService.createPaceMaker(USER_ID, createRequest(null, null)));

        assertEquals(PaceMakerErrorCode.SALARY_ACCOUNT_NOT_FOUND, exception.getErrorCode());
        verify(paceMakerMapper, never()).insertAutoSaving(anyLong(), anyLong(), anyLong(), any());
    }

    private PaceMakerCreateRequest createRequest(Long accountId, Long maxAmount) {
        PaceMakerCreateRequest request = new PaceMakerCreateRequest();
        ReflectionTestUtils.setField(request, "accountId", accountId);
        ReflectionTestUtils.setField(request, "maxAmount", maxAmount);
        return request;
    }

    @Test
    @DisplayName("Get active pace maker")
    void getPaceMaker_active() {
        AutoSaving autoSaving = createAutoSaving(21L, "ACTIVE");
        when(paceMakerMapper.findByUserId(USER_ID)).thenReturn(autoSaving);

        PaceMakerResponse response = paceMakerService.getPaceMaker(USER_ID);

        assertEquals(21L, response.getAutoSavingId());
        assertEquals(3L, response.getMoneyBoxId());
        assertTrue(response.isRegistered());
        assertEquals("ACTIVE", response.getStatus());
        assertTrue(response.isEnabled());
        verify(paceMakerMapper).findByUserId(USER_ID);
    }

    @Test
    @DisplayName("Get paused pace maker")
    void getPaceMaker_paused() {
        AutoSaving autoSaving = createAutoSaving(22L, "PAUSED");
        when(paceMakerMapper.findByUserId(USER_ID)).thenReturn(autoSaving);

        PaceMakerResponse response = paceMakerService.getPaceMaker(USER_ID);

        assertEquals(22L, response.getAutoSavingId());
        assertTrue(response.isRegistered());
        assertEquals("PAUSED", response.getStatus());
        assertFalse(response.isEnabled());
        verify(paceMakerMapper).findByUserId(USER_ID);
    }

    @Test
    @DisplayName("Get not created pace maker")
    void getPaceMaker_notCreated() {
        when(paceMakerMapper.findByUserId(USER_ID)).thenReturn(null);

        PaceMakerResponse response = paceMakerService.getPaceMaker(USER_ID);

        assertNull(response.getAutoSavingId());
        assertNull(response.getMoneyBoxId());
        assertFalse(response.isRegistered());
        assertNull(response.getStatus());
        assertFalse(response.isEnabled());
        verify(paceMakerMapper).findByUserId(USER_ID);
    }

    @Test
    @DisplayName("Update status")
    void updateStatus_success() {
        Long autoSavingId = 21L;
        AutoSaving autoSaving = createAutoSaving(autoSavingId, "PAUSED");
        when(paceMakerMapper.updateStatus(USER_ID, autoSavingId, "PAUSED")).thenReturn(1);
        when(paceMakerMapper.findByUserId(USER_ID)).thenReturn(autoSaving);

        PaceMakerResponse response = paceMakerService.updateStatus(USER_ID, autoSavingId, "PAUSED");

        assertEquals(autoSavingId, response.getAutoSavingId());
        assertTrue(response.isRegistered());
        assertEquals("PAUSED", response.getStatus());
        assertFalse(response.isEnabled());
        verify(paceMakerMapper).updateStatus(USER_ID, autoSavingId, "PAUSED");
        verify(paceMakerMapper).findByUserId(USER_ID);
    }

    @Test
    @DisplayName("Update status not found")
    void updateStatus_notFound() {
        Long autoSavingId = 99L;
        when(paceMakerMapper.updateStatus(USER_ID, autoSavingId, "ACTIVE")).thenReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> paceMakerService.updateStatus(USER_ID, autoSavingId, "ACTIVE")
        );

        assertEquals(CommonErrorCode.RESOURCE_NOT_FOUND, exception.getErrorCode());
        verify(paceMakerMapper).updateStatus(USER_ID, autoSavingId, "ACTIVE");
        verify(paceMakerMapper, never()).findByUserId(USER_ID);
    }

    @Test
    @DisplayName("Update max amount")
    void updateMaxAmount_success() {
        Long autoSavingId = 21L;
        Long maxAmount = 500_000L;
        when(paceMakerMapper.updateMaxAmount(USER_ID, autoSavingId, maxAmount)).thenReturn(1);

        PaceMakerMaxAmountUpdateResponse response =
                paceMakerService.updateMaxAmount(USER_ID, autoSavingId, maxAmount);

        assertEquals(autoSavingId, response.getAutoSavingId());
        assertEquals(maxAmount, response.getMaxAmount());
        verify(paceMakerMapper).updateMaxAmount(USER_ID, autoSavingId, maxAmount);
    }

    @Test
    @DisplayName("Update max amount not found")
    void updateMaxAmount_notFound() {
        Long autoSavingId = 99L;
        Long maxAmount = 500_000L;
        when(paceMakerMapper.updateMaxAmount(USER_ID, autoSavingId, maxAmount)).thenReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> paceMakerService.updateMaxAmount(USER_ID, autoSavingId, maxAmount)
        );

        assertEquals(CommonErrorCode.RESOURCE_NOT_FOUND, exception.getErrorCode());
        verify(paceMakerMapper).updateMaxAmount(USER_ID, autoSavingId, maxAmount);
    }

    @Test
    @DisplayName("Get dashboard without streak")
    void getDashboard_withoutStreak() {
        PaceMakerDashboardSummaryResponse summary = createDashboardSummary();
        when(paceMakerMapper.findDashboardByUserId(USER_ID)).thenReturn(summary);

        PaceMakerDashboardResponse response = paceMakerService.getDashboard(USER_ID, false);

        assertEquals(21L, response.getAutoSavingId());
        assertEquals("ACTIVE", response.getStatus());
        assertEquals(300_000L, response.getMaxAmount());
        assertEquals(3L, response.getMoneyBox().getMoneyBoxId());
        assertEquals(120_000L, response.getMoneyBox().getBalance());
        assertEquals("123-***-789", response.getMoneyBox().getMaskedAccountNumber());
        assertTrue(response.getTodaySaving().isSaved());
        assertEquals(5_000L, response.getTodaySaving().getAmount());
        assertNull(response.getCurrentStreak());
        assertNull(response.getWeeklyStreak());
        assertNull(response.getMonthlySuccessCount());
        verify(paceMakerMapper).findDashboardByUserId(USER_ID);
        verify(paceMakerMapper, never()).findRecentSavingDates(anyLong());
        verify(paceMakerMapper, never()).findWeeklyStreak(anyLong());
        verify(paceMakerMapper, never()).countMonthlySuccess(anyLong());
    }

    @Test
    @DisplayName("Get dashboard with streak")
    void getDashboard_withStreak() {
        PaceMakerDashboardSummaryResponse summary = createDashboardSummary();
        List<LocalDate> savingDates = List.of(
                LocalDate.now(),
                LocalDate.now().minusDays(1),
                LocalDate.now().minusDays(2)
        );
        List<PaceMakerWeeklyStreakResponse> weeklyStreak = List.of(
                PaceMakerWeeklyStreakResponse.builder()
                        .dayOfWeek("MONDAY")
                        .saved(true)
                        .amount(5_000L)
                        .build()
        );
        when(paceMakerMapper.findDashboardByUserId(USER_ID)).thenReturn(summary);
        when(paceMakerMapper.findRecentSavingDates(21L)).thenReturn(savingDates);
        when(paceMakerMapper.findWeeklyStreak(21L)).thenReturn(weeklyStreak);
        when(paceMakerMapper.countMonthlySuccess(21L)).thenReturn(18);

        PaceMakerDashboardResponse response = paceMakerService.getDashboard(USER_ID, true);

        assertEquals(3, response.getCurrentStreak());
        assertEquals(weeklyStreak, response.getWeeklyStreak());
        assertEquals(18, response.getMonthlySuccessCount());
        verify(paceMakerMapper).findDashboardByUserId(USER_ID);
        verify(paceMakerMapper).findRecentSavingDates(21L);
        verify(paceMakerMapper).findWeeklyStreak(21L);
        verify(paceMakerMapper).countMonthlySuccess(21L);
    }

    @Test
    @DisplayName("Get dashboard not found")
    void getDashboard_notFound() {
        when(paceMakerMapper.findDashboardByUserId(USER_ID)).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> paceMakerService.getDashboard(USER_ID, false)
        );

        assertEquals(CommonErrorCode.RESOURCE_NOT_FOUND, exception.getErrorCode());
        verify(paceMakerMapper).findDashboardByUserId(USER_ID);
        verify(paceMakerMapper, never()).findRecentSavingDates(anyLong());
        verify(paceMakerMapper, never()).findWeeklyStreak(anyLong());
        verify(paceMakerMapper, never()).countMonthlySuccess(anyLong());
    }

    @Test
    @DisplayName("조회할 월을 지정하면 그달 이력을 조회한다")
    void getHistories_withYearMonth() {
        PaceMakerHistorySearchCondition condition = new PaceMakerHistorySearchCondition();
        condition.setYearMonth("2026-06");
        LocalDateTime startDateTime = LocalDate.of(2026, 6, 1).atStartOfDay();
        LocalDateTime endDateTime = startDateTime.plusMonths(1);
        when(paceMakerMapper.findHistories(USER_ID, startDateTime, endDateTime, 0L, 10))
                .thenReturn(List.of());
        when(paceMakerMapper.countHistories(USER_ID, startDateTime, endDateTime)).thenReturn(0L);

        paceMakerService.getHistories(USER_ID, condition);

        verify(paceMakerMapper).findHistories(USER_ID, startDateTime, endDateTime, 0L, 10);
    }

    @Test
    @DisplayName("월 형식이 잘못되면 조회할 수 없다")
    void getHistories_invalidYearMonth() {
        PaceMakerHistorySearchCondition condition = new PaceMakerHistorySearchCondition();
        condition.setYearMonth("2026/06");

        assertThrows(BusinessException.class,
                () -> paceMakerService.getHistories(USER_ID, condition));
    }

    @Test
    @DisplayName("Get histories")
    void getHistories_success() {
        PaceMakerHistorySearchCondition condition = new PaceMakerHistorySearchCondition();
        condition.setPage(1);
        condition.setSize(2);
        LocalDateTime startDateTime = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endDateTime = startDateTime.plusMonths(1);
        List<PaceMakerHistoryResponse> histories = List.of(
                PaceMakerHistoryResponse.builder()
                        .date("2026-08-05")
                        .status("SAVED")
                        .amount(10_000L)
                        .description(null)
                        .build(),
                PaceMakerHistoryResponse.builder()
                        .date("2026-08-03")
                        .status("SAVED")
                        .amount(5_000L)
                        .description(null)
                        .build()
        );
        when(paceMakerMapper.findHistories(USER_ID, startDateTime, endDateTime, 2L, 2))
                .thenReturn(histories);
        when(paceMakerMapper.countHistories(USER_ID, startDateTime, endDateTime))
                .thenReturn(5L);

        PageResponse<PaceMakerHistoryResponse> response =
                paceMakerService.getHistories(USER_ID, condition);

        assertEquals(histories, response.getContent());
        assertEquals(1, response.getPage());
        assertEquals(2, response.getSize());
        assertEquals(5L, response.getTotalElements());
        assertEquals(3, response.getTotalPages());
        assertFalse(response.isFirst());
        assertFalse(response.isLast());
        verify(paceMakerMapper).findHistories(USER_ID, startDateTime, endDateTime, 2L, 2);
        verify(paceMakerMapper).countHistories(USER_ID, startDateTime, endDateTime);
    }

    @Test
    @DisplayName("Get histories empty")
    void getHistories_empty() {
        PaceMakerHistorySearchCondition condition = new PaceMakerHistorySearchCondition();
        LocalDateTime startDateTime = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endDateTime = startDateTime.plusMonths(1);
        when(paceMakerMapper.findHistories(USER_ID, startDateTime, endDateTime, 0L, 10))
                .thenReturn(List.of());
        when(paceMakerMapper.countHistories(USER_ID, startDateTime, endDateTime))
                .thenReturn(0L);

        PageResponse<PaceMakerHistoryResponse> response =
                paceMakerService.getHistories(USER_ID, condition);

        assertTrue(response.getContent().isEmpty());
        assertEquals(0, response.getPage());
        assertEquals(10, response.getSize());
        assertEquals(0L, response.getTotalElements());
        assertEquals(0, response.getTotalPages());
        assertTrue(response.isFirst());
        assertTrue(response.isLast());
        verify(paceMakerMapper).findHistories(USER_ID, startDateTime, endDateTime, 0L, 10);
        verify(paceMakerMapper).countHistories(USER_ID, startDateTime, endDateTime);
    }

    @Test
    @DisplayName("Get histories invalid condition")
    void getHistories_invalidCondition() {
        PaceMakerHistorySearchCondition condition = new PaceMakerHistorySearchCondition();
        condition.setPage(-1);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> paceMakerService.getHistories(USER_ID, condition)
        );

        assertEquals(CommonErrorCode.INVALID_INPUT_VALUE, exception.getErrorCode());
        verify(paceMakerMapper, never()).findHistories(anyLong(), any(), any(), any(), any());
        verify(paceMakerMapper, never()).countHistories(anyLong(), any(), any());
    }

    @Test
    @DisplayName("Get pace maker goals")
    void getPaceMakerGoals_success() {
        when(paceMakerMapper.findByUserId(USER_ID)).thenReturn(createAutoSaving(21L, "ACTIVE"));
        when(paceMakerMapper.findPaceMakerGoals(USER_ID)).thenReturn(List.of(
                createGoalSummary(1L, "Travel", "TRAVEL", 3_000_000L),
                createGoalSummary(2L, "Emergency", "EMERGENCY", 1_000_000L)
        ));
        when(paceMakerMapper.findPaceMakerGoalDepositAssets(USER_ID)).thenReturn(List.of(
                createDepositAssetRow(1L, "ACCOUNT", 3L, "KB", "123-***-789", 700_000L),
                createDepositAssetRow(1L, "ACCOUNT", 4L, "Shinhan", "456-***-111", 500_000L),
                createDepositAssetRow(2L, "MONEY_BOX", 5L, null, "789-***-222", 100_000L)
        ));
        when(paceMakerMapper.findPaceMakerGoalWithdrawalAccounts(USER_ID)).thenReturn(List.of(
                createWithdrawalAccountRow(1L, 8L, "KB", "987-***-123", 1_000_000L),
                createWithdrawalAccountRow(2L, 9L, "Hana", "555-***-777", 300_000L)
        ));

        PaceMakerGoalListResponse response = paceMakerService.getPaceMakerGoals(USER_ID);

        assertEquals(2, response.getGoals().size());
        assertEquals(1L, response.getGoals().get(0).getGoalId());
        assertEquals("Travel", response.getGoals().get(0).getGoalName());
        assertEquals("TRAVEL", response.getGoals().get(0).getGoalType());
        assertEquals(3_000_000L, response.getGoals().get(0).getGoalAmount());
        assertEquals(1_200_000L, response.getGoals().get(0).getTotalSavedAmount());
        assertEquals(2, response.getGoals().get(0).getDepositAssets().size());
        assertEquals("KB", response.getGoals().get(0).getDepositAssets().get(0).getFinancialInstitutionName());
        assertEquals(1, response.getGoals().get(0).getWithdrawalAccounts().size());
        assertEquals(8L, response.getGoals().get(0).getWithdrawalAccounts().get(0).getAccountId());
        assertEquals(100_000L, response.getGoals().get(1).getTotalSavedAmount());
        assertEquals("MONEY_BOX", response.getGoals().get(1).getDepositAssets().get(0).getAssetType());
        verify(paceMakerMapper).findByUserId(USER_ID);
        verify(paceMakerMapper).findPaceMakerGoals(USER_ID);
        verify(paceMakerMapper).findPaceMakerGoalDepositAssets(USER_ID);
        verify(paceMakerMapper).findPaceMakerGoalWithdrawalAccounts(USER_ID);
    }

    @Test
    @DisplayName("Get pace maker goals empty")
    void getPaceMakerGoals_empty() {
        when(paceMakerMapper.findByUserId(USER_ID)).thenReturn(createAutoSaving(21L, "ACTIVE"));
        when(paceMakerMapper.findPaceMakerGoals(USER_ID)).thenReturn(List.of());
        when(paceMakerMapper.findPaceMakerGoalDepositAssets(USER_ID)).thenReturn(List.of());
        when(paceMakerMapper.findPaceMakerGoalWithdrawalAccounts(USER_ID)).thenReturn(List.of());

        PaceMakerGoalListResponse response = paceMakerService.getPaceMakerGoals(USER_ID);

        assertTrue(response.getGoals().isEmpty());
        verify(paceMakerMapper).findByUserId(USER_ID);
        verify(paceMakerMapper).findPaceMakerGoals(USER_ID);
        verify(paceMakerMapper).findPaceMakerGoalDepositAssets(USER_ID);
        verify(paceMakerMapper).findPaceMakerGoalWithdrawalAccounts(USER_ID);
    }

    @Test
    @DisplayName("Get pace maker goals not registered")
    void getPaceMakerGoals_notRegistered() {
        when(paceMakerMapper.findByUserId(USER_ID)).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> paceMakerService.getPaceMakerGoals(USER_ID)
        );

        assertEquals(PaceMakerErrorCode.NOT_REGISTERED, exception.getErrorCode());
        verify(paceMakerMapper).findByUserId(USER_ID);
        verify(paceMakerMapper, never()).findPaceMakerGoals(anyLong());
        verify(paceMakerMapper, never()).findPaceMakerGoalDepositAssets(anyLong());
        verify(paceMakerMapper, never()).findPaceMakerGoalWithdrawalAccounts(anyLong());
    }

    @Test
    @DisplayName("Deposit pace maker balance to a goal deposit account")
    void depositToGoal_toAccount_success() {
        Long accountId = 8L;
        Long moneyBoxAccountId = 10L;
        PaceMakerGoalDepositRequest request =
                new PaceMakerGoalDepositRequest(3L, AssetType.ACCOUNT, accountId, 270_000L);
        MoneyBox paceMakerMoneyBox = MoneyBox.builder()
                .moneyBoxId(3L)
                .userId(USER_ID)
                .accountId(moneyBoxAccountId)
                .balance(300_000L)
                .build();

        when(moneyBoxMapper.findPaceMakerMoneyBoxByIdAndUserIdForUpdate(3L, USER_ID))
                .thenReturn(paceMakerMoneyBox);
        when(paceMakerMapper.existsGoalDepositAssetByUserIdAndAsset(USER_ID, AssetType.ACCOUNT, accountId))
                .thenReturn(true);
        when(moneyBoxMapper.decreaseBalance(3L, USER_ID, 270_000L)).thenReturn(1);
        when(accountMapper.increaseBalance(accountId, USER_ID, 270_000L)).thenReturn(1);

        PaceMakerGoalDepositResponse response = paceMakerService.depositToGoal(USER_ID, request);

        assertEquals(AssetType.ACCOUNT, response.getAssetType());
        assertEquals(accountId, response.getAssetId());
        assertEquals(270_000L, response.getDepositedAmount());
        assertEquals(30_000L, response.getRemainingBalance());
        verify(moneyBoxMapper).findPaceMakerMoneyBoxByIdAndUserIdForUpdate(3L, USER_ID);
        verify(paceMakerMapper).existsGoalDepositAssetByUserIdAndAsset(USER_ID, AssetType.ACCOUNT, accountId);
        verify(moneyBoxMapper).decreaseBalance(3L, USER_ID, 270_000L);
        // 저금통이 속한 계좌와 목표 계좌는 항상 다른 물리 계좌라 실제 이체를 거쳐야 한다
        verify(accountTransferService).transfer(USER_ID, moneyBoxAccountId, accountId, 270_000L);
        verify(accountMapper).increaseBalance(accountId, USER_ID, 270_000L);
    }

    @Test
    @DisplayName("Deposit to a goal money box on a different account transfers first")
    void depositToGoal_toMoneyBox_differentAccount_transfers() {
        Long sourceAccountId = 10L;
        Long targetAccountId = 20L;
        Long targetMoneyBoxId = 55L;
        PaceMakerGoalDepositRequest request =
                new PaceMakerGoalDepositRequest(3L, AssetType.MONEY_BOX, targetMoneyBoxId, 100_000L);
        MoneyBox paceMakerMoneyBox = MoneyBox.builder()
                .moneyBoxId(3L).userId(USER_ID).accountId(sourceAccountId).balance(300_000L).build();
        MoneyBox targetMoneyBox = MoneyBox.builder()
                .moneyBoxId(targetMoneyBoxId).accountId(targetAccountId).build();

        when(moneyBoxMapper.findPaceMakerMoneyBoxByIdAndUserIdForUpdate(3L, USER_ID))
                .thenReturn(paceMakerMoneyBox);
        when(paceMakerMapper.existsGoalDepositAssetByUserIdAndAsset(
                USER_ID, AssetType.MONEY_BOX, targetMoneyBoxId)).thenReturn(true);
        when(moneyBoxMapper.findById(targetMoneyBoxId)).thenReturn(targetMoneyBox);

        paceMakerService.depositToGoal(USER_ID, request);

        verify(accountTransferService).transfer(USER_ID, sourceAccountId, targetAccountId, 100_000L);
        verify(accountMapper).increaseBalance(targetAccountId, USER_ID, 100_000L);
        verify(moneyBoxMapper).increaseBalance(targetMoneyBoxId, 100_000L);
    }

    @Test
    @DisplayName("Deposit to a goal money box on the same account skips the transfer")
    void depositToGoal_toMoneyBox_sameAccount_skipsTransfer() {
        Long sharedAccountId = 10L;
        Long targetMoneyBoxId = 55L;
        PaceMakerGoalDepositRequest request =
                new PaceMakerGoalDepositRequest(3L, AssetType.MONEY_BOX, targetMoneyBoxId, 100_000L);
        MoneyBox paceMakerMoneyBox = MoneyBox.builder()
                .moneyBoxId(3L).userId(USER_ID).accountId(sharedAccountId).balance(300_000L).build();
        MoneyBox targetMoneyBox = MoneyBox.builder()
                .moneyBoxId(targetMoneyBoxId).accountId(sharedAccountId).build();

        when(moneyBoxMapper.findPaceMakerMoneyBoxByIdAndUserIdForUpdate(3L, USER_ID))
                .thenReturn(paceMakerMoneyBox);
        when(paceMakerMapper.existsGoalDepositAssetByUserIdAndAsset(
                USER_ID, AssetType.MONEY_BOX, targetMoneyBoxId)).thenReturn(true);
        when(moneyBoxMapper.findById(targetMoneyBoxId)).thenReturn(targetMoneyBox);

        paceMakerService.depositToGoal(USER_ID, request);

        verify(accountTransferService, never()).transfer(any(), any(), any(), any());
        verify(accountMapper, never()).increaseBalance(anyLong(), anyLong(), anyLong());
        verify(moneyBoxMapper).increaseBalance(targetMoneyBoxId, 100_000L);
    }

    @Test
    @DisplayName("Deposit fails when amount is invalid")
    void depositToGoal_invalidAmount() {
        PaceMakerGoalDepositRequest request =
                new PaceMakerGoalDepositRequest(3L, AssetType.ACCOUNT, 8L, 0L);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> paceMakerService.depositToGoal(USER_ID, request)
        );

        assertEquals(PaceMakerErrorCode.INVALID_DEPOSIT_AMOUNT, exception.getErrorCode());
        verify(moneyBoxMapper, never()).findPaceMakerMoneyBoxByIdAndUserIdForUpdate(anyLong(), anyLong());
        verify(moneyBoxMapper, never()).decreaseBalance(anyLong(), anyLong(), anyLong());
        verify(accountMapper, never()).increaseBalance(anyLong(), anyLong(), anyLong());
    }

    @Test
    @DisplayName("Deposit fails when asset type is LOAN")
    void depositToGoal_loanAsset_rejected() {
        PaceMakerGoalDepositRequest request =
                new PaceMakerGoalDepositRequest(3L, AssetType.LOAN, 8L, 10_000L);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> paceMakerService.depositToGoal(USER_ID, request)
        );

        assertEquals(PaceMakerErrorCode.FORBIDDEN_GOAL_ACCOUNT, exception.getErrorCode());
        verify(moneyBoxMapper, never()).findPaceMakerMoneyBoxByIdAndUserIdForUpdate(anyLong(), anyLong());
    }

    @Test
    @DisplayName("Deposit fails when pace maker is not registered")
    void depositToGoal_notRegistered() {
        PaceMakerGoalDepositRequest request =
                new PaceMakerGoalDepositRequest(3L, AssetType.ACCOUNT, 8L, 10_000L);
        when(moneyBoxMapper.findPaceMakerMoneyBoxByIdAndUserIdForUpdate(3L, USER_ID)).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> paceMakerService.depositToGoal(USER_ID, request)
        );

        assertEquals(PaceMakerErrorCode.NOT_REGISTERED, exception.getErrorCode());
        verify(moneyBoxMapper).findPaceMakerMoneyBoxByIdAndUserIdForUpdate(3L, USER_ID);
        verify(paceMakerMapper, never())
                .existsGoalDepositAssetByUserIdAndAsset(anyLong(), any(), anyLong());
        verify(moneyBoxMapper, never()).decreaseBalance(anyLong(), anyLong(), anyLong());
        verify(accountMapper, never()).increaseBalance(anyLong(), anyLong(), anyLong());
    }

    @Test
    @DisplayName("Deposit fails when asset is not connected to user's goal")
    void depositToGoal_forbiddenGoalAccount() {
        Long accountId = 8L;
        PaceMakerGoalDepositRequest request =
                new PaceMakerGoalDepositRequest(3L, AssetType.ACCOUNT, accountId, 10_000L);
        MoneyBox paceMakerMoneyBox = MoneyBox.builder()
                .moneyBoxId(3L)
                .userId(USER_ID)
                .balance(300_000L)
                .build();
        when(moneyBoxMapper.findPaceMakerMoneyBoxByIdAndUserIdForUpdate(3L, USER_ID))
                .thenReturn(paceMakerMoneyBox);
        when(paceMakerMapper.existsGoalDepositAssetByUserIdAndAsset(USER_ID, AssetType.ACCOUNT, accountId))
                .thenReturn(false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> paceMakerService.depositToGoal(USER_ID, request)
        );

        assertEquals(PaceMakerErrorCode.FORBIDDEN_GOAL_ACCOUNT, exception.getErrorCode());
        verify(moneyBoxMapper, never()).decreaseBalance(anyLong(), anyLong(), anyLong());
        verify(accountMapper, never()).increaseBalance(anyLong(), anyLong(), anyLong());
    }

    @Test
    @DisplayName("Deposit fails when pace maker balance is insufficient")
    void depositToGoal_insufficientBalance() {
        Long accountId = 8L;
        PaceMakerGoalDepositRequest request =
                new PaceMakerGoalDepositRequest(3L, AssetType.ACCOUNT, accountId, 270_000L);
        MoneyBox paceMakerMoneyBox = MoneyBox.builder()
                .moneyBoxId(3L)
                .userId(USER_ID)
                .balance(100_000L)
                .build();
        when(moneyBoxMapper.findPaceMakerMoneyBoxByIdAndUserIdForUpdate(3L, USER_ID))
                .thenReturn(paceMakerMoneyBox);
        when(paceMakerMapper.existsGoalDepositAssetByUserIdAndAsset(USER_ID, AssetType.ACCOUNT, accountId))
                .thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> paceMakerService.depositToGoal(USER_ID, request)
        );

        assertEquals(PaceMakerErrorCode.INSUFFICIENT_BALANCE, exception.getErrorCode());
        verify(moneyBoxMapper, never()).decreaseBalance(anyLong(), anyLong(), anyLong());
        verify(accountMapper, never()).increaseBalance(anyLong(), anyLong(), anyLong());
    }
    private AutoSaving createAutoSaving(Long autoSavingId, String status) {
        AutoSaving autoSaving = new AutoSaving();
        ReflectionTestUtils.setField(autoSaving, "autoSavingId", autoSavingId);
        ReflectionTestUtils.setField(autoSaving, "userId", USER_ID);
        ReflectionTestUtils.setField(autoSaving, "moneyBoxId", 3L);
        ReflectionTestUtils.setField(autoSaving, "accountId", 4L);
        ReflectionTestUtils.setField(autoSaving, "maxAmount", 100_000L);
        ReflectionTestUtils.setField(autoSaving, "autoSavingStatus", status);
        return autoSaving;
    }

    private PaceMakerDashboardSummaryResponse createDashboardSummary() {
        PaceMakerDashboardSummaryResponse summary = new PaceMakerDashboardSummaryResponse();
        ReflectionTestUtils.setField(summary, "autoSavingId", 21L);
        ReflectionTestUtils.setField(summary, "status", "ACTIVE");
        ReflectionTestUtils.setField(summary, "maxAmount", 300_000L);
        ReflectionTestUtils.setField(summary, "moneyBoxId", 3L);
        ReflectionTestUtils.setField(summary, "balance", 120_000L);
        ReflectionTestUtils.setField(summary, "maskedAccountNumber", "123-***-789");
        ReflectionTestUtils.setField(summary, "todaySaved", true);
        ReflectionTestUtils.setField(summary, "todaySavingAmount", 5_000L);
        return summary;
    }

    private PaceMakerGoalSummaryResponse createGoalSummary(
            Long goalId,
            String goalName,
            String goalType,
            Long goalAmount
    ) {
        PaceMakerGoalSummaryResponse response = new PaceMakerGoalSummaryResponse();
        ReflectionTestUtils.setField(response, "goalId", goalId);
        ReflectionTestUtils.setField(response, "goalName", goalName);
        ReflectionTestUtils.setField(response, "goalType", goalType);
        ReflectionTestUtils.setField(response, "goalAmount", goalAmount);
        return response;
    }

    private PaceMakerGoalDepositAssetRowResponse createDepositAssetRow(
            Long goalId,
            String assetType,
            Long assetId,
            String financialInstitutionName,
            String maskedAccountNumber,
            Long balance
    ) {
        PaceMakerGoalDepositAssetRowResponse response = new PaceMakerGoalDepositAssetRowResponse();
        ReflectionTestUtils.setField(response, "goalId", goalId);
        ReflectionTestUtils.setField(response, "assetType", assetType);
        ReflectionTestUtils.setField(response, "assetId", assetId);
        ReflectionTestUtils.setField(response, "financialInstitutionName", financialInstitutionName);
        ReflectionTestUtils.setField(response, "maskedAccountNumber", maskedAccountNumber);
        ReflectionTestUtils.setField(response, "balance", balance);
        return response;
    }

    private PaceMakerGoalWithdrawalAccountRowResponse createWithdrawalAccountRow(
            Long goalId,
            Long accountId,
            String financialInstitutionName,
            String maskedAccountNumber,
            Long balance
    ) {
        PaceMakerGoalWithdrawalAccountRowResponse response = new PaceMakerGoalWithdrawalAccountRowResponse();
        ReflectionTestUtils.setField(response, "goalId", goalId);
        ReflectionTestUtils.setField(response, "accountId", accountId);
        ReflectionTestUtils.setField(response, "financialInstitutionName", financialInstitutionName);
        ReflectionTestUtils.setField(response, "maskedAccountNumber", maskedAccountNumber);
        ReflectionTestUtils.setField(response, "balance", balance);
        return response;
    }
}