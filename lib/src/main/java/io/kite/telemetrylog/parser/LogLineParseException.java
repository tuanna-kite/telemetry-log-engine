package io.kite.telemetrylog.parser;

public class LogLineParseException extends RuntimeException {

    private final ParseFailureReason reason;

    public LogLineParseException(ParseFailureReason reason) {
        this.reason = reason;
    }

    public LogLineParseException(ParseFailureReason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public LogLineParseException(ParseFailureReason reason, String message, Throwable cause) {
        super(message, cause);
        this.reason = reason;
    }

    public LogLineParseException(ParseFailureReason reason, Throwable cause) {
        super(cause);
        this.reason = reason;
    }

    public ParseFailureReason reason() {
        return reason;
    }
}
