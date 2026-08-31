package org.jejuro.miraero.domain.aicoach.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AiCoachResponseParser {

    private static final Pattern TITLE_PATTERN = Pattern.compile("(?m)^TITLE\\s*:\\s*(.+)$");
    private static final Pattern ANSWER_PATTERN = Pattern.compile(
            "(?ms)^ANSWER\\s*:\\s*(.*?)(?=^SUMMARY\\s*:|\\z)"
    );
    private static final Pattern SUMMARY_PATTERN = Pattern.compile("(?ms)^SUMMARY\\s*:\\s*(.*)$");

    public ParsedResponse parse(String response) {
        String title = extractTitle(response);
        String answer = extractAnswer(response);
        String summary = extractSummary(response);
        return new ParsedResponse(title, answer, summary);
    }

    private String extractTitle(String response) {
        Matcher matcher = TITLE_PATTERN.matcher(response);
        return matcher.find() ? matcher.group(1).trim() : null;
    }

    private String extractAnswer(String response) {
        Matcher matcher = ANSWER_PATTERN.matcher(response);
        return matcher.find() ? matcher.group(1).trim() : response.trim();
    }

    private String extractSummary(String response) {
        Matcher matcher = SUMMARY_PATTERN.matcher(response);
        return matcher.find() ? matcher.group(1).trim() : null;
    }

    public static class ParsedResponse {

        private final String title;
        private final String answer;
        private final String summary;

        private ParsedResponse(String title, String answer, String summary) {
            this.title = title;
            this.answer = answer;
            this.summary = summary;
        }

        public String getTitle() {
            return title;
        }

        public String getAnswer() {
            return answer;
        }

        public String getSummary() {
            return summary;
        }
    }
}
