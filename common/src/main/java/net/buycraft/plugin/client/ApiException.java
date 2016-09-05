package net.buycraft.plugin.client;

import okhttp3.Request;
import okhttp3.Response;

public class ApiException extends Exception {
    private Request sentRequest;
    private Response receivedResponse;
    private String responseBody;

    public ApiException() {
    }

    public ApiException(String message) {
        super(message);
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApiException(Throwable cause) {
        super(cause);
    }

    public ApiException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ApiException(String message, Request sentRequest, Response receivedResponse, String responseBody) {
        super(message);
        this.sentRequest = sentRequest;
        this.receivedResponse = receivedResponse;
        this.responseBody = responseBody;
    }

    public ApiException(String message, Throwable cause, Request sentRequest, Response receivedResponse, String responseBody) {
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
