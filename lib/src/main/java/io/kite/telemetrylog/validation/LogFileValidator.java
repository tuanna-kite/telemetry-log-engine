package io.kite.telemetrylog.validation;


import io.kite.telemetrylog.parser.LogLineParseException;
import io.kite.telemetrylog.parser.LogLineParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

public final class LogFileValidator {

    public record FileValidationResult(long validatedCount) {
    }

    public static FileValidationResult validate(Path path) {
        verifyPath(path);
        long validatedCount = 0;
        long lineNumber = 0;
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                try {
                    LogLineParser.parse(line);
                    validatedCount++;
                } catch (LogLineParseException ex) {
                    throw LogFileValidationException.parseFailed(
                            path, lineNumber, ex
                    );
                }
            }
        } catch (IOException ex) {
            throw LogFileValidationException.ioError(path, ex);
        }
        return new FileValidationResult(validatedCount);
    }

    private LogFileValidator() { }

    private static void verifyPath(Path path) {
        Objects.requireNonNull(path, "path");

        try {
            BasicFileAttributes attributes = Files.readAttributes(
                    path, BasicFileAttributes.class
            );
            if (!attributes.isRegularFile()) {
                throw LogFileValidationException.notRegularFile(path);
            }
        } catch (NoSuchFileException ex) {
            throw LogFileValidationException.fileNotFound(path);
        } catch (IOException ex) {
            throw LogFileValidationException.ioError(path, ex);
        }


    }
}
