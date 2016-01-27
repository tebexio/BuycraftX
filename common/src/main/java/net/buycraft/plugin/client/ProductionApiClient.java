package net.buycraft.plugin.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.buycraft.plugin.data.RecentPayment;
import net.buycraft.plugin.data.responses.*;
import okhttp3.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class ProductionApiClient implements ApiClient {
    private static final String API_URL = "https://plugin.buycraft.net";

    private final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
            .create();
    private final OkHttpClient httpClient;
    private final String secret;

    public ProductionApiClient(final String secret) {
        this(secret, new OkHttpClient());
    }

    public ProductionApiClient(final String secret, OkHttpClient client) {
        this.secret = Objects.requireNonNull(secret, "secret");
        this.httpClient = Objects.requireNonNull(client, "client");
    }

    private <T> T get(String endpoint, Type type) throws IOException, ApiException {
        Request request = new Request.Builder()
                .url(API_URL + endpoint)
                .addHeader("X-Buycraft-Secret", secret)
                .get()
                .build();
        Response response = httpClient.newCall(request).execute();

        if (response.isSuccessful()) {
            T result = gson.fromJson(response.body().charStream(), type);
            response.body().close();
            return result;
        } else {
            BuycraftError error = gson.fromJson(response.body().charStream(), BuycraftError.class);
            response.body().close();
            throw new ApiException(error.getErrorMessage());
        }
    }

    @Override
    public ServerInformation getServerInformation() throws IOException, ApiException {
        return get("/information", ServerInformation.class);
    }

    @Override
    public Listing retrieveListing() throws IOException, ApiException {
        Listing listing = get("/listing", Listing.class);
        listing.order();
        return listing;
    }

    @Override
    public QueueInformation retrieveOfflineQueue() throws IOException, ApiException {
        return get("/queue/offline-commands", QueueInformation.class);
    }

    @Override
    public DueQueueInformation retrieveDueQueue() throws IOException, ApiException {
        return get("/queue", DueQueueInformation.class);
    }

    @Override
    public QueueInformation getPlayerQueue(int id) throws IOException, ApiException {
        return get("/queue/online-commands/" + id, QueueInformation.class);
    }

    @Override
    public void deleteCommand(List<Integer> ids) throws IOException, ApiException {
        FormBody.Builder builder = new FormBody.Builder();

        for (Integer id : ids) {
            builder.add("ids[]", id.toString());
        }

        Request request = new Request.Builder()
                .url(API_URL + "/queue")
                .addHeader("X-Buycraft-Secret", secret)
                .method("DELETE", builder.build())
                .build();
        Response response = httpClient.newCall(request).execute();

        if (!response.isSuccessful()) {
            BuycraftError error = gson.fromJson(response.body().charStream(), BuycraftError.class);
            response.body().close();
            throw new ApiException(error.getErrorMessage());
        }
    }

    @Override
    public CheckoutUrlResponse getCheckoutUri(String username, int packageId) throws IOException, ApiException {
        RequestBody body = new FormBody.Builder()
                .add("username", username)
                .add("package_id", Integer.toString(packageId))
                .build();

        Request request = new Request.Builder()
                .url(API_URL + "/checkout")
                .addHeader("X-Buycraft-Secret", secret)
                .post(body)
                .build();
        Response response = httpClient.newCall(request).execute();

        if (!response.isSuccessful()) {
            BuycraftError error = gson.fromJson(response.body().charStream(), BuycraftError.class);
            response.body().close();
            throw new ApiException(error.getErrorMessage());
        } else {
            CheckoutUrlResponse urlResponse = gson.fromJson(response.body().charStream(), CheckoutUrlResponse.class);
            response.body().close();
            return urlResponse;
        }
    }

    @Override
    public List<RecentPayment> getRecentPayments(int limit) throws IOException, ApiException {
        return get("/payments?limit=" + limit, new TypeToken<List<RecentPayment>>() {}.getType());
    }
}
