package net.buycraft.plugin.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.buycraft.plugin.data.Coupon;
import net.buycraft.plugin.data.RecentPayment;
import net.buycraft.plugin.data.responses.*;
import okhttp3.*;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class ProductionApiClient implements ApiClient {
    private static final String API_URL = "https://plugin.buycraft.net";
    private static final String API_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssX";

    private final Gson gson = new GsonBuilder()
            .setDateFormat(API_DATE_FORMAT)
            .create();
    private final OkHttpClient httpClient;
    private final String secret;

    private Logger logger;

    public ProductionApiClient(String secret) {
        this(secret, new OkHttpClient());
    }

    public ProductionApiClient(String secret, OkHttpClient client) {
        this.secret = Objects.requireNonNull(secret, "secret");
        this.httpClient = Objects.requireNonNull(client, "client");
    }

    public ProductionApiClient(String secret, OkHttpClient client, Logger logger) {
        this.secret = Objects.requireNonNull(secret, "secret");
        this.httpClient = Objects.requireNonNull(client, "client");
        this.logger = logger;
    }

    private Request.Builder getBuilder(String endpoint) {
        return new Request.Builder()
                .url(API_URL + endpoint)
                .addHeader("X-Buycraft-Secret", secret)
                .addHeader("Accept", "application/json")
                .addHeader("User-Agent", "BuycraftX");
    }

    private ApiException handleError(Response response, ResponseBody body) throws IOException {
        String in = body.string();
        if (!Objects.equals(response.header("Content-Type"), "application/json")) {
            return new ApiException("Unexpected content-type " + response.header("Content-Type"), response.request(), response, in);
        }
        BuycraftError error = gson.fromJson(in, BuycraftError.class);
        if (error != null) {
            return new ApiException(error.getErrorMessage(), response.request(), response, in);
        } else {
            return new ApiException("Unknown error occurred whilst deserializing error object.", response.request(), response, in);
        }
    }

    private <T> T get(String endpoint, Type type) throws IOException, ApiException {
        return get(endpoint, null, type);
    }

    private <T> T get(String endpoint, CacheControl control, Type type) throws IOException, ApiException {
        try {
            Request.Builder requestBuilder = getBuilder(endpoint).get();
            if (control != null) requestBuilder.cacheControl(control);
            Request request = requestBuilder.build();

            Response response = httpClient.newCall(request).execute();

            try (ResponseBody body = response.body()) {
                if (response.isSuccessful()) {
                    try {
                        return gson.fromJson(body.charStream(), type);
                    } catch (JsonSyntaxException e) {
                        throw new ApiException("Unable to parse response.", response.request(), response, body.string());
                    }
                } else {
                    throw handleError(response, body);
                }
            }
        } catch (Exception e) {
            if(this.logger != null) {
                this.logger.severe("Unable to connect to API. Please check that your secret key is correct.");
            }
            return null;
        }
    }

    @Override
    public ServerInformation getServerInformation() throws IOException, ApiException {
        return get("/information", CacheControl.FORCE_NETWORK, ServerInformation.class);
    }

    @Override
    public Listing retrieveListing() throws IOException, ApiException {
        Listing listing = get("/listing", CacheControl.FORCE_NETWORK, Listing.class);
        if(listing != null)
        listing.order();
        return listing;
    }

    @Override
    public QueueInformation retrieveOfflineQueue() throws IOException, ApiException {
        return get("/queue/offline-commands", CacheControl.FORCE_NETWORK, QueueInformation.class);
    }

    @Override
    public DueQueueInformation retrieveDueQueue() throws IOException, ApiException {
        return get("/queue", CacheControl.FORCE_NETWORK, DueQueueInformation.class);
    }

    @Override
    public QueueInformation getPlayerQueue(int id) throws IOException, ApiException {
        return get("/queue/online-commands/" + id, CacheControl.FORCE_NETWORK, QueueInformation.class);
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



        httpClient.newBuilder()
                .connectTimeout(6, TimeUnit.SECONDS)
                .writeTimeout(7, TimeUnit.SECONDS)
                .readTimeout(7, TimeUnit.SECONDS)
                .build().newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(final Call call, IOException e) {
                        // Error
                        System.out.println("Error when trying to mark commands as complete...");
                        e.printStackTrace();
                    }

                    @Override public void onResponse(Call call, Response response) throws IOException {
                        try (ResponseBody responseBody = response.body()) {
                            if (!response.isSuccessful()) {
                                System.out.println("Error when trying to mark commands as complete...");
                                System.out.println(response.body());
                            }
                        }
                    }
                });

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
                throw handleError(response, rspBody);
            } else {
                return gson.fromJson(rspBody.charStream(), CheckoutUrlResponse.class);
            }
        }
    }

    @Override
    public CheckoutUrlResponse getCategoryUri(String username, int categoryId) throws IOException, ApiException {
        RequestBody body = new FormBody.Builder()
                .add("username", username)
                .add("category", "true")
                .add("category_id", Integer.toString(categoryId))
                .build();

        Request request = getBuilder("/checkout")
                .post(body)
                .build();
        Response response = httpClient.newCall(request).execute();

        try (ResponseBody rspBody = response.body()) {
            if (!response.isSuccessful()) {
                throw handleError(response, rspBody);
            } else {
                return gson.fromJson(rspBody.charStream(), CheckoutUrlResponse.class);
            }
        }
    }

    @Override
    public List<RecentPayment> getRecentPayments(int limit) throws IOException, ApiException {
        return get("/payments?limit=" + limit, CacheControl.FORCE_NETWORK, new TypeToken<List<RecentPayment>>() {
        }.getType());
    }

    @Override
    public List<Coupon> getAllCoupons() throws IOException, ApiException {
        CouponListing listing = get("/coupons", CouponListing.class);
        if(listing == null){
            return null;
        }
        return listing.getData();
    }

    @Override
    public Coupon getCoupon(int id) throws IOException, ApiException {
        CouponSingleListing listing = get("/coupons/" + id, CouponSingleListing.class);
        return listing.getData();
    }

    @Override
    public void deleteCoupon(int id) throws IOException, ApiException {
        Request request = getBuilder("/coupons/" + id)
                .delete()
                .build();

        Response response = httpClient.newCall(request).execute();

        try (ResponseBody rspBody = response.body()) {
            if (!response.isSuccessful()) {
                throw handleError(response, rspBody);
            }
        }
    }

    @Override
    public void deleteCoupon(String id) throws IOException, ApiException {
        Request request = getBuilder("/coupons/" + id + "/code")
                .delete()
                .build();

        Response response = httpClient.newCall(request).execute();

        try (ResponseBody rspBody = response.body()) {
            if (!response.isSuccessful()) {
                throw handleError(response, rspBody);
            }
        }
    }

    @Override
    public Coupon createCoupon(Coupon coupon) throws IOException, ApiException {
        FormBody.Builder build = new FormBody.Builder()
                .add("code", coupon.getCode())
                .add("effective_on", coupon.getEffective().getType());
        switch (coupon.getEffective().getType()) {
            case "packages":
                for (Integer id1 : coupon.getEffective().getPackages()) {
                    build.add("packages[]", Integer.toString(id1));
                }
                break;
            case "categories":
                for (Integer id2 : coupon.getEffective().getCategories()) {
                    build.add("categories[]", Integer.toString(id2));
                }
                break;
        }
        RequestBody body = build.add("discount_type", coupon.getDiscount().getType())
                .add("discount_amount", coupon.getDiscount().getValue().toPlainString())
                .add("discount_percentage", coupon.getDiscount().getPercentage().toPlainString())
                .add("expire_type", coupon.getExpire().getType())
                .add("expire_limit", Integer.toString(coupon.getExpire().getLimit()))
                .add("expire_date", new SimpleDateFormat(API_DATE_FORMAT).format(coupon.getExpire().getDate()))
                .add("start_date", new SimpleDateFormat(API_DATE_FORMAT).format(coupon.getStartDate()))
                .add("basket_type", coupon.getBasketType())
                .add("minimum", coupon.getMinimum().toPlainString())
                .add("redeem_limit", Integer.toString(coupon.getUserLimit()))
                .add("discount_application_method", Integer.toString(coupon.getDiscountMethod()))
                .add("redeem_unlimited", coupon.getRedeemUnlimited() == 1 ? "true" : "false")
                .add("expire_never", coupon.getExpireNever() == 1 ? "true" : "false")
                .add("username", coupon.getUsername() == null ? "" : coupon.getUsername())
                .add("note", coupon.getNote() == null ? "" : coupon.getNote())
                .build();

        Request request = getBuilder("/coupons")
                .post(body)
                .build();
        Response response = httpClient.newCall(request).execute();

        try (ResponseBody rspBody = response.body()) {
            if (!response.isSuccessful()) {
                throw handleError(response, rspBody);
            } else {
                return gson.fromJson(rspBody.charStream(), CouponSingleListing.class).getData();
            }
        }
    }
}
