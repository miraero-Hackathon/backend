package org.jejuro.miraero.global.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.sql.Connection;
import java.sql.DriverManager;
import org.junit.jupiter.api.Test;

class DataConnectionTest {

    private static final String JDBC_URL_FORMAT =
        "jdbc:mysql://%s:%s/%s?serverTimezone=Asia/Seoul&characterEncoding=UTF-8";

    @Test
    void connectionTest() throws Exception {
        String host = getEnvOrDefault("MYSQL_HOST", "localhost");
        String port = getRequiredEnv("MYSQL_PORT");
        String database = getRequiredEnv("MYSQL_DATABASE");
        String username = getRequiredEnv("MYSQL_USER");
        String password = getRequiredEnv("MYSQL_PASSWORD");

        String jdbcUrl = String.format(JDBC_URL_FORMAT, host, port, database);

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            assertNotNull(connection);
        }
    }

    private String getRequiredEnv(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(key + " environment variable is required.");
        }
        return removeWrappingQuotes(value);
    }

    private String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return removeWrappingQuotes(value);
    }

    private String removeWrappingQuotes(String value) {
        if (value.length() < 2) {
            return value;
        }

        char first = value.charAt(0);
        char last = value.charAt(value.length() - 1);
        if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
}
