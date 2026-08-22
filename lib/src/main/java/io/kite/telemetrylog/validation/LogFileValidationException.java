package io.kite.telemetrylog.validation;

import io.kite.telemetrylog.parser.LogLineParseException;

import java.nio.file.Path;
import java.util.Objects;
import java.util.OptionalLong;

public final class LogFileValidationException extends RuntimeException {

    private final ValidationFailureReason reason;
    private final Path path;
    private final Long lineNumber;

    private LogFileValidationException(
            ValidationFailureReason reason,
            Path path,
            Long lineNumber,
            String message,
            Throwable cause
    ) {
        super(message, cause);
        this.reason = reason;
        this.path = path;
        this.lineNumber = lineNumber;
    }

    public static LogFileValidationException fileNotFound(Path path) {
        Objects.requireNonNull(path);

        return new LogFileValidationException(
                ValidationFailureReason.FILE_NOT_FOUND,
                path,
                null,
                "Log file does not exist: " + path,
                null
        );
    }

    public static LogFileValidationException notRegularFile(Path path) {
        Objects.requireNonNull(path);

        return new LogFileValidationException(
                ValidationFailureReason.NOT_A_REGULAR_FILE,
                path,
                null,
                "Path does not point to a regular file: " + path,
                null
        );
    }

    public static LogFileValidationException ioError(Path path, Throwable cause) {
        Objects.requireNonNull(path);
        Objects.requireNonNull(cause);

        return new LogFileValidationException(
                ValidationFailureReason.FILE_IO_ERROR,
                path,
                null,
                "Failed to read log file: " + path,
                cause
        );
    }

    public static LogFileValidationException parseFailed(
            Path path,
            long lineNumber,
            LogLineParseException cause
    ) {
        Objects.requireNonNull(path);
        Objects.requireNonNull(cause);

        if (lineNumber <= 0) {
            throw new IllegalArgumentException("lineNumber must be positive");
        }

        return new LogFileValidationException(
                ValidationFailureReason.PARSE_FAILED,
                path,
                lineNumber,
                "Failed to parse log file at line " + lineNumber + ": " + path,
                cause
        );
    }

    public ValidationFailureReason reason() {
        return reason;
    }

    public Path path() {
        return path;
    }

    public OptionalLong lineNumber() {
        return lineNumber == null ?
                OptionalLong.empty() :
                OptionalLong.of(lineNumber);
    }
}
