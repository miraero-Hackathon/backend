package org.jejuro.miraero.domain.aicoach.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AiCoachResponseParserTest {

    private final AiCoachResponseParser responseParser = new AiCoachResponseParser();

    @Test
    void parse_separatesAnswerAndSummary() {
        AiCoachResponseParser.ParsedResponse response = responseParser.parse(
                "TITLE: Budget plan\n"
                        + "ANSWER: Limit food spending to 450,000 won.\n"
                        + "SUMMARY: User aims to save 300,000 won monthly."
        );

        assertEquals("Budget plan", response.getTitle());
        assertEquals("Limit food spending to 450,000 won.", response.getAnswer());
        assertEquals("User aims to save 300,000 won monthly.", response.getSummary());
    }
}
