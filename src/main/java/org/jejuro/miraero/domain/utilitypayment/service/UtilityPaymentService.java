package org.jejuro.miraero.domain.utilitypayment.service;

import java.time.YearMonth;
import java.util.List;
import org.jejuro.miraero.domain.utilitypayment.dto.response.UtilityPaymentStatusResponse;

public interface UtilityPaymentService {

    // 화면에 5개 항목 진행률("5/6개월")을 보여주기 위한 조회
    List<UtilityPaymentStatusResponse> getStatus(Long userId);

    // 배치(스케줄러/시연용 컨트롤러)가 호출하는 판정 실행
    int executeAll(YearMonth targetMonth, Long userId);
}
