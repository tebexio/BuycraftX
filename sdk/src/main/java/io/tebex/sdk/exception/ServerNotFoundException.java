package io.tebex.sdk.exception;

/**
 * Represents an exception thrown when a requested server is not found.
 */
public class ServerNotFoundException extends Throwable {

    /**
     * Returns the error message associated with the exception.
     *
     * @return The error message.
     */
    @Override
    public String getMessage() {
        return "That server doesn't exist!";
    }
}