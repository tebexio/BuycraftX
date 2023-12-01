package io.tebex.sdk;

import com.google.gson.*;
import io.tebex.sdk.exception.ServerNotFoundException;
import io.tebex.sdk.exception.ServerNotSetupException;
import io.tebex.sdk.obj.Package;
import io.tebex.sdk.obj.*;
import io.tebex.sdk.platform.Platform;
import io.tebex.sdk.request.TebexRequest;
import io.tebex.sdk.request.builder.CreateCouponRequest;
import io.tebex.sdk.request.response.DuePlayersResponse;
import io.tebex.sdk.request.response.OfflineCommandsResponse;
import io.tebex.sdk.request.response.PaginatedResponse;
import io.tebex.sdk.request.response.ServerInformation;
import io.tebex.sdk.util.Pagination;
import okhttp3.OkHttpClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

/**
 * The main SDK class for interacting with the Tebex API.
 */
public class SDK {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();
    private final OkHttpClient HTTP_CLIENT = new OkHttpClient().newBuilder().retryOnConnectionFailure(true).build();

    private final Platform platform;
    private String secretKey;

    /**
     * Constructs a new SDK instance with the specified platform and secret key.
     *
     * @param platform  The platform on which the SDK is running.
     * @param secretKey The secret key for authentication.
     */
    public SDK(Platform platform, String secretKey) {
        this.platform = platform;
        this.secretKey = secretKey;
    }

    /**
     * Retrieves information about the server.
     *
     * @return A CompletableFuture that contains the ServerInformation object.
     */
    public CompletableFuture<ServerInformation> getServerInformation() {
        if (getSecretKey() == null) {
            CompletableFuture<ServerInformation> future = new CompletableFuture<>();
            future.completeExceptionally(new ServerNotSetupException());
            return future;
        }

        return request("/information").withSecretKey(secretKey).sendAsync().thenApply(response -> {
            if(response.code() == 404 || response.code() == 403) {
                throw new CompletionException(new ServerNotFoundException());
            } else if(response.code() != 200) {
                throw new CompletionException(new IOException("Unexpected status code (" + response.code() + ")"));
            }

            try {
                JsonObject jsonObject = GSON.fromJson(response.body().string(), JsonObject.class);
                JsonObject account = jsonObject.getAsJsonObject("account");
                JsonObject server = jsonObject.getAsJsonObject("server");
                JsonObject currency = account.getAsJsonObject("currency");

                return new ServerInformation(
                        new ServerInformation.Store(
                                account.get("id").getAsInt(),
                                account.get("domain").getAsString(),
                                account.get("name").getAsString(),
                                new ServerInformation.Store.Currency(currency.get("iso_4217").getAsString(), currency.get("symbol").getAsString()),
                                account.get("online_mode").getAsBoolean(),
                                account.get("game_type").getAsString(), account.get("log_events").getAsBoolean()
                        ),
                        new ServerInformation.Server(server.get("id").getAsInt(), server.get("name").getAsString())
                );
            } catch (IOException e) {
                platform.sendTriageEvent(e);
                throw new CompletionException(new IOException("Unexpected response"));
            }
        });
    }

    /**
     * Get the players who have commands due to be executed when they next login.
     *
     * @return A CompletableFuture that contains the DuePlayersResponse object.
     */
    public CompletableFuture<DuePlayersResponse> getDuePlayers() {
        if (getSecretKey() == null) {
            CompletableFuture<DuePlayersResponse> future = new CompletableFuture<>();
            future.completeExceptionally(new ServerNotSetupException());
            return future;
        }

        return request("/queue").withSecretKey(secretKey).sendAsync().thenApply(response -> {
            if(response.code() == 404) {
                throw new CompletionException(new ServerNotFoundException());
            } else if(response.code() != 200) {
                throw new CompletionException(new IOException("Unexpected status code (" + response.code() + ")"));
            }

            try {
                JsonObject jsonObject = GSON.fromJson(response.body().string(), JsonObject.class);
                JsonObject meta = jsonObject.get("meta").getAsJsonObject();
                JsonArray server = jsonObject.get("players").getAsJsonArray();

                List<QueuedPlayer> players = new ArrayList<>();
                for(JsonElement element : server) {
                    JsonObject asJsonObject = element.getAsJsonObject();
                    players.add(QueuedPlayer.fromJson(asJsonObject));
                }

                return new DuePlayersResponse(meta.get("execute_offline").getAsBoolean(), meta.get("next_check").getAsInt(), meta.get("more").getAsBoolean(), players);
            } catch (IOException e) {
                platform.sendTriageEvent(e);
                throw new CompletionException(new IOException("Unexpected response"));
            }
        });
    }

