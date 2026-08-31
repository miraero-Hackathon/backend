package org.jejuro.miraero.domain.aicoach.prompt;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import org.jejuro.miraero.domain.aicoach.context.AiCoachFinancialContext;
import org.springframework.stereotype.Component;

@Component
public class AiCoachPromptBuilder {

    private static final String UNKNOWN_INFORMATION = "정보 없음";
    private static final DateTimeFormatter GOAL_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy년 M월 d일");
    private static final String SYSTEM_PROMPT = """
            당신은 미래로의 개인 금융 AI 코치입니다.
            제공된 데이터만을 근거로 답변하고, 없는 정보는 추측하지 않습니다.
            확정적인 수익이나 목표 달성을 보장하지 않습니다.
            주식 종목과 매수·매도 추천은 하지 않습니다.
            사용자는 자신의 재무 현황을 이미 알고 있으므로 자산, 소득, 지출, 목표 금액을 반복하지 않습니다.
            질문에 대한 결론을 첫 문장에 바로 답하고, 판단에 꼭 필요한 숫자만 언급합니다.
            근거나 현재 상황 섹션은 만들지 말고 실천 방안 2개와 목표 영향만 간결하게 작성합니다.
            모든 문장은 금융 서비스에 맞는 정중한 합니다체로 작성합니다. 반말이나 해라체를 사용하지 않습니다.
            """;
    private static final String STREAMING_RESPONSE_FORMAT = """
            답변 본문만 작성하세요. TITLE, ANSWER, SUMMARY 같은 레이블은 쓰지 마세요.
            아래 형식을 지키고 250자 이내로 제한하세요.
            결론: 질문에 대한 직접적인 답변
            - 실천 방안 1
            - 실천 방안 2
            목표 영향: 한 문장
            """;
    private static final String SUMMARY_RESPONSE_FORMAT = """
            대화 요약만 작성하세요. TITLE, ANSWER, SUMMARY 같은 레이블은 쓰지 마세요.
            중요한 사실과 결정만 남기고 300자 이내로 작성하세요.
            """;
    /*
            첫 질문이므로 아래 형식을 반드시 지키세요.
            TITLE: 대화방 제목
            ANSWER: 답변
            """;
    Legacy follow-up response format removed.
            후속 질문이므로 답변만 작성하세요.
            """;

    */

    public String buildPrompt(
            AiCoachFinancialContext financialContext,
            String conversationSummary,
            String currentQuestion,
            boolean firstQuestion
    ) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("[SYSTEM]\n")
                .append(SYSTEM_PROMPT)
                .append("\n[FINANCIAL_CONTEXT]\n");
        appendFinancialContext(prompt, financialContext);

