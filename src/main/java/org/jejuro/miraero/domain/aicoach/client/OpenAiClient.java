package org.jejuro.miraero.domain.aicoach.client;

import java.util.function.Consumer;

public interface OpenAiClient {

    String generateText(String prompt);

    String generateText(String prompt, int maxOutputTokens);

    String generateTextStream(String prompt, int maxOutputTokens, Consumer<String> deltaConsumer);
}
