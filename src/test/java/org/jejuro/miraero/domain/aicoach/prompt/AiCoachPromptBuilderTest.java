package org.jejuro.miraero.domain.aicoach.prompt;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.jejuro.miraero.domain.aicoach.context.AiCoachFinancialContext;
import org.junit.jupiter.api.Test;

class AiCoachPromptBuilderTest {

    private final AiCoachPromptBuilder promptBuilder = new AiCoachPromptBuilder();

    @Test
    void buildPrompt_includesPreviousSummaryAndSummaryResponseFormat() {
        String prompt = promptBuilder.buildPrompt(
                AiCoachFinancialContext.builder().build(),
                "User plans to save 300,000 won monthly.",
                "How should I reduce food spending?",
                false
        );

        assertTrue(prompt.contains("[PREVIOUS_CONVERSATION_SUMMARY]"));
        assertTrue(prompt.contains("User plans to save 300,000 won monthly."));
        assertTrue(prompt.contains("[CURRENT_USER_QUESTION]"));
        assertTrue(prompt.contains("USER: How should I reduce food spending?"));
        assertTrue(prompt.contains("ANSWER: answer"));
        assertTrue(prompt.contains("SUMMARY: updated cumulative conversation summary"));
        assertFalse(prompt.contains("[RECENT_CONVERSATION]"));
    }

    @Test
    void buildPrompt_representsMissingSummaryAsUnknownInformation() {
        String prompt = promptBuilder.buildPrompt(
                AiCoachFinancialContext.builder().build(),
                null,
                "First question",
                true
        );

        assertTrue(prompt.contains("[PREVIOUS_CONVERSATION_SUMMARY]"));
        assertTrue(prompt.contains("TITLE: conversation title"));
        assertTrue(prompt.contains("SUMMARY: updated cumulative conversation summary"));
    }
}
