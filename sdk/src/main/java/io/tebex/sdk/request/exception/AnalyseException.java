package io.tebex.sdk.request.exception;

public class AnalyseException extends Throwable {
    private final String message;

    public AnalyseException(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
