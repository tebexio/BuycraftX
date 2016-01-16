package net.buycraft.plugin.client;

import com.google.gson.Gson;
import net.buycraft.plugin.data.responses.*;
import okhttp3.*;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class ProductionApiClient implements ApiClient {
    private static final String API_URL = "https://plugin.buycraft.net";

    private final Gson gson = new Gson();
    private final OkHttpClient httpClient;
    private final String secret;

    public ProductionApiClient(final String secret) {
        this.secret = Objects.requireNonNull(secret, "secret");
        this.httpClient = new OkHttpClient();
    }

    private <T> T get(String endpoint, Class<T> clazz) throws IOException, ApiException {
        Request request = new Request.Builder()
                .url(API_URL + endpoint)
                .addHeader("X-Buycraft-Secret", secret)
                .get()
                .build();
        Response response = httpClient.newCall(request).execute();

        if (response.isSuccessful()) {
            return gson.fromJson(response.body().charStream(), clazz);
        } else {
            BuycraftError error = gson.fromJson(response.body().charStream(), BuycraftError.class);
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
            throw new ApiException(error.getErrorMessage());
        } else {
            return gson.fromJson(response.body().charStream(), CheckoutUrlResponse.class);
        }
    }
}
