package net.buycraft.plugin.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.buycraft.plugin.data.RecentPayment;
import net.buycraft.plugin.data.responses.*;
import okhttp3.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

public class ProductionApiClient implements ApiClient {
    private static final String API_URL = "https://plugin.buycraft.net";
    private static final CacheControl NO_STORE = new CacheControl.Builder().noStore().build();

    private final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
            .create();
    private final OkHttpClient httpClient;
    private final String secret;

    public ProductionApiClient(String secret) {
        this(secret, new OkHttpClient());
    }

    public ProductionApiClient(String secret, OkHttpClient client) {
        this.secret = Objects.requireNonNull(secret, "secret");
        this.httpClient = Objects.requireNonNull(client, "client");
    }

    private Request.Builder getBuilder(String endpoint) {
        return new Request.Builder()
                .url(API_URL + endpoint)
                .addHeader("X-Buycraft-Secret", secret);
    }

    private <T> T get(String endpoint, Type type) throws IOException, ApiException {
        return get(endpoint, null, type);
    }

    private <T> T get(String endpoint, CacheControl control, Type type) throws IOException, ApiException {
        Request.Builder requestBuilder = getBuilder(endpoint).get();
        if (control != null) requestBuilder.cacheControl(control);
        Request request = requestBuilder.build();

        Response response = httpClient.newCall(request).execute();

        try (ResponseBody body = response.body()) {
            if (response.isSuccessful()) {
                return gson.fromJson(body.charStream(), type);
            } else {
                BuycraftError error = gson.fromJson(body.charStream(), BuycraftError.class);
                if (error != null) {
                    throw new ApiException(error.getErrorMessage());
                } else {
                    throw new ApiException("Unknown error occurred whilst deserializing error object.");
                }
            }
        }
    }

    @Override
    public ServerInformation getServerInformation() throws IOException, ApiException {
        return get("/information", CacheControl.FORCE_NETWORK, ServerInformation.class);
    }

    @Override
    public Listing retrieveListing() throws IOException, ApiException {
        Listing listing = get("/listing", CacheControl.FORCE_NETWORK, Listing.class);
        listing.order();
        return listing;
    }

    @Override
    public QueueInformation retrieveOfflineQueue() throws IOException, ApiException {
        return get("/queue/offline-commands", NO_STORE, QueueInformation.class);
    }

    @Override
    public DueQueueInformation retrieveDueQueue(int limit, int page) throws IOException, ApiException {
        return get("/queue?limit=" + limit + "&page=" + page, CacheControl.FORCE_NETWORK, DueQueueInformation.class);
    }

    @Override
    public QueueInformation getPlayerQueue(int id) throws IOException, ApiException {
        return get("/queue/online-commands/" + id, NO_STORE, QueueInformation.class);
    }

    @Override
    public void deleteCommand(List<Integer> ids) throws IOException, ApiException {
        FormBody.Builder builder = new FormBody.Builder();

        for (Integer id : ids) {
            builder.add("ids[]", id.toString());
        }

        Request request = getBuilder("/queue")
                .method("DELETE", builder.build())
                .build();
        Response response = httpClient.newCall(request).execute();

        try (ResponseBody body = response.body()) {
            if (!response.isSuccessful()) {
                BuycraftError error = gson.fromJson(body.charStream(), BuycraftError.class);
                throw new ApiException(error.getErrorMessage());
            }
        }
    }

    @Override
    public CheckoutUrlResponse getCheckoutUri(String username, int packageId) throws IOException, ApiException {
        RequestBody body = new FormBody.Builder()
                .add("username", username)
                .add("package_id", Integer.toString(packageId))
                .build();

        Request request = getBuilder("/checkout")
                .post(body)
                .build();
        Response response = httpClient.newCall(request).execute();

        try (ResponseBody rspBody = response.body()) {
            if (!response.isSuccessful()) {
                BuycraftError error = gson.fromJson(rspBody.charStream(), BuycraftError.class);
                throw new ApiException(error.getErrorMessage());
            } else {
                return gson.fromJson(rspBody.charStream(), CheckoutUrlResponse.class);
            }
        }
    }

    @Override
    public List<RecentPayment> getRecentPayments(int limit) throws IOException, ApiException {
        return get("/payments?limit=" + limit, CacheControl.FORCE_NETWORK, new TypeToken<List<RecentPayment>>() {}.getType());
    }
}
