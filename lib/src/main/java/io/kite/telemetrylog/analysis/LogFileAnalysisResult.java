package io.kite.telemetrylog.analysis;

import io.kite.telemetrylog.domain.LogLevel;

import java.util.Map;
import java.util.Objects;

public record LogFileAnalysisResult(
        long totalLines,
        long invalidLines,
        long totalEntries,
        Map<LogLevel, Long> entriesByLevel,
        Map<String, Long> entriesByService
) {

    public LogFileAnalysisResult {
        Objects.requireNonNull(entriesByLevel, "entriesByLevel");
        Objects.requireNonNull(entriesByService, "entriesByService");

        if (totalLines < 0) {
            throw new IllegalArgumentException("totalLines must be non-negative");
        }

        if (invalidLines < 0) {
            throw new IllegalArgumentException("invalidLines must be non-negative");
        }

        if (totalEntries < 0) {
            throw new IllegalArgumentException("totalEntries must be non-negative");
        }

        if (totalLines != invalidLines + totalEntries) {
            throw new IllegalArgumentException("totalLines must equal invalidLines + totalEntries");
        }

        entriesByLevel = Map.copyOf(entriesByLevel);
        entriesByService = Map.copyOf(entriesByService);

        long totalByLevel = entriesByLevel.values().stream().mapToLong(Long::longValue).sum();
        long totalByService = entriesByService.values().stream().mapToLong(Long::longValue).sum();

        for (long count: entriesByLevel.values()) {
            if (count <= 0) {
                throw new IllegalArgumentException("count entries by level must be positive");
            }
        }

        for (long count: entriesByService.values()) {
            if (count <= 0) {
                throw new IllegalArgumentException("count entries by service must be positive");
            }
        }

        if(totalByLevel != totalEntries) {
            throw new IllegalArgumentException("totalEntries must equal the sum of entriesByLevel");
        }

        if(totalByService != totalEntries) {
            throw new IllegalArgumentException("totalEntries must equal the sum of entriesByService");
        }
    }
}
