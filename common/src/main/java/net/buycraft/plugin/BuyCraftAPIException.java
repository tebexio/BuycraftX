package net.buycraft.plugin;

import okhttp3.Request;
import okhttp3.Response;

public class BuyCraftAPIException extends RuntimeException {
    private Request sentRequest;
    private Response receivedResponse;
    private String responseBody;

    public BuyCraftAPIException() {
    }

    public BuyCraftAPIException(String message) {
        super(message);
    }

    public BuyCraftAPIException(String message, Throwable cause) {
        super(message, cause);
    }

    public BuyCraftAPIException(Throwable cause) {
        super(cause);
    }

    public BuyCraftAPIException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public BuyCraftAPIException(String message, Request sentRequest, Response receivedResponse, String responseBody) {
        super(message);
        this.sentRequest = sentRequest;
        this.receivedResponse = receivedResponse;
        this.responseBody = responseBody;
    }

    public BuyCraftAPIException(String message, Throwable cause, Request sentRequest, Response receivedResponse, String responseBody) {
        super(message, cause);
        this.sentRequest = sentRequest;
        this.receivedResponse = receivedResponse;
        this.responseBody = responseBody;
    }

    public Request getSentRequest() {
        return sentRequest;
    }

    public Response getReceivedResponse() {
        return receivedResponse;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
