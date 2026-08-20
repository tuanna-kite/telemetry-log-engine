package io.kite.telemetrylog.parser;

public enum ParseFailureReason {
    BLANK_LINE,
    INVALID_TIMESTAMP,
    MISSING_LEVEL,
    INVALID_LEVEL,
    MISSING_SERVICE_NAME,
}
