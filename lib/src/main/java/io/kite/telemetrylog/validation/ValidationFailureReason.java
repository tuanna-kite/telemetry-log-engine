package io.kite.telemetrylog.validation;

public enum ValidationFailureReason {
    FILE_NOT_FOUND,
    NOT_A_REGULAR_FILE,
    FILE_IO_ERROR,
    PARSE_FAILED
}
