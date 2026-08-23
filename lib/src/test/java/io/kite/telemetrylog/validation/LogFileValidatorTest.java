package io.kite.telemetrylog.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class LogFileValidatorTest {

    private static final String TIMESTAMP = "2026-08-16T10:15:30.00Z";
    private static final String LEVEL = "INFO";
    private static final String SERVICE_NAME = "auth-service";
    private static final String MESSAGE = "message";

    private static final String VALID_LOG = "%s %s %s %s".formatted(TIMESTAMP, LEVEL, SERVICE_NAME, MESSAGE);
    private static final String INVALID_LOG = "%s %s %s %s".formatted(
            "Hello", LEVEL, SERVICE_NAME, MESSAGE
    );

    @TempDir
    Path tempDir;

    @Test
    void shouldFailWhenFileDoesNotExist() {
        Path missingFile = tempDir.resolve("missing.log");

        var exception = assertThrows(
                LogFileValidationException.class,
                () -> LogFileValidator.validate(missingFile)
        );

        assertEquals(
                ValidationFailureReason.FILE_NOT_FOUND,
                exception.reason()
        );

        assertEquals(
                missingFile,
                exception.path()
        );
    }

    @Test
    void shouldFailWhenPathPointsToDirectory() throws IOException {
        Path directory = Files.createDirectory(tempDir.resolve("logs"));
        var exception = assertThrows(
                LogFileValidationException.class,
                () -> LogFileValidator.validate(directory)
        );

        assertEquals(
                ValidationFailureReason.NOT_A_REGULAR_FILE,
                exception.reason()
        );
        assertEquals(directory, exception.path());
    }

    @Test
    void shouldValidateEmptyFile() throws IOException {
        Path file = createUtf8File("empty.log", "");
        var result = LogFileValidator.validate(file);
        assertNotNull(result);
        assertEquals(0L, result.validatedCount());
    }

    @Test
    void shouldValidateSingleRecord() throws IOException {
        Path file = createUtf8File("single.log", VALID_LOG);
        var result = LogFileValidator.validate(file);
        assertNotNull(result);
        assertEquals(1L, result.validatedCount());
    }

    @Test
    void shouldValidateMultipleRecord() throws IOException {
        String content = String.join("\n",
                VALID_LOG, VALID_LOG, VALID_LOG);
        Path file = createUtf8File("multiple.log", content);
        var result = LogFileValidator.validate(file);
        assertNotNull(result);
        assertEquals(3L, result.validatedCount());
    }

    @Test
    void shouldFailWithFirstLine() throws IOException {
        String content = String.join("\n",
                INVALID_LOG, VALID_LOG, VALID_LOG);

        Path file = createUtf8File("first-line-failed.log", content);
        var exception = assertThrows(
                LogFileValidationException.class,
                () -> LogFileValidator.validate(file)
        );

        assertEquals(
                ValidationFailureReason.PARSE_FAILED,
                exception.reason()
        );
        assertEquals(1L, exception.lineNumber().orElseThrow());
        assertNotNull(exception.getCause());
    }

    @Test
    void shouldFailWithLastLine() throws IOException {
        String content = String.join("\n",
                VALID_LOG, VALID_LOG, INVALID_LOG);

        Path file = createUtf8File("last-failed.log", content);
        var exception = assertThrows(
                LogFileValidationException.class,
                () -> LogFileValidator.validate(file)
        );

        assertEquals(
                ValidationFailureReason.PARSE_FAILED,
                exception.reason()
        );
        assertEquals(3L, exception.lineNumber().orElseThrow());
        assertNotNull(exception.getCause());
    }

    @Test
    void shouldFailWithMiddleLine() throws IOException {
        String content = String.join("\n",
                VALID_LOG,
                INVALID_LOG,
                VALID_LOG,
                VALID_LOG
        );

        Path file = createUtf8File("middle-failed.log", content);
        var exception = assertThrows(
                LogFileValidationException.class,
                () -> LogFileValidator.validate(file)
        );

        assertEquals(
                ValidationFailureReason.PARSE_FAILED,
                exception.reason()
        );
        assertEquals(2L, exception.lineNumber().orElseThrow());
        assertNotNull(exception.getCause());
    }

    @Test
    void shouldFailWithEarliestFailLine() throws IOException {
        String content = String.join("\n",
                VALID_LOG,
                INVALID_LOG,
                VALID_LOG,
                INVALID_LOG,
                VALID_LOG
        );

        Path file = createUtf8File("multiple-failed.log", content);
        var exception = assertThrows(
                LogFileValidationException.class,
                () -> LogFileValidator.validate(file)
        );

        assertEquals(
                ValidationFailureReason.PARSE_FAILED,
                exception.reason()
        );
        assertEquals(2L, exception.lineNumber().orElseThrow());
        assertNotNull(exception.getCause());
    }

    @Test
    void shouldReportEmptyPhysicalLineAsInvalidRecord() throws IOException {
        String content = new StringBuilder()
                .append(VALID_LOG)
                .append("\n")
                .append("\n")
                .append(VALID_LOG)
                .append("\n")
                .append(INVALID_LOG)
                .append("\n")
                .toString();

        Path file = createUtf8File("multiple-failed.log", content);
        var exception = assertThrows(
                LogFileValidationException.class,
                () -> LogFileValidator.validate(file)
        );

        assertEquals(
                ValidationFailureReason.PARSE_FAILED,
                exception.reason()
        );
        assertEquals(2L, exception.lineNumber().orElseThrow());
        assertNotNull(exception.getCause());
    }

    @Test
    void shouldReportIoErrorForMalformedUtf8() throws IOException {
        Path file = tempDir.resolve("malformed.log");
        byte[] invalidUtf8 = {
                (byte) 0xC3,
                (byte) 0x28,
        };
        Files.write(file, invalidUtf8);
        var exception = assertThrows(
                LogFileValidationException.class,
                () -> LogFileValidator.validate(file)
        );
        assertEquals(file, exception.path());
        assertEquals(
                ValidationFailureReason.FILE_IO_ERROR,
                exception.reason()
        );
    }

    private Path createUtf8File(String filename, String content) throws IOException {
        Path file = tempDir.resolve(filename);
        return Files.writeString(file, content, StandardCharsets.UTF_8);
    }
}
