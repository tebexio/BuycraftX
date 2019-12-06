package net.buycraft.plugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.buycraft.plugin.data.Coupon;
import net.buycraft.plugin.data.GiftCard;
import net.buycraft.plugin.data.RecentPayment;
import net.buycraft.plugin.data.ServerEvent;
import net.buycraft.plugin.data.responses.*;
import okhttp3.*;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("UnnecessaryInterfaceModifier")
public interface BuyCraftAPI {
    static final String API_URL = "https://plugin.buycraft.net";
    static final String API_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssX";
    static final Gson gson = new GsonBuilder()
            .setDateFormat(API_DATE_FORMAT)
            .create();

    public static BuyCraftAPI create(final String secret) {
        return BuyCraftAPI.create(secret, null);
    }

    public static BuyCraftAPI create(final String secret, OkHttpClient client) {
        OkHttpClient.Builder clientBuilder = client != null ? client.newBuilder() : new OkHttpClient.Builder();
        //noinspection Convert2Lambda
        return new Retrofit.Builder()
                .baseUrl(API_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(clientBuilder
                        .addInterceptor(new Interceptor() {
                            @Override
                            public Response intercept(Chain chain) throws IOException {
                                Request original = chain.request();

                                Request request = original.newBuilder()
                                        .header("X-Buycraft-Secret", secret)
                                        .header("Accept", "application/json")
                                        .header("User-Agent", "BuycraftX")
                                        .method(original.method(), original.body())
                                        .build();

                                return chain.proceed(request);
                            }
                        })
                        .addInterceptor(new Interceptor() {
                            @Override
                            public Response intercept(Chain chain) throws IOException {
                                Response response = chain.proceed(chain.request());
                                if (!response.isSuccessful()) {
                                    ResponseBody body = response.body();
                                    if (body == null) {
                                        throw new BuyCraftAPIException("Unknown error occurred whilst deserializing error object.", response.request(), response, "");
                                    }
                                    String in = body.string();
                                    if (!Objects.equals(response.header("Content-Type"), "application/json")) {
                                        throw new BuyCraftAPIException("Unexpected content-type " + response.header("Content-Type"), response.request(), response, in);
                                    }
                                    BuycraftError error = gson.fromJson(in, BuycraftError.class);
                                    if (error != null) {
                                        throw new BuyCraftAPIException(error.getErrorMessage(), response.request(), response, in);
                                    } else {
                                        throw new BuyCraftAPIException("Unknown error occurred whilst deserializing error object.", response.request(), response, in);
                                    }
                                }

                                return response;
                            }
                        }).build())
                .build().create(BuyCraftAPI.class);
    }

    @GET("/information")
    public Call<ServerInformation> getServerInformation();

    @GET("/listing")
    public Call<Listing> retrieveListing();

    @GET("/queue")
    public Call<DueQueueInformation> retrieveDueQueue();

    @GET("/queue/offline-commands")
    public Call<QueueInformation> retrieveOfflineQueue();

    @GET("/queue/online-commands/{id}")
    public Call<QueueInformation> getPlayerQueue(@Path("id") int id);

    @FormUrlEncoded
    @HTTP(method = "DELETE", path = "/queue", hasBody = true)
    public Call<Void> deleteCommands(@Field("ids[]") List<Integer> ids);

    @FormUrlEncoded
    @POST("/checkout")
    public Call<CheckoutUrlResponse> getCheckoutUri(@Field("username") String username, @Field("package_id") int packageId);

    @FormUrlEncoded
    @POST("/checkout")
    public Call<CheckoutUrlResponse> getCategoryUri(@Field("username") String username, @Field("category_id") int categoryId); //TODO Figure out category=true

    @GET("/payments")
    public Call<List<RecentPayment>> getRecentPayments(@Query("limit") int limit);

    @GET("/coupons")
    public Call<CouponListing> getAllCoupons();

    @GET("/coupons/{id}")
    public Call<Coupon> getCoupon(@Path("id") int id);

    @DELETE("/coupons/{id}")
    public Call<Void> deleteCoupon(@Path("id") int id);

    @DELETE("/coupons/{id}/code")
    public Call<Void> deleteCoupon(@Path("id") String id);

    public default Call<CouponSingleListing> createCoupon(Coupon coupon) {
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
                .add("redeem_unlimited", coupon.getRedeemUnlimited()+"")
                .add("expire_never", coupon.getExpireNever()+"")
                .add("username", coupon.getUsername() == null ? "" : coupon.getUsername())
                .add("note", coupon.getNote() == null ? "" : coupon.getNote())
                .build();

        return createCoupon(body);
    }

    @POST("/coupons")
    public Call<CouponSingleListing> createCoupon(@Body RequestBody body);

    @GET("/gift-cards")
    public Call<GiftCardListing> getAllGiftCards();

    @GET("/gift-cards/{id}")
    public Call<GiftCardSingleListing> getGiftCard(@Path("id") int id);

    @DELETE("/gift-cards/{id}")
    public Call<GiftCardSingleListing> voidGiftCard(@Path("id") int id);

    public default Call<GiftCardSingleListing> topUpGiftCard(int id, BigDecimal amount) {
        return topUpGiftCard(id, new FormBody.Builder().add("amount", amount.toPlainString()).build());
    }

    @PUT("/gift-cards/{id}")
    public Call<GiftCardSingleListing> topUpGiftCard(@Path("id") int id, @Body RequestBody body);

    public default Call<GiftCardSingleListing> createGiftCard(BigDecimal amount, String note) {
        return createGiftCard(new FormBody.Builder()
                .add("amount", amount.toPlainString())
                .add("note", note)
                .build());
    }

    public default Call<GiftCardSingleListing> createGiftCard(GiftCard giftCard) {
        return createGiftCard(new FormBody.Builder()
                .add("amount", giftCard.getBalance().getStarting().toPlainString())
                .add("note", giftCard.getNote())
                .build());
    }

    @POST("/gift-cards")
    public Call<GiftCardSingleListing> createGiftCard(@Body RequestBody body);

    @POST("/events")
    public Call<Void> sendEvents(@Body List<ServerEvent> events);

}
