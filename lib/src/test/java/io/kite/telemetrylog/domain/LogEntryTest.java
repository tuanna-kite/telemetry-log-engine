package io.kite.telemetrylog.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class LogEntryTest {
    private static final Instant TIMESTAMP = Instant.parse("2026-08-15T10:15:30Z");
    private static final LogLevel LOG_LEVEL = LogLevel.INFO;
    private static final String SERVICE_NAME = "auth-service";
    private static final String MESSAGE = "user A logged in system";

    @Test
    void constructor_withValidValues_thenCreateLogEntry() {
        var entry = new LogEntry(TIMESTAMP, LOG_LEVEL, SERVICE_NAME, MESSAGE);

        assertAll(
                () -> assertEquals(TIMESTAMP, entry.timestamp()),
                () -> assertEquals(LOG_LEVEL, entry.level()),
                () -> assertEquals(SERVICE_NAME, entry.serviceName()),
                () -> assertEquals(MESSAGE, entry.message())
        );
    }

    @Test
    void constructor_withEmptyMessage_thenCreateLogEntry() {
        var entry = new LogEntry(TIMESTAMP, LOG_LEVEL, SERVICE_NAME, "");
        assertEquals("", entry.message());
    }

    @Test
    void constructor_withNullTimestamp_thenRejectsInput() {
        assertThrows(
                NullPointerException.class,
                () -> new LogEntry(null, LOG_LEVEL, SERVICE_NAME, MESSAGE)
        );
    }

    @Test
    void constructor_withNullLevel_thenRejectsInput() {
        assertThrows(
                NullPointerException.class,
                () -> new LogEntry(TIMESTAMP, null, SERVICE_NAME, MESSAGE)
        );
    }

    @Test
    void constructor_withNullServiceName_thenRejectsInput() {
        assertThrows(
                NullPointerException.class,
                () -> new LogEntry(TIMESTAMP, LOG_LEVEL, null, MESSAGE)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "       ", "\t\t    \n", "\n\n\n"})
    void constructor_withBlankServiceName_thenRejectsInput(String name) {
        assertThrows(
                IllegalArgumentException.class,
                () -> new LogEntry(TIMESTAMP, LOG_LEVEL, name, MESSAGE)
        );
    }

    @Test
    void constructor_withNullMessage_thenRejectsInput() {
        assertThrows(
                NullPointerException.class,
                () -> new LogEntry(TIMESTAMP, LOG_LEVEL, SERVICE_NAME, null)
        );
    }

    @Test
    void equalComponents_produceEqualLogEntries() {
        var first = new LogEntry(TIMESTAMP, LOG_LEVEL, SERVICE_NAME, MESSAGE);
        var second = new LogEntry(TIMESTAMP, LOG_LEVEL, SERVICE_NAME, MESSAGE);
        assertAll(
                () -> assertEquals(first, second),
                () -> assertEquals(first.hashCode(), second.hashCode())
        );
    }

    @Test
    void equalComponents_produceSameHashCode() {
        var first = new LogEntry(TIMESTAMP, LOG_LEVEL, SERVICE_NAME, MESSAGE);
        var second = new LogEntry(TIMESTAMP, LOG_LEVEL, SERVICE_NAME, MESSAGE);
    }

    @Test
    void differentComponents_produceDifferentLogEntries() {
        var first = new LogEntry(TIMESTAMP, LOG_LEVEL, SERVICE_NAME, "message A");
        var second = new LogEntry(TIMESTAMP, LOG_LEVEL, SERVICE_NAME, "message B");
        assertNotEquals(first, second);
    }
}
