package io.kite.telemetrylog.parser;

import io.kite.telemetrylog.domain.LogLevel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.FieldSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.*;

class LogLineParserTest {

    private static final String TIMESTAMP = "2026-08-16T10:15:30.00Z";
    private static final String LEVEL = "INFO";
    private static final String SERVICE_NAME = "auth-service";
    private static final String MESSAGE = "message";
    private static final String MESSAGE_WITH_SEP = "message \t\t      hello   \t  ";

    private static final String[] VALID_SEPARATOR_VARIATIONS = new String[]{
            "%s %s %s %s".formatted(TIMESTAMP, LEVEL, SERVICE_NAME, MESSAGE),
            "    %s  \t  %s  \t\t  %s    %s".formatted(TIMESTAMP, LEVEL, SERVICE_NAME, MESSAGE),
            "%s  \t  %s  \t\t  %s    %s".formatted(TIMESTAMP, LEVEL, SERVICE_NAME, MESSAGE),
            "\t\t%s\t%s\t\t%s\t%s".formatted(TIMESTAMP, LEVEL, SERVICE_NAME, MESSAGE),
    };

    @Test
    void parse_nullInput_rejectWithNPE() {
        assertThrows(NullPointerException.class, () -> LogLineParser.parse(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "", "    \t  \t ", "     ", "\t\t\t\t"
    })
    void parse_blankLogLine_reject(String logLine) {
        var ex = assertThrows(LogLineParseException.class, () -> LogLineParser.parse(logLine));

        assertEquals(ParseFailureReason.BLANK_LINE, ex.reason());
    }

    @ParameterizedTest
    @FieldSource("VALID_SEPARATOR_VARIATIONS")
    void parse_validWithMultipleSepartor_accept(String logLine) {
        var entry = LogLineParser.parse(logLine);
        assertNotNull(entry);
        assertAll(() -> assertEquals(Instant.parse(TIMESTAMP), entry.timestamp()),
                () -> assertEquals(LogLevel.valueOf(LEVEL), entry.level()),
                () -> assertEquals(SERVICE_NAME, entry.serviceName()),
                () -> assertEquals(MESSAGE, entry.message())
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "INfO", "Info", "info"
    })
    void parse_mixCaseLevel_accept(String level) {
        String logLine = "%s %s %s %s".formatted(TIMESTAMP, level, SERVICE_NAME, MESSAGE);
        var entry = LogLineParser.parse(logLine);

        assertNotNull(entry);
        assertAll(
                () -> assertEquals(Instant.parse(TIMESTAMP), entry.timestamp()),
                () -> assertEquals(LogLevel.valueOf(LEVEL), entry.level()),
                () -> assertEquals(SERVICE_NAME, entry.serviceName()),
                () -> assertEquals(MESSAGE, entry.message())
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "", "      ", "  \t\t   "
    })
    void parse_absentMessage_acceptWithEmptyString(String message) {
        String logLine = "%s %s %s%s".formatted(TIMESTAMP, LEVEL, SERVICE_NAME, message);
        var entry = LogLineParser.parse(logLine);

        assertNotNull(entry);
        assertAll(() -> assertEquals(Instant.parse(TIMESTAMP), entry.timestamp()),
                () -> assertEquals(LogLevel.valueOf(LEVEL), entry.level()),
                () -> assertEquals(SERVICE_NAME, entry.serviceName()), () -> assertTrue(entry.message().isEmpty()));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "  " + MESSAGE_WITH_SEP,
            "      " + MESSAGE_WITH_SEP,
            "  \t\t   " + MESSAGE_WITH_SEP
    })
    void parse_messageWithSeparator_acceptWithPreserveMessage(String tail) {
        String logLine = "%s %s %s%s".formatted(TIMESTAMP, LEVEL, SERVICE_NAME, tail);
        var entry = LogLineParser.parse(logLine);

        assertNotNull(entry);
        assertAll(() -> assertEquals(Instant.parse(TIMESTAMP), entry.timestamp()),
                () -> assertEquals(LogLevel.valueOf(LEVEL), entry.level()),
                () -> assertEquals(SERVICE_NAME, entry.serviceName()),
                () -> assertEquals(MESSAGE_WITH_SEP, entry.message()));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "INfO ", "  \t\t hello   "
    })
    void parse_invalidTimestamp_reject(String timestamp) {
        String logLine = "%s %s %s".formatted(timestamp, LEVEL, SERVICE_NAME);

        var ex = assertThrows(
                LogLineParseException.class,
                () -> LogLineParser.parse(logLine)
        );

        assertEquals(ParseFailureReason.INVALID_TIMESTAMP, ex.reason());
        assertInstanceOf(DateTimeParseException.class, ex.getCause());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "", "    \t\t  ", "   ", "\t\t\t"
    })
    void parse_missingLevel_reject(String tail) {
        String logLine = "%s%s".formatted(TIMESTAMP, tail);
        var ex = assertThrows(LogLineParseException.class, () -> LogLineParser.parse(logLine));

        assertEquals(ParseFailureReason.MISSING_LEVEL, ex.reason());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "  \t\t  Fatal ", "auth-service", "Infoo", "\t\tauth"
    })
    void parse_invalidLevel_reject(String level) {
        String logLine = "%s %s %s".formatted(TIMESTAMP, level, SERVICE_NAME);
        var ex = assertThrows(LogLineParseException.class, () -> LogLineParser.parse(logLine));

        assertEquals(ParseFailureReason.INVALID_LEVEL, ex.reason());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "2026-08-16T10:15:30Z     INFO  \t\t  ",
            "    \t \t 2026-08-16T10:15:30Z    INFO  \t\t  ",
            "2026-08-16T10:15:30Z INFO",
            "2026-08-16T10:15:30Z   \t\t  INFO"
    })
    void parse_missingServiceName_reject(String logLine) {
        var ex = assertThrows(LogLineParseException.class, () -> LogLineParser.parse(logLine));

        assertEquals(ParseFailureReason.MISSING_SERVICE_NAME, ex.reason());
    }

    @ParameterizedTest
    @EnumSource(LogLevel.class)
    void parse_supportedLevel_accept(LogLevel level) {
        String logLine = "%s %s %s %s".formatted(TIMESTAMP, level.name(), SERVICE_NAME, MESSAGE);
        var entry = LogLineParser.parse(logLine);

        assertNotNull(entry);
        assertAll(
                () -> assertEquals(Instant.parse(TIMESTAMP), entry.timestamp()),
                () -> assertEquals(level, entry.level()),
                () -> assertEquals(SERVICE_NAME, entry.serviceName()),
                () -> assertEquals(MESSAGE, entry.message())
        );
    }
}
