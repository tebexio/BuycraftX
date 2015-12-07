package net.buycraft.plugin.client;

import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import net.buycraft.plugin.data.responses.*;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class ProductionApiClient implements ApiClient {
    private static final String API_URL = "https://plugin.buycraft.net/v1";

    private final HttpRequestFactory requestFactory;

    public ProductionApiClient(final String apiKey) {
        Objects.requireNonNull(apiKey, "apiKey");
        requestFactory = new NetHttpTransport.Builder().build().createRequestFactory(new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest httpRequest) throws IOException {
                HttpHeaders headers = new HttpHeaders();
                headers.set("X-Buycraft-Secret", apiKey);
                httpRequest.setHeaders(headers);
            }
        });
    }

    private <T> T get(String endpoint, Class<T> clazz) throws IOException, ApiException {
        HttpResponse response = requestFactory.buildGetRequest(new GenericUrl(API_URL + endpoint)).execute();

        if (response.isSuccessStatusCode()) {
            return response.parseAs(clazz);
        } else {
            BuycraftError error = response.parseAs(BuycraftError.class);
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
        StringBuilder urlParameterBuilder = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            if (i == 0) {
                // Start of the parameter list.
                urlParameterBuilder.append('?');
            } else {
                urlParameterBuilder.append('&');
            }
            urlParameterBuilder.append("ids[]=");
            urlParameterBuilder.append(ids.get(i));
        }

        GenericUrl url = new GenericUrl(API_URL + "/queue" + urlParameterBuilder.toString());
        HttpResponse response = requestFactory.buildDeleteRequest(url).execute();

        if (!response.isSuccessStatusCode()) {
            BuycraftError error = response.parseAs(BuycraftError.class);
            throw new ApiException(error.getErrorMessage());
        }
    }

    @Override
    public CheckoutUrlResponse getCheckoutUri(String username, int packageId) throws IOException, ApiException {
        return null;
    }
}
