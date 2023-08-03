package io.tebex.sdk.request;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A class for constructing and executing HTTP requests using the OkHttp library.
 */
public class TebexRequest {
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("analyse-#%1$d").build());

    private final Request.Builder request;
    private final OkHttpClient client;

    /**
     * Constructs an TebexRequest instance with a specified endpoint and OkHttpClient.
     *
     * @param endpoint The URL to send the request to.
     * @param client   The OkHttpClient instance to be used for executing the request.
     */
    public TebexRequest(String endpoint, OkHttpClient client) {
        this.request = new Request.Builder().url(endpoint);
        this.client = client;
    }

    /**
     * Adds a header to the request.
     *
     * @param key   The header key.
     * @param value The header value.
     * @return The TebexRequest instance for chaining.
     */
    public TebexRequest withHeader(String key, String value) {
        request.addHeader(key, value);
        return this;
    }

    /**
     * Sets the request body and method to POST.
     *
     * @param body The request body in JSON format.
     * @return The TebexRequest instance for chaining.
     */
    public TebexRequest withBody(String body) {
        return withBody(body, "POST");
    }

    /**
     * Sets the request body and method.
     *
     * @param body The request body in JSON format.
     * @return The TebexRequest instance for chaining.
     */
    public TebexRequest withBody(String body, String method) {
        request.method(method, RequestBody.create(body, MediaType.get("application/json; charset=utf-8")));
        return this;
    }

    /**
     * Sets the request as a delete request.
     *
     * @return The TebexRequest instance for chaining.
     */
    public TebexRequest delete() {
        request.delete();
        return this;
    }

    /**
     * Adds the secret key as a header to the request.
     *
     * @param secretKey The secret key.
     * @return The TebexRequest instance for chaining.
     */
    public TebexRequest withSecretKey(String secretKey) {
        return withHeader("X-Tebex-Secret", secretKey);
    }

    /**
     * Builds the request into a Call object.
     *
     * @return The Call object representing the request.
     */
    public Call build() {
        return client.newCall(request.build());
    }

    /**
     * Executes the request synchronously.
     *
     * @return The Response object from the server.
     * @throws IOException If there is a problem executing the request.
     */
    public Response send() throws IOException {
        return build().execute();
    }

    /**
     * Executes the request asynchronously and returns a CompletableFuture.
     *
     * @return The CompletableFuture that will complete with the server Response.
     */
    public CompletableFuture<Response> sendAsync() {
        CompletableFuture<Response> future = new CompletableFuture<>();

        EXECUTOR.submit(() -> this.build().enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                future.complete(response);
                response.close();
            }
        }));

        return future;
    }
}