    /**
     * Get the offline commands that are due to be executed.
     *
     * @return A CompletableFuture that contains the OfflineCommandsResponse object.
     */
    public CompletableFuture<OfflineCommandsResponse> getOfflineCommands() {
        if (getSecretKey() == null) {
            CompletableFuture<OfflineCommandsResponse> future = new CompletableFuture<>();
            future.completeExceptionally(new ServerNotSetupException());
            return future;
        }

        return request("/queue/offline-commands").withSecretKey(secretKey).sendAsync().thenApply(response -> {
            if(response.code() == 404) {
                throw new CompletionException(new ServerNotFoundException());
            } else if(response.code() != 200) {
                throw new CompletionException(new IOException("Unexpected status code (" + response.code() + ")"));
            }

            try {
                JsonObject jsonObject = GSON.fromJson(response.body().string(), JsonObject.class);
                JsonObject meta = jsonObject.get("meta").getAsJsonObject();
                JsonArray commands = jsonObject.get("commands").getAsJsonArray();

                List<QueuedCommand> offlineCommands = new ArrayList<>();
                for(JsonElement element : commands) {
                    JsonObject commandJson = element.getAsJsonObject();
                    JsonObject conditions = commandJson.get("conditions").getAsJsonObject();

                    QueuedPlayer queuedPlayer = QueuedPlayer.fromJson(commandJson.get("player").getAsJsonObject());
                    offlineCommands.add(new QueuedCommand(
                            commandJson.get("id").getAsInt(),
                            platform.getPlaceholderManager().handlePlaceholders(queuedPlayer, commandJson.get("command").getAsString()),
                            commandJson.get("payment").getAsInt(),
                            commandJson.get("package").getAsInt(),
                            conditions.get("delay").getAsInt(),
                            queuedPlayer
                    ));
                }

                return new OfflineCommandsResponse(meta.get("limited").getAsBoolean(), offlineCommands);
            } catch (IOException e) {
                platform.sendTriageEvent(e);
                throw new CompletionException(new IOException("Unexpected response"));
            }
        });
    }

    /**
     * Get the online commands that are due to be executed for a particular player.
     *
     * @param player The player for whom to retrieve commands.
     * @return A CompletableFuture that contains a list of QueuedCommand objects.
     */
    public CompletableFuture<List<QueuedCommand>> getOnlineCommands(QueuedPlayer player) {
        if (getSecretKey() == null) {
            CompletableFuture<List<QueuedCommand>> future = new CompletableFuture<>();
            future.completeExceptionally(new ServerNotSetupException());
            return future;
        }

        return request("/queue/online-commands/" + player.getId()).withSecretKey(secretKey).sendAsync().thenApply(response -> {
            if(response.code() == 404) {
                throw new CompletionException(new ServerNotFoundException());
            } else if(response.code() != 200) {
                throw new CompletionException(new IOException("Unexpected status code (" + response.code() + ")"));
            }

            try {
                JsonObject jsonObject = GSON.fromJson(response.body().string(), JsonObject.class);
                JsonArray commands = jsonObject.getAsJsonArray("commands");

                List<QueuedCommand> queuedCommands = new ArrayList<>();
                for(JsonElement element : commands) {
                    JsonObject commandJson = element.getAsJsonObject();
                    JsonObject conditions = commandJson.getAsJsonObject("conditions");

                    queuedCommands.add(new QueuedCommand(
                            commandJson.get("id").getAsInt(),
                            platform.getPlaceholderManager().handlePlaceholders(player, commandJson.get("command").getAsString()),
                            commandJson.get("payment").getAsInt(),
                            commandJson.get("package").getAsInt(),
                            conditions.get("delay").getAsInt(),
                            conditions.get("slots").getAsInt()

                    ));
                }

                return queuedCommands;
            } catch (IOException e) {
                platform.sendTriageEvent(e);
                throw new CompletionException(new IOException("Unexpected response"));
            }
        });
    }

