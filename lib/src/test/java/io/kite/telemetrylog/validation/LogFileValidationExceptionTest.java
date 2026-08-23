package io.kite.telemetrylog.validation;

import io.kite.telemetrylog.parser.LogLineParseException;
import io.kite.telemetrylog.parser.ParseFailureReason;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Path;
import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.*;

class LogFileValidationExceptionTest {

    @Test
    void fileNotFoundShouldPreseveFailureContext() {
        Path path = Path.of("missing.log");
        var exeption = LogFileValidationException.fileNotFound(path);
        assertEquals(path, exeption.path());
        assertEquals(ValidationFailureReason.FILE_NOT_FOUND, exeption.reason());
        assertTrue(exeption.lineNumber().isEmpty());
        assertNull(exeption.getCause());
    }

    @Test
    void notRegularFileShouldPreseveFailureContext() {
        Path path = Path.of("directory");
        var exeption = LogFileValidationException.notRegularFile(path);
        assertEquals(path, exeption.path());
        assertEquals(ValidationFailureReason.NOT_A_REGULAR_FILE, exeption.reason());
        assertTrue(exeption.lineNumber().isEmpty());
        assertNull(exeption.getCause());
    }

    @Test
    void ioErrorShouldPreserveCause() {
        Path path = Path.of("file.log");
        var ioExeption = new IOException("disk error");
        var exeption = LogFileValidationException.ioError(path, ioExeption);

        assertEquals(path, exeption.path());
        assertEquals(ValidationFailureReason.FILE_IO_ERROR, exeption.reason());
        assertTrue(exeption.lineNumber().isEmpty());
        assertSame(ioExeption, exeption.getCause());
    }

    @Test
    void parseFailedShouldPreserveLineNumberAndCause() {
        Path path = Path.of("file.log");
        var parseDatetimeExeption = new DateTimeParseException(
                "Failed to parse datetime", "parsed data", 0
        );
        long lineNumber = 100L;
        var parseException = new LogLineParseException(
                ParseFailureReason.INVALID_TIMESTAMP,
                parseDatetimeExeption
        );
        var exeption = LogFileValidationException.parseFailed(path, lineNumber, parseException);

        assertEquals(path, exeption.path());
        assertEquals(ValidationFailureReason.PARSE_FAILED, exeption.reason());
        assertEquals(lineNumber, exeption.lineNumber().orElseThrow());

        assertSame(parseException, exeption.getCause());
    }

    @ParameterizedTest
    @ValueSource(longs = {-1, 0, -100})
    void parseFailedShouldRejectNonPositiveLineNumber(long lineNumber) {
        Path path = Path.of("file.log");
        var parseDatetimeExeption = new DateTimeParseException(
                "Failed to parse datetime", "parsed data", 0
        );
        var parseException = new LogLineParseException(
                ParseFailureReason.INVALID_TIMESTAMP,
                parseDatetimeExeption
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> LogFileValidationException.parseFailed(path, lineNumber, parseException)
        );
    }
}