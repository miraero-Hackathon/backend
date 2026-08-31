package org.jejuro.miraero.domain.goal.milestone.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.aicoach.client.OpenAiClient;
import org.jejuro.miraero.domain.goal.milestone.dto.request.MilestoneReportAiRequest;
import org.jejuro.miraero.domain.goal.milestone.exception.MilestoneErrorCode;
import org.jejuro.miraero.global.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class MilestoneReportAiService {

    private final OpenAiClient openAiClient;
    private final ObjectMapper objectMapper =
            new ObjectMapper().findAndRegisterModules();


    public ParsedReport generateReport(
            MilestoneReportAiRequest request
    ) {

        try {

            String prompt =
                    buildPrompt(request);

            String response =
                    openAiClient.generateText(
                            prompt
                    );

            return parseAiResponse(
                    response
            );

        } catch (JsonProcessingException e) {

            throw new BusinessException(
                    MilestoneErrorCode.MILESTONE_REPORT_GENERATION_FAILED
            );
        }
    }

    private String buildPrompt(
            MilestoneReportAiRequest request
    ) throws JsonProcessingException {

        String requestJson =
                objectMapper.writeValueAsString(
                        request
                );

        return """
                당신은 금융 목표 달성 과정을 분석하고 사용자에게 다음 행동 방향을 알려주며 그동안의 여정을 격려해주는 AI 금융 코치입니다.
              
                아래 데이터를 바탕으로 마일스톤 리포트를 작성하세요.
              
                [응답 형식]
                - 반드시 다른 설명 없이 JSON 형식으로만 응답하세요.
                  {"title": "...", "content": "..."}
              
                [본문(content) 작성 구조 - 총 3~4문장, 150자 내외]
                    1. 공감과 격려: 적립을 지켜온 꾸준함을 인정 (1문장)
                    2. 과정 평가: 진행 속도와 소비 패턴이 목표 달성에 미친 영향을 해석 (1~2문장)
                    3. 행동 제안: 다음 마일스톤까지 실천할 수 있는 구체적이고 간단한 행동 제안 (1문장)
              
                [핵심 작성 규칙]
                    - 화면에 이미 노출되는 단순 수치(달성 금액, 달성률 %, 총지출 금액, 카테고리별 지출 금액 및 비율 %)는 절대 본문에 직접 쓰지 마세요.
                      (금지: "75% 달성하였으며 지출은 600만 원이고 식비가 47%입니다.")
                      (권장: "목표를 향해 안정적으로 나아가고 있으나, 최근 식비 지출의 비중이 다소 높아 달성 속도에 영향을 주었습니다.")
                    - 지출 데이터가 있는 경우 소비 영향을 해석하고, 없으면 목표 달성 과정 중심으로만 작성하세요.
                    - 제공된 데이터에 없는 사실이나 수치를 임의로 만들어내지 마세요.
                    - 한국어로 작성하세요.
                    - 제목은 짧고 명확하게 작성하세요. 마일스톤 달성 상황이 드러나도록 작성할 수 있습니다.
              
                [진행 속도(pace) 활용 규칙]
                  - pace가 null이면 진행 속도를 절대 언급하지 마세요.
                  - paceStatus가 AHEAD면 계획보다 앞선 상태, ON_TRACK이면 계획대로, BEHIND면 뒤처진 상태입니다.
                  - BEHIND일 때도 질책하지 말고, 회복 가능한 방향을 제시하세요.
                  - expectedAmount, differenceAmount 같은 금액은 직접 쓰지 말고 상태만 해석하세요.

                [적립 이력(savingSummary) 활용 규칙]
                  - attemptCount가 0이면 적립 이력을 언급하지 마세요.
                  - successCount/attemptCount는 약속을 지킨 비율입니다. 격려의 근거로 쓰세요.
                  - maxStreak는 최장 연속 적립일입니다. 값이 클 때 꾸준함의 근거로 쓸 수 있습니다.
                  - failedCount가 많으면 잔액 부족이 반복된 것이므로 지출 조절을 제안하세요.
                  - partialCount는 상한에 걸려 일부만 적립된 경우로, 상한 조정을 제안할 수 있습니다.
                  - 연속 일수는 언급해도 되지만 적립 금액은 직접 쓰지 마세요.

                [목표유형(goalType) 활용 규칙]
                  - INDEPENDENCE(자립)는 주거 독립, EMERGENCY(비상금)는 예기치 못한 상황 대비,
                    WEDDING(결혼)은 결혼 준비, LOAN(대출상환)은 상환 부담 경감이 목적입니다.
                  - 목표의 목적에 맞는 관점으로 해석하되, 유형 이름을 그대로 쓰지는 마세요.

                [추가 지침]
                  - changeRate가 제공된 경우에만 이전 기간과 비교하세요. changeRate가 null이면 증감 여부를 언급하지 마세요.
                  - 과도하게 단정적인 금융 조언을 하지 말고, 현재 데이터에 근거하여 현실적인 행동 방향을 제안하세요.
                  - 숫자는 반드시 목표 달성 과정이나 소비 특성을 설명하는 데 의미가 있을 때만 사용하세요.
                  - 마일스톤은 전체 목표(100%)를 달성하기 위한 25%, 50%, 75%, 100% 단위의 중간 단계입니다.
                    현재 진행 중인 마일스톤의 위치를 바탕으로 전체 여정에서의 남은 과제나 진행 흐름을 평가하세요.
                    (단, 본문에 '25%', '50%' 같은 숫자를 직접 작성하지는 마세요.)
                [올바른 작성 예시]
                    {
                      "title": "꾸준함이 만든 한 걸음",
                      "content": "20일 넘게 적립을 거르지 않으신 꾸준함이 돋보입니다. 계획보다 앞선 속도로 나아가고 있으나, 식비 비중이 늘어 흐름이 다소 무거워졌습니다. 다음 구간에는 식비를 한 끼 단위로 점검해 지금의 속도를 지켜보세요."
                    }
              
                요청 데이터:
              """ + requestJson;
    }

    private ParsedReport parseAiResponse(
            String response
    ) {

        if (!StringUtils.hasText(response)) {
            throw new BusinessException(
                    MilestoneErrorCode.MILESTONE_REPORT_GENERATION_FAILED
            );
        }

        String cleanedResponse =
                response
                        .replace("```json", "")
                        .replace("```", "")
                        .trim();

        try {

            JsonNode jsonNode =
                    objectMapper.readTree(
                            cleanedResponse
                    );

            String title =
                    jsonNode
                            .path("title")
                            .asText();

            String content =
                    jsonNode
                            .path("content")
                            .asText();

            if (!StringUtils.hasText(title)) {
                title = "마일스톤 AI 리포트";
            }

            if (!StringUtils.hasText(content)) {
                throw new BusinessException(
                        MilestoneErrorCode
                                .MILESTONE_REPORT_GENERATION_FAILED
                );
            }

            return new ParsedReport(
                    title.trim(),
                    content.trim()
            );

        } catch (JsonProcessingException e) {

            throw new BusinessException(
                    MilestoneErrorCode
                            .MILESTONE_REPORT_GENERATION_FAILED
            );
        }
    }

    public record ParsedReport(
            String title,
            String content
    ) {
    }
}