    /**
     * Delete one or more commands which have been executed on the game server.
     *
     * @param ids The IDs of the commands to delete.
     * @return A CompletableFuture that returns true if the commands were deleted successfully.
     */
    public CompletableFuture<Boolean> deleteCommands(List<Integer> ids) {
        if (getSecretKey() == null) {
            CompletableFuture<Boolean> future = new CompletableFuture<>();
            future.completeExceptionally(new ServerNotSetupException());
            return future;
        }

        JsonArray idArray = new JsonArray();
        ids.forEach(idArray::add);

        JsonObject body = new JsonObject();
        body.add("ids", idArray);

        return request("/queue").withBody(GSON.toJson(body), "DELETE").withSecretKey(secretKey).sendAsync().thenApply(response -> {
            if(response.code() == 404) {
                throw new CompletionException(new ServerNotFoundException());
            } else if(response.code() != 204) {
                throw new CompletionException(new IOException("Unexpected status code (" + response.code() + ")"));
            }

            return true;
        });
    }

    /**
     * Get all community goals.
     *
     * @return A CompletableFuture that returns a list of CommunityGoal objects.
     */
    public CompletableFuture<List<CommunityGoal>> getCommunityGoals() {
        if (getSecretKey() == null) {
            CompletableFuture<List<CommunityGoal>> future = new CompletableFuture<>();
            future.completeExceptionally(new ServerNotSetupException());
            return future;
        }

        return request("/community_goals").withSecretKey(secretKey).sendAsync().thenApply(response -> {
            if(response.code() == 404) {
                throw new CompletionException(new ServerNotFoundException());
            } else if(response.code() != 200) {
                throw new CompletionException(new IOException("Unexpected status code (" + response.code() + ")"));
            }

            try {
                JsonArray jsonObject = GSON.fromJson(response.body().string(), JsonArray.class);
                return jsonObject.asList().stream().map(item -> CommunityGoal.fromJsonObject(item.getAsJsonObject())).collect(Collectors.toList());
            } catch (IOException e) {
                platform.sendTriageEvent(e);
                throw new CompletionException(new IOException("Unexpected response"));
            }
        });
    }

    /**
     * Get a specific community goal.
     *
     * @param communityGoalId The ID of the community goal to retrieve.
     * @return A CompletableFuture that contains the CommunityGoal object.
     */
    public CompletableFuture<CommunityGoal> getCommunityGoal(int communityGoalId) {
        if (getSecretKey() == null) {
            CompletableFuture<CommunityGoal> future = new CompletableFuture<>();
            future.completeExceptionally(new ServerNotSetupException());
            return future;
        }

        return request("/community_goals/" + communityGoalId).withSecretKey(secretKey).sendAsync().thenApply(response -> {
            if(response.code() == 404) {
                throw new CompletionException(new ServerNotFoundException());
            } else if(response.code() != 200) {
                throw new CompletionException(new IOException("Unexpected status code (" + response.code() + ")"));
            }

            try {
                JsonObject jsonObject = GSON.fromJson(response.body().string(), JsonObject.class);
                return CommunityGoal.fromJsonObject(jsonObject);
            } catch (IOException e) {
                platform.sendTriageEvent(e);
                throw new CompletionException(new IOException("Unexpected response"));
            }
        });
    }

    /**
     * Create a checkout URL for a package.
     *
     * @param packageId The ID of the package for which to create a checkout URL.
     * @param username  The username of the user who will be checking out.
     * @return A CompletableFuture that contains the CheckoutUrl object.
     */
    public CompletableFuture<CheckoutUrl> createCheckoutUrl(int packageId, String username) {
        if (getSecretKey() == null) {
            CompletableFuture<CheckoutUrl> future = new CompletableFuture<>();
            future.completeExceptionally(new ServerNotSetupException());
            return future;
        }

        JsonObject payload = new JsonObject();
        payload.addProperty("package_id", packageId);
        payload.addProperty("username", username);

        return request("/checkout").withBody(GSON.toJson(payload)).withSecretKey(secretKey).sendAsync().thenApply(response -> {
            if(response.code() == 404) {
                throw new CompletionException(new ServerNotFoundException());
            } else if(response.code() != 201) {
                throw new CompletionException(new IOException("Unexpected status code (" + response.code() + ")"));
            }

            try {
                return GSON.fromJson(response.body().string(), CheckoutUrl.class);
            } catch (IOException e) {
                platform.sendTriageEvent(e);
                throw new CompletionException(new IOException("Unexpected response"));
            }
        });
    }

