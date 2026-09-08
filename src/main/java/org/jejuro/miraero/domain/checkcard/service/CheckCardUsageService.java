package org.jejuro.miraero.domain.checkcard.service;

import java.time.YearMonth;
import java.util.List;
import org.jejuro.miraero.domain.checkcard.dto.response.CheckCardUsageStatusResponse;

public interface CheckCardUsageService {

    // 화면에 상위 2개 카드의 진행률("3/6개월")을 보여주기 위한 조회
    List<CheckCardUsageStatusResponse> getStatus(Long userId);

    // 배치(스케줄러/시연용 컨트롤러)가 호출하는 판정 실행
    int executeAll(YearMonth targetMonth, Long userId);
}
