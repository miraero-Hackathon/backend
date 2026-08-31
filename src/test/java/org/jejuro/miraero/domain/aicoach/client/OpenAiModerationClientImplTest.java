package org.jejuro.miraero.domain.aicoach.client;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.jejuro.miraero.global.config.OpenAiProperties;
import org.junit.jupiter.api.Test;

class OpenAiModerationClientImplTest {

    @Test
    void doesNotCallModerationApiWhenModerationIsDisabled() {
        OpenAiProperties properties = new OpenAiProperties(
                "test-api-key",
                "gpt-5-nano",
                "https://api.openai.com/v1",
                1,
                700,
                "low",
                false
        );
        OpenAiModerationClient client = new OpenAiModerationClientImpl(properties);

        assertFalse(client.isFlagged("정상 질문"));
    }
}