    /**
     * Get all coupons.
     *
     * @return A CompletableFuture that returns a PaginatedResponse of Coupon objects.
     */
    public CompletableFuture<PaginatedResponse<Coupon>> getCoupons() {
        if (getSecretKey() == null) {
            CompletableFuture<PaginatedResponse<Coupon>> future = new CompletableFuture<>();
            future.completeExceptionally(new ServerNotSetupException());
            return future;
        }

        return request("/coupons").withSecretKey(secretKey).sendAsync().thenApply(response -> {
            if(response.code() == 404) {
                throw new CompletionException(new ServerNotFoundException());
            } else if(response.code() != 200) {
                throw new CompletionException(new IOException("Unexpected status code (" + response.code() + ")"));
            }

            try {
                JsonObject jsonObject = GSON.fromJson(response.body().string(), JsonObject.class);

                return new PaginatedResponse<>(
                        Pagination.fromJsonObject(jsonObject.getAsJsonObject("pagination")),
                        jsonObject.getAsJsonArray("data").asList().stream().map(item -> Coupon.fromJsonObject(item.getAsJsonObject())).collect(Collectors.toList())
                );
            } catch (IOException e) {
                platform.sendTriageEvent(e);
                throw new CompletionException(new IOException("Unexpected response"));
            }
        });
    }

    /**
     * Get a specific coupon.
     *
     * @param id The ID of the coupon to retrieve.
     * @return A CompletableFuture that contains the Coupon object.
     */
    public CompletableFuture<Coupon> getCoupon(int id) {
        if (getSecretKey() == null) {
            CompletableFuture<Coupon> future = new CompletableFuture<>();
            future.completeExceptionally(new ServerNotSetupException());
            return future;
        }

        return request("/coupons/" + id).withSecretKey(secretKey).sendAsync().thenApply(response -> {
            if(response.code() == 404) {
                throw new CompletionException(new ServerNotFoundException());
            } else if(response.code() != 200) {
                throw new CompletionException(new IOException("Unexpected status code (" + response.code() + ")"));
            }

            try {
                JsonObject jsonObject = GSON.fromJson(response.body().string(), JsonObject.class);
                return Coupon.fromJsonObject(jsonObject.get("data").getAsJsonObject());
            } catch (IOException e) {
                platform.sendTriageEvent(e);
                throw new CompletionException(new IOException("Unexpected response"));
            }
        });
    }

