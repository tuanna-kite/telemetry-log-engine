package io.kite.telemetrylog.analysis;

import io.kite.telemetrylog.domain.LogEntry;
import io.kite.telemetrylog.domain.LogLevel;
import io.kite.telemetrylog.parser.LogLineParseException;
import io.kite.telemetrylog.parser.LogLineParser;
import io.kite.telemetrylog.validation.LogFileValidator;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class LogFileAnalyzer {

    private LogFileAnalyzer() { }

    public static LogFileAnalysisResult analyze(Path path) throws IOException {
        LogFileValidator.verifyPath(path);

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            long totalLines = 0;
            long invalidLines = 0;
            long totalEntries = 0;
            Map<LogLevel, Long> entriesByLevel = new HashMap<>();
            Map<String, Long> entriesByService = new HashMap<>();

            while ((line = reader.readLine()) != null) {
                totalLines++;
                try {
                    LogEntry entry = LogLineParser.parse(line);
                    totalEntries++;
                    entriesByLevel.merge(entry.level(), 1L, Long::sum);
                    entriesByService.merge(entry.serviceName(), 1L, Long::sum);
                } catch (LogLineParseException exception) {
                    invalidLines++;
                }
            }

            return new LogFileAnalysisResult(
                    totalLines, invalidLines, totalEntries, entriesByLevel, entriesByService
            );
        }
    }
}