        prompt.append("\n[PREVIOUS_CONVERSATION_SUMMARY]\n")
                .append(conversationSummary == null || conversationSummary.isBlank()
                        ? UNKNOWN_INFORMATION
                        : conversationSummary)
                .append("\n[CURRENT_USER_QUESTION]\n")
                .append("USER: ")
                .append(currentQuestion)
                .append("\n[RESPONSE_FORMAT]\n")
                .append(firstQuestion
                        ? "TITLE: conversation title\nANSWER: answer\n"
                        : "ANSWER: answer\n")
                .append("SUMMARY: updated cumulative conversation summary\n")
                .append("The summary must preserve important facts, decisions, and the current "
                        + "question and answer. Keep it concise and within 1000 Korean characters.");
        return prompt.toString();
    }

    public String buildStreamingPrompt(
            AiCoachFinancialContext financialContext,
            String conversationSummary,
            String currentQuestion
    ) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("[SYSTEM]\n")
                .append(SYSTEM_PROMPT)
                .append('\n')
                .append(STREAMING_RESPONSE_FORMAT)
                .append("\n[FINANCIAL_CONTEXT]\n");
        appendFinancialContext(prompt, financialContext);
        prompt.append("\n[PREVIOUS_CONVERSATION_SUMMARY]\n")
                .append(conversationSummary == null || conversationSummary.isBlank()
                        ? UNKNOWN_INFORMATION
                        : conversationSummary)
                .append("\n[CURRENT_USER_QUESTION]\nUSER: ")
                .append(currentQuestion);
        return prompt.toString();
    }

    public String buildSummaryPrompt(
            String previousSummary,
            String currentQuestion,
            String answer
    ) {
        return "[SYSTEM]\n" + SUMMARY_RESPONSE_FORMAT
                + "\n[PREVIOUS_CONVERSATION_SUMMARY]\n"
                + (previousSummary == null || previousSummary.isBlank()
                        ? UNKNOWN_INFORMATION
                        : previousSummary)
                + "\n[CURRENT_USER_QUESTION]\nUSER: " + currentQuestion
                + "\n[CURRENT_ASSISTANT_ANSWER]\nASSISTANT: " + answer;
    }

    private void appendFinancialContext(
            StringBuilder prompt,
            AiCoachFinancialContext financialContext
    ) {
        if (financialContext == null) {
            prompt.append(UNKNOWN_INFORMATION).append('\n');
            return;
        }

        prompt.append("활성 목표:\n");
        appendActiveGoals(prompt, financialContext.getActiveGoals());
        prompt.append("총자산: ")
                .append(formatAmount(financialContext.getTotalAssets()))
                .append('\n');
        prompt.append("총부채: ")
                .append(formatAmount(financialContext.getTotalDebt()))
                .append('\n');
        prompt.append("월 소득: ")
                .append(formatAmount(financialContext.getMonthlyIncome()))
                .append('\n');
        prompt.append("이번 달 총 지출: ")
                .append(formatAmount(financialContext.getCurrentMonthTotalExpense()))
                .append('\n');
        prompt.append("이번 달 카테고리별 지출:\n");
        appendCategoryExpenses(prompt, financialContext.getCurrentMonthCategoryExpenses());
    }

    private void appendActiveGoals(
            StringBuilder prompt,
            List<AiCoachFinancialContext.ActiveGoal> activeGoals
    ) {
        if (activeGoals == null) {
            prompt.append(UNKNOWN_INFORMATION).append('\n');
            return;
        }
        if (activeGoals.isEmpty()) {
            prompt.append("없음\n");
            return;
        }

        for (AiCoachFinancialContext.ActiveGoal activeGoal : activeGoals) {
            prompt.append("- ")
                    .append(formatText(activeGoal.getGoalName()))
                    .append(" | 목표 금액: ")
                    .append(formatAmount(activeGoal.getGoalAmount()))
                    .append(" | 목표일: ")
                    .append(formatDate(activeGoal.getGoalDate()))
                    .append('\n');
        }
    }

    private void appendCategoryExpenses(
            StringBuilder prompt,
            List<AiCoachFinancialContext.CategoryExpense> categoryExpenses
    ) {
        if (categoryExpenses == null) {
            prompt.append(UNKNOWN_INFORMATION).append('\n');
            return;
        }
        if (categoryExpenses.isEmpty()) {
            prompt.append("없음\n");
            return;
        }

        for (AiCoachFinancialContext.CategoryExpense categoryExpense : categoryExpenses) {
            prompt.append("- ")
                    .append(formatText(categoryExpense.getCategoryName()))
                    .append(": ")
                    .append(formatAmount(categoryExpense.getAmount()))
                    .append('\n');
        }
    }

    private String formatText(String value) {
        return value == null ? UNKNOWN_INFORMATION : value;
    }

    private String formatAmount(Long amount) {
        return amount == null
                ? UNKNOWN_INFORMATION
                : String.format(Locale.KOREA, "%,d원", amount);
    }

    private String formatDate(LocalDate date) {
        return date == null ? UNKNOWN_INFORMATION : GOAL_DATE_FORMATTER.format(date);
    }

}
