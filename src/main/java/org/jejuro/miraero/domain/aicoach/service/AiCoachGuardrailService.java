package org.jejuro.miraero.domain.aicoach.service;

import java.util.List;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AiCoachGuardrailService {

    private static final String PROMPT_ATTACK_MESSAGE =
            "AI 코치의 내부 지침이나 설정은 제공할 수 없어요. "
                    + "저축, 예산, 소비 관리와 관련된 질문을 해 주세요.";
    private static final String PERSONAL_DATA_MESSAGE =
            "계좌번호, 비밀번호, 인증번호 같은 민감한 금융정보는 입력하지 마세요. "
                    + "민감정보를 제외하고 다시 질문해 주세요.";
    private static final String FINANCIAL_HIGH_RISK_MESSAGE =
            "특정 금융상품의 매수·매도나 대출 실행을 결정해 드릴 수는 없어요. "
                    + "대신 위험 요소와 예산 관리 기준을 함께 살펴볼 수 있어요.";
    private static final String GENERAL_HARM_MESSAGE =
            "안전한 상담을 위해 이 요청에는 답변을 제공할 수 없어요. "
                    + "저축, 예산, 소비 관리와 관련된 질문을 해 주세요.";

    private static final List<Pattern> PROMPT_ATTACK_PATTERNS = List.of(
            Pattern.compile("(?i)(ignore|disregard).{0,30}(previous|above|system|instruction)"),
            Pattern.compile("(?i)(system|developer)\\s*(prompt|instruction)"),
            Pattern.compile("(이전|위|기존).{0,15}(지시|명령|규칙).{0,10}(무시|해제|바꿔)"),
            Pattern.compile("(시스템|개발자).{0,10}(프롬프트|지시|명령).{0,15}(보여|출력|공개|알려)"),
            Pattern.compile("(역할|규칙).{0,15}(변경|바꿔|무시|해제)")
    );
    private static final List<Pattern> PERSONAL_DATA_PATTERNS = List.of(
            Pattern.compile("(?<!\\d)\\d{10,19}(?!\\d)"),
            Pattern.compile("(?<!\\d)\\d{2,4}-\\d{2,4}-\\d{4,6}(?!\\d)"),
            Pattern.compile("(?i)(비밀번호|password|otp|인증번호|cvc|cvv)")
    );
    private static final List<Pattern> FINANCIAL_HIGH_RISK_INPUT_PATTERNS = List.of(
            Pattern.compile("(전재산|전부|올인|빚.?내).{0,20}(매수|사|투자|대출)"),
            Pattern.compile("(매수|사|팔|매도).{0,20}(확정|보장|무조건|반드시)"),
            Pattern.compile("(주식|코인|가상자산|비트코인|etf).{0,30}(매수|매도|사|팔).{0,20}(해|할까|추천)", Pattern.CASE_INSENSITIVE)
    );
    private static final List<Pattern> FINANCIAL_HIGH_RISK_OUTPUT_PATTERNS = List.of(
            Pattern.compile("(전재산|전부|올인|빚.?내).{0,20}(매수|사|투자|대출)"),
            Pattern.compile("(수익|원금).{0,10}(보장|확실|무조건)"),
            Pattern.compile("(주식|코인|가상자산|비트코인|etf).{0,30}(매수|매도|사|팔)", Pattern.CASE_INSENSITIVE)
    );

    public AiCoachGuardrailDecision inspectInput(String content) {
        if (!StringUtils.hasText(content)) {
            return AiCoachGuardrailDecision.allow();
        }
        if (matchesAny(content, PROMPT_ATTACK_PATTERNS)) {
            return AiCoachGuardrailDecision.block(AiCoachRiskType.PROMPT_ATTACK, PROMPT_ATTACK_MESSAGE);
        }
        if (matchesAny(content, PERSONAL_DATA_PATTERNS)) {
            return AiCoachGuardrailDecision.block(AiCoachRiskType.PERSONAL_DATA, PERSONAL_DATA_MESSAGE);
        }
        if (matchesAny(content, FINANCIAL_HIGH_RISK_INPUT_PATTERNS)) {
            return AiCoachGuardrailDecision.safeRedirect(
                    AiCoachRiskType.FINANCIAL_HIGH_RISK,
                    FINANCIAL_HIGH_RISK_MESSAGE
            );
        }
        return AiCoachGuardrailDecision.allow();
    }

    public AiCoachGuardrailDecision inspectOutput(String content) {
        if (!StringUtils.hasText(content)) {
            return AiCoachGuardrailDecision.block(
                    AiCoachRiskType.FINANCIAL_HIGH_RISK,
                    FINANCIAL_HIGH_RISK_MESSAGE
            );
        }
        if (matchesAny(content, PERSONAL_DATA_PATTERNS)) {
            return AiCoachGuardrailDecision.block(AiCoachRiskType.PERSONAL_DATA, PERSONAL_DATA_MESSAGE);
        }
        if (matchesAny(content, FINANCIAL_HIGH_RISK_OUTPUT_PATTERNS)) {
            return AiCoachGuardrailDecision.safeRedirect(
                    AiCoachRiskType.FINANCIAL_HIGH_RISK,
                    FINANCIAL_HIGH_RISK_MESSAGE
            );
        }
        return AiCoachGuardrailDecision.allow();
    }

    public AiCoachGuardrailDecision inspectModerationResult(boolean flagged) {
        if (!flagged) {
            return AiCoachGuardrailDecision.allow();
        }
        return AiCoachGuardrailDecision.block(AiCoachRiskType.GENERAL_HARM, GENERAL_HARM_MESSAGE);
    }

    private boolean matchesAny(String content, List<Pattern> patterns) {
        return patterns.stream().anyMatch(pattern -> pattern.matcher(content).find());
    }
}
