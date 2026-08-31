package org.jejuro.miraero.domain.product.dto.external;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class FssProductApiResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void 정기예금_응답을_역직렬화한다() throws Exception {
        String json = """
            {
              "result": {
                "prdt_div": "D",
                "total_count": 1,
                "max_page_no": 1,
                "now_page_no": 1,
                "err_cd": "000",
                "err_msg": "정상",
                "baseList": [{
                  "dcls_month": "202601",
                  "fin_co_no": "0010001",
                  "fin_prdt_cd": "DP001",
                  "kor_co_nm": "테스트은행",
                  "fin_prdt_nm": "테스트 정기예금",
                  "join_way": "영업점",
                  "mtrt_int": "만기 후 이율",
                  "spcl_cnd": "우대 조건",
                  "join_deny": "1",
                  "join_member": "제한없음",
                  "etc_note": "비고",
                  "max_limit": null,
                  "dcls_strt_day": "20260101",
                  "dcls_end_day": "20260131",
                  "fin_co_subm_day": "202601011200"
                }],
                "optionList": [{
                  "dcls_month": "202601",
                  "fin_co_no": "0010001",
                  "fin_prdt_cd": "DP001",
                  "intr_rate_type": "S",
                  "intr_rate_type_nm": "단리",
                  "save_trm": "12",
                  "intr_rate": null,
                  "intr_rate2": null
                }]
              }
            }
            """;

        FssDepositApiResponse response = objectMapper.readValue(json, FssDepositApiResponse.class);

        assertNotNull(response.getResult());
        assertEquals("D", response.getResult().getProductDivision());
        assertEquals(1, response.getResult().getBaseList().size());
        assertEquals(1, response.getResult().getOptionList().size());
        assertEquals("0010001", response.getResult().getBaseList().get(0).getFinancialCompanyCode());
        assertEquals("테스트 정기예금", response.getResult().getBaseList().get(0).getFinancialProductName());
        assertNull(response.getResult().getBaseList().get(0).getMaxLimit());
        assertEquals("12", response.getResult().getOptionList().get(0).getSaveTerm());
        assertNull(response.getResult().getOptionList().get(0).getInterestRate());
        assertNull(response.getResult().getOptionList().get(0).getMaximumInterestRate());
    }

    @Test
    void 적금_응답을_역직렬화한다() throws Exception {
        String json = """
            {
              "result": {
                "prdt_div": "S",
                "total_count": 1,
                "max_page_no": 1,
                "now_page_no": 1,
                "err_cd": "000",
                "err_msg": "정상",
                "baseList": [{
                  "dcls_month": "202601",
                  "fin_co_no": "0010002",
                  "fin_prdt_cd": "SP001",
                  "kor_co_nm": "테스트저축은행",
                  "fin_prdt_nm": "테스트 적금",
                  "join_way": "인터넷",
                  "mtrt_int": "만기 후 이율",
                  "spcl_cnd": "우대 조건",
                  "join_deny": "2",
                  "join_member": "서민전용",
                  "etc_note": "비고",
                  "max_limit": null,
                  "dcls_strt_day": "20260101",
                  "dcls_end_day": "20260131",
                  "fin_co_subm_day": "202601011200"
                }],
                "optionList": [{
                  "dcls_month": "202601",
                  "fin_co_no": "0010002",
                  "fin_prdt_cd": "SP001",
                  "intr_rate_type": "S",
                  "intr_rate_type_nm": "단리",
                  "rsrv_type": "F",
                  "rsrv_type_nm": "자유적립식",
                  "save_trm": "6",
                  "intr_rate": 3.15,
                  "intr_rate2": 3.55
                }]
              }
            }
            """;

        FssSavingApiResponse response = objectMapper.readValue(json, FssSavingApiResponse.class);

        assertNotNull(response.getResult());
        assertEquals("S", response.getResult().getProductDivision());
        assertEquals(1, response.getResult().getBaseList().size());
        assertEquals(1, response.getResult().getOptionList().size());
        assertEquals("SP001", response.getResult().getBaseList().get(0).getFinancialProductCode());
        assertNull(response.getResult().getBaseList().get(0).getMaxLimit());
        assertEquals("F", response.getResult().getOptionList().get(0).getReserveType());
        assertEquals("6", response.getResult().getOptionList().get(0).getSaveTerm());
        assertEquals(new BigDecimal("3.15"), response.getResult().getOptionList().get(0).getInterestRate());
        assertEquals(new BigDecimal("3.55"), response.getResult().getOptionList().get(0).getMaximumInterestRate());
    }
}
