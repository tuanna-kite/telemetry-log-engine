package io.kite.telemetrylog.analysis;

import io.kite.telemetrylog.domain.LogLevel;
import io.kite.telemetrylog.validation.LogFileValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LogFileAnalyzerTest {

    @TempDir
    Path tmpDir;

    private static final String TIMESTAMP = "2026-08-16T10:15:30.00Z";
    private static final String MESSAGE = "message";
    private static final int[] LOG_COUNTS = new int[]{10, 11, 12, 13, 15};
    private static final LogLevel[] LOG_LEVELS = LogLevel.values();
    private static final String[] SERVICES = new String[]{
            "auth-service", "order-service", "schedule-service",
            "payment-service", "finance-service"
    };
    private static final String[] MALFORMED_LINES = new String[]{
            "",
            "   \t\t\t   ",
            "hello",
            TIMESTAMP,
            TIMESTAMP + " " + "fatal",
            TIMESTAMP + " " + "info ",
    };


    @Test
    void analyze_fileDoesNotExist_validationException() {
        Path missingFile = tmpDir.resolve("missing.log");
        assertThrows(
                LogFileValidationException.class,
                () -> LogFileAnalyzer.analyze(missingFile)
        );
    }

    @Test
    void shouldFailWhenPathPointsToDirectory() throws IOException {
        Path directory = Files.createDirectory(tmpDir.resolve("logs"));

        assertThrows(
                LogFileValidationException.class,
                () -> LogFileAnalyzer.analyze(directory)
        );
    }

    @Test
    void analyze_failedDecode_throwIOException() throws IOException {
        Path file = tmpDir.resolve("by-service.log");
        Files.writeString(file, "test1234", StandardCharsets.UTF_16);
        assertThrows(
                IOException.class,
                () -> LogFileAnalyzer.analyze(file)
        );
    }

    @Test
    void analyze_emptyFile_zeroEntries() throws IOException {
        Path file = tmpDir.resolve("empty.log");
        Files.writeString(file, "", StandardCharsets.UTF_8);
        var result = LogFileAnalyzer.analyze(file);

        // Not null
        assertNotNull(result);
        assertNotNull(result.entriesByLevel());
        assertNotNull(result.entriesByService());

        // Zero total
        assertEquals(0, result.totalLines());
        assertEquals(0, result.totalEntries());
        assertEquals(0, result.entriesByLevel().size());
        assertEquals(0, result.entriesByService().size());
        assertEquals(0, result.invalidLines());
    }

    @Test
    void analyze_totalEntriesMustEqualsTotalByLevel() throws IOException {
        int[] logCounts = new int[]{10, 11, 12, 13, 15};
        LogLevel[] levels = LogLevel.values();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < logCounts.length; i++) {
            sb.append(generateLogs(logCounts[i], levels[i], "test-service"));
        }

        Path file = tmpDir.resolve("by-level.log");
        Files.writeString(file, sb, StandardCharsets.UTF_8);
        var result = LogFileAnalyzer.analyze(file);
        assertNotNull(result);
        assertNotNull(result.entriesByLevel());

        long totalByLevel = result.entriesByLevel().values()
                .stream()
                .mapToLong(Long::longValue)
                .sum();
        assertEquals(result.totalEntries(), totalByLevel);
    }

    @Test
    void analyze_totalEntriesMustEqualsTotalByService() throws IOException {
        Path file = tmpDir.resolve("by-service.log");
        createLogsFile(file);
        var result = LogFileAnalyzer.analyze(file);
        assertNotNull(result);
        assertNotNull(result.entriesByService());

        long totalByService = result.entriesByService().values()
                .stream()
                .mapToLong(Long::longValue)
                .sum();
        assertEquals(result.totalEntries(), totalByService);
    }

    @Test
    void analyze_totalByLevelMustCorrect() throws IOException {
        Path file = tmpDir.resolve("by-level.log");
        createLogsFile(file);
        var result = LogFileAnalyzer.analyze(file);
        assertNotNull(result);
        assertNotNull(result.entriesByLevel());
        for (int i = 0; i < LOG_COUNTS.length; i++) {
            assertEquals(
                    LOG_COUNTS[i],
                    result.entriesByLevel().getOrDefault(LOG_LEVELS[i], 0L)
            );
        }
    }

    @Test
    void analyze_totalByServiceMustCorrect() throws IOException {
        Path file = tmpDir.resolve("by-service.log");
        createLogsFile(file);
        var result = LogFileAnalyzer.analyze(file);
        assertNotNull(result);
        for (int i = 0; i < LOG_COUNTS.length; i++) {
            assertEquals(
                    LOG_COUNTS[i],
                    result.entriesByService().getOrDefault(SERVICES[i], 0L)
            );
        }
    }

    @Test
    void shouldReportIoErrorForMalformedUtf8() throws IOException {
        Path file = tmpDir.resolve("malformed.log");
        createLogsFile(file);
        byte[] invalidUtf8 = {
                (byte) 0xC3,
                (byte) 0x28,
        };
        Files.write(file, invalidUtf8, StandardOpenOption.APPEND);
        assertThrows(
                IOException.class,
                () -> LogFileAnalyzer.analyze(file)
        );

    }

    @Test
    void analyze_malformedLines_countsAsInvalid() throws IOException {
        Path file = tmpDir.resolve("malformed-lines.log");
        createLogsFile(file);
        int malformedLines = 20;
        Files.writeString(file,
                generateMalformedLogs(malformedLines),
                StandardOpenOption.APPEND);

        var result = LogFileAnalyzer.analyze(file);

        assertNotNull(result);
        assertEquals(malformedLines, result.invalidLines());
        assertEquals(result.totalLines(), result.invalidLines() + result.totalEntries());
    }

    @Test
    void analyze_malformedLines_countsAndCountinues() throws IOException {
        Path file = tmpDir.resolve("malformed-lines.log");
        String content = generateLogs(3, LogLevel.INFO, "auth-service")
                + generateMalformedLogs(20)
                + generateLogs(4, LogLevel.ERROR, "payment-service");
        Files.writeString(file, content, StandardCharsets.UTF_8);

        var result = LogFileAnalyzer.analyze(file);

        assertNotNull(result);
        assertEquals(20, result.invalidLines());
        assertEquals(7, result.totalEntries());
        assertEquals(27, result.totalLines());

        assertEquals(3L, result.entriesByLevel().get(LogLevel.INFO));
        assertEquals(4L, result.entriesByLevel().get(LogLevel.ERROR));

        assertEquals(
                result.totalLines(),
                result.totalEntries() + result.invalidLines()
        );
    }

    @Test
    void result_entriesByLevel_cannotBeModified() {
        Map<LogLevel, Long> levels = new HashMap<>();
        Map<String, Long> services = new HashMap<>();

        levels.put(LogLevel.INFO, 2L);
        services.put("auth", 2L);

        var results = new LogFileAnalysisResult(
                2, 0, 2,
                levels, services
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> results.entriesByLevel().put(LogLevel.ERROR, 3L)
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> results.entriesByService().put("order", 3L)
        );
    }

    @Test
    void result_doesNotChangeWhenSourceMapsAreModified() {
        Map<LogLevel, Long> levels = new HashMap<>();
        Map<String, Long> services = new HashMap<>();

        levels.put(LogLevel.INFO, 2L);
        services.put("auth", 2L);

        var results = new LogFileAnalysisResult(
                2, 0, 2,
                levels, services
        );

        levels.put(LogLevel.INFO, 4L);
        levels.put(LogLevel.DEBUG, 4L);
        services.put("auth", 5L);
        services.put("order", 3L);

        assertEquals(2L, results.entriesByLevel().getOrDefault(LogLevel.INFO, 0L));
        assertEquals(2L, results.entriesByService().getOrDefault("auth", 0L));
        assertEquals(1, results.entriesByLevel().size());
        assertEquals(1, results.entriesByService().size());

        levels.clear();
        services.clear();

        assertEquals(2L, results.entriesByLevel().getOrDefault(LogLevel.INFO, 0L));
        assertEquals(2L, results.entriesByService().getOrDefault("auth", 0L));
        assertEquals(1, results.entriesByLevel().size());
        assertEquals(1, results.entriesByService().size());
    }

    private String generateLogs(int lines, LogLevel level, String serviceName) {
        String log = "%s %s %s %s\n".formatted(TIMESTAMP, level.name(), serviceName, MESSAGE);
        return log.repeat(Math.max(0, lines));
    }

    private String generateMalformedLogs(int lines) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= lines; i++) {
            sb.append(MALFORMED_LINES[i % MALFORMED_LINES.length])
                    .append("\n");
        }
        return sb.toString();
    }

    private void createLogsFile(Path file) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < LOG_COUNTS.length; i++) {
            sb.append(generateLogs(LOG_COUNTS[i], LOG_LEVELS[i], SERVICES[i]));
        }

        Files.writeString(file, sb, StandardCharsets.UTF_8);
    }

}