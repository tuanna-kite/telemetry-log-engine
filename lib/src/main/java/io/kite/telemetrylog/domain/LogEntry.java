package io.kite.telemetrylog.domain;

import java.time.Instant;
import java.util.Objects;

public record LogEntry(Instant timestamp, LogLevel level, String serviceName, String message) {
    public LogEntry {
        Objects.requireNonNull(timestamp, "timestamp must be not null");
        Objects.requireNonNull(level, "level must be not null");
        Objects.requireNonNull(serviceName, "serviceName must be not null");
        Objects.requireNonNull(message, "message must be not null");

        if (serviceName.isBlank()) {
            throw new IllegalArgumentException("serviceName must not be blank");
        }
    }
}
