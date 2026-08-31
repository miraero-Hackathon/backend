package org.jejuro.miraero.domain.youthpolicy.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import org.jejuro.miraero.domain.youthpolicy.client.YouthPolicyApiClient;
import org.jejuro.miraero.domain.youthpolicy.domain.YouthPolicy;
import org.jejuro.miraero.domain.youthpolicy.dto.external.YouthPolicyApiItem;
import org.jejuro.miraero.domain.youthpolicy.dto.external.YouthPolicyApiResponse;
import org.jejuro.miraero.domain.youthpolicy.mapper.YouthPolicyMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class YouthPolicySyncServiceImplTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private YouthPolicyApiClient youthPolicyApiClient;

    @Mock
    private YouthPolicyMapper youthPolicyMapper;

    private YouthPolicySyncService youthPolicySyncService;

    @BeforeEach
    void setUp() {
        youthPolicySyncService = new YouthPolicySyncServiceImpl(youthPolicyApiClient, youthPolicyMapper);
        lenient().when(youthPolicyMapper.findYouthPolicyIdByPolicyNo(anyString())).thenReturn(1L);
    }

    @Test
    void syncYouthPolicy_convertsApiItemAndUpsertsYouthPolicy() throws Exception {
        youthPolicySyncService.syncYouthPolicy(readItem("""
                {"plcyNo":"policy-1","plcyNm":"청년 정책","aplyYmd":"20260728 ~ 20260807","sprtTrgtMinAge":"19","sprtTrgtMaxAge":"39","earnMinAmt":"0","earnMaxAmt":"60000000","addAplyQlfcCndCn":"추가 자격","ptcpPrpTrgtCn":"참여 대상"}
                """));

        YouthPolicy youthPolicy = capturedYouthPolicy();
        assertEquals("policy-1", youthPolicy.getPolicyNo());
        assertEquals(LocalDate.of(2026, 7, 28), youthPolicy.getApplicationStartDate());
        assertEquals(LocalDate.of(2026, 8, 7), youthPolicy.getApplicationEndDate());
        assertEquals(19, youthPolicy.getMinAge());
        assertEquals(60_000_000L, youthPolicy.getMaxIncome());
        assertEquals("추가 자격\n참여 대상", youthPolicy.getQualification());
        assertNotNull(youthPolicy.getSyncedAt());
    }

    @Test
    void syncYouthPolicy_handlesUnparseablePeriodAndInvalidNumbers() throws Exception {
        youthPolicySyncService.syncYouthPolicy(readItem("""
                {"plcyNo":"policy-2","plcyNm":"상시 정책","aplyYmd":"상시","sprtTrgtMinAge":"미정","earnMinAmt":"invalid"}
                """));

        YouthPolicy youthPolicy = capturedYouthPolicy();
        assertNull(youthPolicy.getApplicationStartDate());
        assertNull(youthPolicy.getMinAge());
        assertNull(youthPolicy.getMinIncome());
    }

    @Test
    void syncYouthPolicies_requestsEachTargetKeywordAndUpsertsResults() throws Exception {
        when(youthPolicyApiClient.getYouthPolicies(eq(1), anyString())).thenReturn(readResponse("""
                {"resultCode":200,"result":{"pagging":{"totCount":1,"pageNum":1,"pageSize":10},"youthPolicyList":[{"plcyNo":"policy-1","plcyNm":"정책"}]}}
                """));

        youthPolicySyncService.syncYouthPolicies();

        ArgumentCaptor<String> keywordCaptor = ArgumentCaptor.forClass(String.class);
        verify(youthPolicyApiClient, times(7)).getYouthPolicies(eq(1), keywordCaptor.capture());
        assertEquals(List.of("대출", "보조금", "바우처", "금리혜택", "신용회복", "공공임대주택", "주거지원"),
                keywordCaptor.getAllValues());
        verify(youthPolicyMapper, times(7)).upsert(org.mockito.ArgumentMatchers.any(YouthPolicy.class));
    }

    @Test
    void syncYouthPolicies_requestsEveryPageForEachTargetKeyword() throws Exception {
        when(youthPolicyApiClient.getYouthPolicies(eq(1), anyString())).thenReturn(readResponse("""
                {"resultCode":200,"result":{"pagging":{"totCount":11,"pageNum":1,"pageSize":10},"youthPolicyList":[{"plcyNo":"first","plcyNm":"첫 정책"}]}}
                """));
        when(youthPolicyApiClient.getYouthPolicies(eq(2), anyString())).thenReturn(readResponse("""
                {"resultCode":200,"result":{"pagging":{"totCount":11,"pageNum":2,"pageSize":10},"youthPolicyList":[{"plcyNo":"second","plcyNm":"둘째 정책"}]}}
                """));

        youthPolicySyncService.syncYouthPolicies();

        verify(youthPolicyApiClient, times(7)).getYouthPolicies(eq(1), anyString());
        verify(youthPolicyApiClient, times(7)).getYouthPolicies(eq(2), anyString());
        verify(youthPolicyMapper, times(14)).upsert(org.mockito.ArgumentMatchers.any(YouthPolicy.class));
    }

    @Test
    void syncYouthPolicies_doesNotStoreWhenEveryKeywordHasNoResults() throws Exception {
        when(youthPolicyApiClient.getYouthPolicies(eq(1), anyString())).thenReturn(readResponse("""
                {"resultCode":200,"result":{"pagging":{"totCount":0,"pageNum":1,"pageSize":10},"youthPolicyList":[]}}
                """));

        youthPolicySyncService.syncYouthPolicies();

        verify(youthPolicyApiClient, times(7)).getYouthPolicies(eq(1), anyString());
        verifyNoInteractions(youthPolicyMapper);
    }

    private YouthPolicyApiItem readItem(String json) throws Exception {
        return objectMapper.readValue(json, YouthPolicyApiItem.class);
    }

    private YouthPolicyApiResponse readResponse(String json) throws Exception {
        return objectMapper.readValue(json, YouthPolicyApiResponse.class);
    }

    private YouthPolicy capturedYouthPolicy() {
        ArgumentCaptor<YouthPolicy> captor = ArgumentCaptor.forClass(YouthPolicy.class);
        verify(youthPolicyMapper).upsert(captor.capture());
        return captor.getValue();
    }
}
