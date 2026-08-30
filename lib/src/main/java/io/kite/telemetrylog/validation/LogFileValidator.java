package io.kite.telemetrylog.validation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

public final class LogFileValidator {

    private LogFileValidator() { }

    public static void verifyPath(Path path) {
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
