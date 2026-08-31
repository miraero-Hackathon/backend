package org.jejuro.miraero.domain.transaction.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.jejuro.miraero.domain.transaction.domain.ExpenseSimulationCurrentExpense;
import org.jejuro.miraero.domain.transaction.dto.request.ExpenseSimulationCategoryRequest;
import org.jejuro.miraero.domain.transaction.dto.request.ExpenseSimulationRequest;
import org.jejuro.miraero.domain.transaction.dto.response.ExpenseSimulationResponse;
import org.jejuro.miraero.domain.transaction.mapper.ExpenseSimulationMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExpenseSimulationServiceTest {
    @Mock private ExpenseSimulationMapper mapper;
    private ExpenseSimulationService service;
    @BeforeEach void setUp() { service = new ExpenseSimulationServiceImpl(mapper); }

    @Test void simulate_calculatesCategoriesAndTotalsInRequestOrder() {
        when(mapper.findCurrentExpensesByCategories(eq(42L), any(), any(), any())).thenReturn(Arrays.asList(current(2L,"교통",50L), current(1L,"식비",320L), current(3L,"여가",0L)));
        ExpenseSimulationResponse r = service.simulate(42L, request(2026,7, category(1L,250L),category(2L,100L),category(3L,10L)));
        assertEquals(1L,r.getCategories().get(0).getCategoryId()); assertEquals(70L,r.getCategories().get(0).getReductionAmount());
        assertEquals(0L,r.getCategories().get(1).getReductionAmount()); assertEquals(0L,r.getCategories().get(2).getCurrentExpense()); assertEquals("여가",r.getCategories().get(2).getCategoryName());
        assertEquals(370L,r.getCurrentTotalExpense()); assertEquals(360L,r.getTargetTotalExpense()); assertEquals(70L,r.getTotalReductionAmount());
    }
    @Test void simulate_rejectsMissingMapperCategoryWithoutCreatingResponse() {
        when(mapper.findCurrentExpensesByCategories(eq(42L), any(), any(), any())).thenReturn(Collections.singletonList(current(1L,"식비",0L)));
        assertInvalid(42L, request(2026,7,category(1L,0L),category(999L,0L)));
    }
    @Test void simulate_passesDecemberRangeAndIds() {
        when(mapper.findCurrentExpensesByCategories(eq(42L),any(),any(),any())).thenReturn(Arrays.asList(current(2L,"교통",0L), current(1L,"식비",0L))); service.simulate(42L,request(2026,12,category(2L,1L),category(1L,1L)));
        ArgumentCaptor<LocalDateTime> s=ArgumentCaptor.forClass(LocalDateTime.class), e=ArgumentCaptor.forClass(LocalDateTime.class); ArgumentCaptor<List<Long>> ids=ArgumentCaptor.forClass(List.class);
        verify(mapper).findCurrentExpensesByCategories(eq(42L),s.capture(),e.capture(),ids.capture()); assertEquals(LocalDateTime.of(2026,12,1,0,0),s.getValue()); assertEquals(LocalDateTime.of(2027,1,1,0,0),e.getValue()); assertEquals(Arrays.asList(2L,1L),ids.getValue());
    }
    @Test void simulate_rejectsInvalidInputsWithoutMapperCall() {
        List<ExpenseSimulationRequest> invalid=Arrays.asList(null,request(2026,0,category(1L,0L)),request(2026,13,category(1L,0L)),request(2026,7),request(2026,7,(ExpenseSimulationCategoryRequest)null),request(2026,7,category(null,0L)),request(2026,7,category(0L,0L)),request(2026,7,category(1L,null)),request(2026,7,category(1L,-1L)),request(2026,7,category(1L,0L),category(1L,0L)));
        assertInvalid(null,request(2026,7,category(1L,0L))); for(ExpenseSimulationRequest r:invalid) assertInvalid(42L,r); verifyNoInteractions(mapper);
    }
    @Test void simulate_rejectsDuplicateMapperCategoryAndOverflow() {
        when(mapper.findCurrentExpensesByCategories(eq(42L),any(),any(),any())).thenReturn(Arrays.asList(current(1L,"식비",1L),current(1L,"식비",2L))); assertInvalid(42L,request(2026,7,category(1L,0L)));
        when(mapper.findCurrentExpensesByCategories(eq(42L),any(),any(),any())).thenReturn(Arrays.asList(current(1L,"식비",Long.MAX_VALUE),current(2L,"교통",1L))); assertThrows(ArithmeticException.class,()->service.simulate(42L,request(2026,7,category(1L,0L),category(2L,0L))));
    }
    private void assertInvalid(Long id,ExpenseSimulationRequest r){BusinessException e=assertThrows(BusinessException.class,()->service.simulate(id,r));assertEquals(CommonErrorCode.INVALID_INPUT_VALUE,e.getErrorCode());}
    private ExpenseSimulationRequest request(int y,int m,ExpenseSimulationCategoryRequest... c){return ExpenseSimulationRequest.builder().year(y).month(m).categories(c==null?null:Arrays.asList(c)).build();}
    private ExpenseSimulationCategoryRequest category(Long id,Long amount){return ExpenseSimulationCategoryRequest.builder().categoryId(id).targetExpense(amount).build();}
    private ExpenseSimulationCurrentExpense current(Long id,String name,Long amount){return new ExpenseSimulationCurrentExpense(id,name,amount);}
}