    /**
     * Create a coupon.
     *
     * @return A CompletableFuture that contains the new Coupon object.
     */
    public CompletableFuture<Coupon> createCoupon(CreateCouponRequest createCouponRequest) {
        if (getSecretKey() == null) {
            CompletableFuture<Coupon> future = new CompletableFuture<>();
            future.completeExceptionally(new ServerNotSetupException());
            return future;
        }

        JsonObject payload = new JsonObject();
        payload.addProperty("code", createCouponRequest.getCode());
        payload.addProperty("effective_on", createCouponRequest.getEffectiveOn().name().toLowerCase());

        JsonArray idArray = new JsonArray();
        createCouponRequest.getEffectiveIds().forEach(idArray::add);

        if(createCouponRequest.getEffectiveOn() == CreateCouponRequest.EffectiveOn.PACKAGE) {
            payload.add("packages", idArray);
        } else if(createCouponRequest.getEffectiveOn() == CreateCouponRequest.EffectiveOn.CATEGORY) {
            payload.add("categories", idArray);
        } else {
            throw new RuntimeException("Invalid option selected");
        }

        payload.addProperty("discount_type", createCouponRequest.getDiscountType().name().toLowerCase());

        payload.addProperty("discount_percentage", createCouponRequest.getDiscountValue());
        payload.addProperty("discount_amount", createCouponRequest.getDiscountValue());

        payload.addProperty("redeem_unlimited", createCouponRequest.canRedeemUnlimited());
        payload.addProperty("expire_never", ! createCouponRequest.canExpire());

        if(! createCouponRequest.canRedeemUnlimited()) {
            payload.addProperty("expire_limit", createCouponRequest.getExpiryLimit());
        }

        if(createCouponRequest.canExpire()) {
            if(createCouponRequest.getExpiryDate() == null) {
                throw new RuntimeException("Coupon has expiry set to true, but no expiry date exists");
            }
            payload.addProperty("expire_date", createCouponRequest.getExpiryDate().toString());
        }

        payload.addProperty("start_date", createCouponRequest.getStartDate().toString());
        payload.addProperty("basket_type", createCouponRequest.getBasketType().name().toLowerCase());
        payload.addProperty("minimum", createCouponRequest.getMinimum());
        payload.addProperty("discount_application_method", createCouponRequest.getDiscountMethod().getValue());
        payload.addProperty("username", createCouponRequest.getUsername());
        payload.addProperty("note", createCouponRequest.getNote());

        return request("/coupons").withBody(GSON.toJson(payload)).withSecretKey(secretKey).sendAsync().thenApply(response -> {
            if(response.code() == 404) {
                throw new CompletionException(new ServerNotFoundException());
            } else if(response.code() != 200) {
                try {
                    JsonObject jsonObject = GSON.fromJson(response.body().string(), JsonObject.class);
                    if(jsonObject.has("error_message")) {
                        throw new CompletionException(new IOException(jsonObject.get("error_message").getAsString()));
                    }

                    throw new CompletionException(new IOException("Unexpected status code (" + response.code() + ")"));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            try {
                JsonObject jsonObject = GSON.fromJson(response.body().string(), JsonObject.class);
                return Coupon.fromJsonObject(jsonObject.getAsJsonObject("data"));
            } catch (IOException e) {
                platform.sendTriageEvent(e);
                throw new CompletionException(new IOException("Unexpected response"));
            }


        });
    }

    /**
     * Get the store listing.
     *
     * @return A CompletableFuture that contains a List of Category objects.
     */
    public CompletableFuture<List<Category>> getListing() {
        if (getSecretKey() == null) {
            CompletableFuture<List<Category>> future = new CompletableFuture<>();
            future.completeExceptionally(new ServerNotSetupException());
            return future;
        }

        return request("/listing").withSecretKey(secretKey).sendAsync().thenApply(response -> {
            if(response.code() == 404) {
                throw new CompletionException(new ServerNotFoundException());
            } else if(response.code() != 200) {
                try {
                    JsonObject jsonObject = GSON.fromJson(response.body().string(), JsonObject.class);
                    if(jsonObject.has("error_message")) {
                        throw new CompletionException(new IOException(jsonObject.get("error_message").getAsString()));
                    }

                    throw new CompletionException(new IOException("Unexpected status code (" + response.code() + ")"));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            try {
                JsonObject jsonObject = GSON.fromJson(response.body().string(), JsonObject.class);

                return jsonObject.getAsJsonArray("categories")
                        .asList()
                        .stream()
                        .map(category -> Category.fromJsonObject(category.getAsJsonObject()))
                        .collect(Collectors.toList());
            } catch (IOException e) {
                platform.sendTriageEvent(e);
                throw new CompletionException(new IOException("Unexpected response"));
            }
        });
    }

    /**
     * Delete a coupon.
     *
     * @param id The ID of the coupon to delete.
     * @return A CompletableFuture that returns true if the coupon was deleted successfully.
     */
    public CompletableFuture<Boolean> deleteCoupon(int id) {
        if (getSecretKey() == null) {
            CompletableFuture<Boolean> future = new CompletableFuture<>();
            future.completeExceptionally(new ServerNotSetupException());
            return future;
        }

        return request("/coupons/" + id).delete().withSecretKey(secretKey).sendAsync().thenApply(response -> {
            if(response.code() == 404) {
                throw new CompletionException(new ServerNotFoundException());
            } else if(response.code() != 204) {
                throw new CompletionException(new IOException("Unexpected status code (" + response.code() + ")"));
            }

            return true;
        });
    }

    public CompletableFuture<Boolean> sendEvents(List<ServerEvent> events) {
        if (getSecretKey() == null) {
            CompletableFuture<Boolean> future = new CompletableFuture<>();
            future.completeExceptionally(new ServerNotSetupException());
            return future;
        }

        return request("/events").withBody(GSON.toJson(events)).withSecretKey(secretKey).sendAsync().thenApply(response -> {
            if (response.code() == 404) {
                throw new CompletionException(new ServerNotFoundException());
            } else if (response.code() != 204) {
                throw new CompletionException(new IOException("Unexpected status code (" + response.code() + ")"));
            }

            return true;
        });
    }

    /**
     * Get a specific package.
     *
     * @param id The ID of the package to retrieve.
     * @return A CompletableFuture that contains the Package object.
     */
    public CompletableFuture<Package> getPackage(int id) {
        if (getSecretKey() == null) {
            CompletableFuture<Package> future = new CompletableFuture<>();
            future.completeExceptionally(new ServerNotSetupException());
            return future;
        }

        return request("/package/" + id).withSecretKey(secretKey).sendAsync().thenApply(response -> {
            if(response.code() == 404) {
                throw new CompletionException(new ServerNotFoundException());
            } else if(response.code() != 200) {
                throw new CompletionException(new IOException("Unexpected status code (" + response.code() + ")"));
            }

            try {
                return Package.fromJsonObject(GSON.fromJson(response.body().string(), JsonObject.class));
            } catch (IOException e) {
                platform.sendTriageEvent(e);
                throw new CompletionException(new IOException("Unexpected response"));
            }
        });
    }

    /**
     * Get all packages.
     *
     * @return A CompletableFuture that returns a List of Package objects.
     */
    public CompletableFuture<List<Package>> getPackages() {
        if (getSecretKey() == null) {
            CompletableFuture<List<Package>> future = new CompletableFuture<>();
            future.completeExceptionally(new ServerNotSetupException());
            return future;
        }

        return request("/packages").withSecretKey(secretKey).sendAsync().thenApply(response -> {
            if(response.code() == 404) {
                throw new CompletionException(new ServerNotFoundException());
            } else if(response.code() != 200) {
                throw new CompletionException(new IOException("Unexpected status code (" + response.code() + ")"));
            }

            try {
                JsonArray jsonObject = GSON.fromJson(response.body().string(), JsonArray.class);
                return jsonObject.asList().stream().map(item -> Package.fromJsonObject(item.getAsJsonObject())).collect(Collectors.toList());
            } catch (IOException e) {
                platform.sendTriageEvent(e);
                throw new CompletionException(new IOException("Unexpected response"));
            }
        });
    }

    /**
     * Sends the current server telemetry to the Analyse API.
     *
     * @return A CompletableFuture that indicates whether the operation was successful.
     */
    public CompletableFuture<Boolean> sendTelemetry() {
        if (getSecretKey() == null) {
            CompletableFuture<Boolean> future = new CompletableFuture<>();
            future.completeExceptionally(new ServerNotSetupException());
            return future;
        }

        Map<String, Object> serverData = new LinkedHashMap<>();
        Map<String, Object> pluginData = new LinkedHashMap<>();

        // Server data
        serverData.put("platform", platform.getType());
        serverData.put("platform_version", platform.getTelemetry().getServerVersion());
        serverData.put("online_mode", platform.isOnlineMode());

        // Plugin data
        pluginData.put("version", platform.getVersion());

        // Combine and send to Tebex
        Map<String, Object> keenData = new LinkedHashMap<>();
        keenData.put("server", serverData);
        keenData.put("plugin", pluginData);

        return request("https://plugin.buycraft.net/analytics/startup", false).withSecretKey(secretKey).withBody(GSON.toJson(keenData)).sendAsync().thenApply(response -> {
            if(response.code() == 404) {
                throw new CompletionException(new ServerNotFoundException());
            } else if(response.code() != 200) {
                throw new CompletionException(new IOException("Unexpected status code (" + response.code() + ")"));
            }

            try {
                JsonObject jsonObject = GSON.fromJson(response.body().string(), JsonObject.class);
                return jsonObject.get("success").getAsBoolean();
            } catch (IOException e) {
                platform.sendTriageEvent(e);
                throw new CompletionException(new IOException("Unexpected response"));
            }
        });
    }

    /**
     * Get the secret key associated with this SDK instance.
     *
     * @return The secret key as a String
     */
    public String getSecretKey() {
        return secretKey;
    }

    /**
     * Set the secret key for this SDK instance.
     *
     * @param secretKey The secret key as a String
     */
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    /**
     * Create a new TebexRequest with the plugin base url.
     *
     * @param url The URL to send the request to
     * @return An TebexRequest instance
     */
    public TebexRequest request(String url) {
        return request(url, true);
    }

    /**
     * Create a new TebexRequest with the specified URL.
     *
     * @param url The base URL to send the request to
     * @param useBaseUrl Whether we prefix with the plugin API base
     * @return An TebexRequest instance
     */
    public TebexRequest request(String url, boolean useBaseUrl) {
        return new TebexRequest(useBaseUrl ? "https://plugin.tebex.io" + url : url, HTTP_CLIENT);
    }
}
