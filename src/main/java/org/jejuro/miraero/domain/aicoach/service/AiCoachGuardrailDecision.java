package org.jejuro.miraero.domain.aicoach.service;

import lombok.Getter;

@Getter
public class AiCoachGuardrailDecision {

    private final AiCoachGuardrailAction action;
    private final AiCoachRiskType riskType;
    private final String safeMessage;

    private AiCoachGuardrailDecision(
            AiCoachGuardrailAction action,
            AiCoachRiskType riskType,
            String safeMessage
    ) {
        this.action = action;
        this.riskType = riskType;
        this.safeMessage = safeMessage;
    }

    public static AiCoachGuardrailDecision allow() {
        return new AiCoachGuardrailDecision(
                AiCoachGuardrailAction.ALLOW,
                AiCoachRiskType.NONE,
                null
        );
    }

    public static AiCoachGuardrailDecision block(
            AiCoachRiskType riskType,
            String safeMessage
    ) {
        return new AiCoachGuardrailDecision(AiCoachGuardrailAction.BLOCK, riskType, safeMessage);
    }

    public static AiCoachGuardrailDecision safeRedirect(
            AiCoachRiskType riskType,
            String safeMessage
    ) {
        return new AiCoachGuardrailDecision(
                AiCoachGuardrailAction.SAFE_REDIRECT,
                riskType,
                safeMessage
        );
    }

    public boolean isAllowed() {
        return action == AiCoachGuardrailAction.ALLOW;
    }
}
