package io.kite.telemetrylog.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class LogFileValidatorTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldFailWhenFileDoesNotExist() {
        Path missingFile = tempDir.resolve("missing.log");

        var exception = assertThrows(
                LogFileValidationException.class,
                () -> LogFileValidator.verifyPath(missingFile)
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
                () -> LogFileValidator.verifyPath(directory)
        );

        assertEquals(
                ValidationFailureReason.NOT_A_REGULAR_FILE,
                exception.reason()
        );

        assertEquals(directory, exception.path());
    }

}
