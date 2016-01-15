package net.buycraft.plugin.client;

import com.google.gson.Gson;
import com.squareup.okhttp.*;
import net.buycraft.plugin.data.responses.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Objects;

public class ProductionApiClient implements ApiClient {
    private static final String API_URL = "https://plugin.buycraft.net";
    private static final MediaType FORMENCODED = MediaType.parse("application/x-www-form-urlencoded");

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
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) {
                content.append('&');
            }
            content.append("ids[]=");
            content.append(ids.get(i));
        }

        Request request = new Request.Builder()
                .url(API_URL + "/queue")
                .addHeader("X-Buycraft-Secret", secret)
                .method("DELETE", RequestBody.create(FORMENCODED, content.toString()))
                .build();
        Response response = httpClient.newCall(request).execute();

        if (!response.isSuccessful()) {
            BuycraftError error = gson.fromJson(response.body().charStream(), BuycraftError.class);
            throw new ApiException(error.getErrorMessage());
        }
    }

    @Override
    public CheckoutUrlResponse getCheckoutUri(String username, int packageId) throws IOException, ApiException {
        String encodedUsername = URLEncoder.encode(username, "UTF-8");
        String content = "username=" + encodedUsername + "&package_id=" + packageId;

        Request request = new Request.Builder()
                .url(API_URL + "/checkout")
                .addHeader("X-Buycraft-Secret", secret)
                .post(RequestBody.create(FORMENCODED, content))
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
