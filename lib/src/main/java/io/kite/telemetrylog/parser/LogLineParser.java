package io.kite.telemetrylog.parser;

import io.kite.telemetrylog.domain.LogEntry;
import io.kite.telemetrylog.domain.LogLevel;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Objects;

public final class LogLineParser {

    private record LogTextComponent(String timestamp, String level, String serviceName, String message) { }

    private LogLineParser() { }

    public static LogEntry parse(String logLine) {
        Objects.requireNonNull(logLine);
        var components = parseComponents(logLine);

        return new LogEntry(
                parseTimestamp(components.timestamp()),
                parseLevel(components.level()),
                parseServiceName(components.serviceName()),
                parseMessage(components.message())
        );
    }

    private static LogTextComponent parseComponents(String logLine) {
        final int required = 3;
        int currentField = 1;

        String timestamp = null;
        String level = null;
        String serviceName = null;
        String message = null;

        int startIndex = -1;

        for (int i = 0; i < logLine.length(); i++) {
            char ch = logLine.charAt(i);

            if (currentField > required && !isSeparator(ch)) {
                message = logLine.substring(i);
                break;
            }

            if (!isSeparator(ch) && startIndex == -1) {
                startIndex = i;
            } else if (isSeparator(ch) && startIndex != -1) {
                String component = logLine.substring(startIndex, i);
                if (currentField == 1) {
                    timestamp = component;
                } else if (currentField == 2) {
                    level = component;
                } else if (currentField == 3) {
                    serviceName = component;
                }
                startIndex = -1;
                currentField++;
            }
        }

        if (currentField <= required && startIndex != -1) {
            String component = logLine.substring(startIndex);
            if (currentField == 1) {
                timestamp = component;
            } else if (currentField == 2) {
                level = component;
            } else if (currentField == 3) {
                serviceName = component;
            }
        }

        return new LogTextComponent(timestamp, level, serviceName, message);
    }

    private static boolean isSeparator(char ch) {
        return ch == '\t' || ch == ' ';
    }

    private static Instant parseTimestamp(String timestamp) {
        if (timestamp == null) {
            throw new LogLineParseException(ParseFailureReason.BLANK_LINE);
        }
        try {
            return Instant.parse(timestamp);
        } catch (DateTimeParseException ex) {
            throw new LogLineParseException(ParseFailureReason.INVALID_TIMESTAMP, ex);
        }
    }

    private static LogLevel parseLevel(String level) {
        if (level == null) {
            throw new LogLineParseException(ParseFailureReason.MISSING_LEVEL);
        }
        try {
            return LogLevel.valueOf(level.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new LogLineParseException(ParseFailureReason.INVALID_LEVEL, ex);
        }
    }

    private static String parseServiceName(String serviceName) {
        if (serviceName == null) {
            throw new LogLineParseException(ParseFailureReason.MISSING_SERVICE_NAME);
        }
        return serviceName;
    }

    private static String parseMessage(String message) {
        return message == null ? "" : message;
    }
}
