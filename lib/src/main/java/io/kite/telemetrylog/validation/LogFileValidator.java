package io.kite.telemetrylog.validation;


import java.nio.file.Path;

public final class LogFileValidator {

    public record FileValidationResult(long validatedCount) {
    }

    public static FileValidationResult validate(Path path) {
        return null;
    }

    private LogFileValidator() { }
}
