package io.tebex.sdk.obj;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Package {
    private final int id;
    private final String name;
    private final String image;
    private final double price;
    private final int expiryLength;
    private final String expiryPeriod;
    private final String type;
    private final Category category;
    private final int globalLimit;
    private final String globalLimitPeriod;
    private final int userLimit;
    private final String userLimitPeriod;
    private final List<Server> servers;
    private final List<Integer> requiredPackages;
    private final boolean requireAny;
    private final boolean createGiftcard;
    private final boolean showUtil;
    private final String itemId;
    private final boolean disabled;
    private final boolean disableQuantity;
    private final boolean customPrice;
    private final boolean chooseServer;
    private final boolean limitExpires;
    private final boolean inheritCommands;
    private final boolean variableGiftcard;

        public Package(int id, String name, String image, double price, int expiryLength, String expiryPeriod, String type, Category category, int globalLimit, String globalLimitPeriod, int userLimit, String userLimitPeriod, List<Server> servers, List<Integer> requiredPackages, boolean requireAny, boolean createGiftcard, boolean showUtil, String itemId, boolean disabled, boolean disableQuantity, boolean customPrice, boolean chooseServer, boolean limitExpires, boolean inheritCommands, boolean variableGiftcard) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.price = price;
        this.expiryLength = expiryLength;
        this.expiryPeriod = expiryPeriod;
        this.type = type;
        this.category = category;
        this.globalLimit = globalLimit;
        this.globalLimitPeriod = globalLimitPeriod;
        this.userLimit = userLimit;
        this.userLimitPeriod = userLimitPeriod;
        this.servers = servers;
        this.requiredPackages = requiredPackages;
        this.requireAny = requireAny;
        this.createGiftcard = createGiftcard;
        this.showUtil = showUtil;
        this.itemId = itemId;
        this.disabled = disabled;
        this.disableQuantity = disableQuantity;
        this.customPrice = customPrice;
        this.chooseServer = chooseServer;
        this.limitExpires = limitExpires;
        this.inheritCommands = inheritCommands;
        this.variableGiftcard = variableGiftcard;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public double getPrice() {
        return price;
    }

    public int getExpiryLength() {
        return expiryLength;
    }

    public String getExpiryPeriod() {
        return expiryPeriod;
    }

    public String getType() {
        return type;
    }

    public Category getCategory() {
        return category;
    }

    public int getGlobalLimit() {
        return globalLimit;
    }

    public String getGlobalLimitPeriod() {
        return globalLimitPeriod;
    }

    public int getUserLimit() {
        return userLimit;
    }

    public String getUserLimitPeriod() {
        return userLimitPeriod;
    }

    public List<Server> getServers() {
        return servers;
    }

    public List<Integer> getRequiredPackages() {
        return requiredPackages;
    }

    public boolean requiresAnyPackages() {
        return requireAny;
    }

    public boolean canCreateGiftcard() {
        return createGiftcard;
    }

    public boolean canShowUtil() {
        return showUtil;
    }

    public String getItemId() {
        return itemId;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public boolean isQuantityDisabled() {
        return disableQuantity;
    }

    public boolean hasCustomPrice() {
        return customPrice;
    }

    public boolean canChooseServer() {
        return chooseServer;
    }

    public boolean doesLimitExpire() {
        return limitExpires;
    }

    public boolean canInheritCommands() {
        return inheritCommands;
    }

    public boolean isVariableGiftcard() {
        return variableGiftcard;
    }

    public static class Category {
        private final int id;
        private final String name;

        public Category(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "Category{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    '}';
        }
    }

    public static class Server {
        private final int id;
        private final String name;

        public Server(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "Server{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    '}';
        }
    }

    public static Package fromJsonObject(JsonObject jsonObject) {
        JsonObject categoryJson = jsonObject.get("category").getAsJsonObject();
        Category category = new Category(
                categoryJson.get("id").getAsInt(),
                categoryJson.get("name").getAsString()
        );

        JsonArray serversJsonArray = jsonObject.get("servers").getAsJsonArray();
        List<Server> servers = new ArrayList<>();
        for(JsonElement serverElement : serversJsonArray) {
            JsonObject serverJson = serverElement.getAsJsonObject();
            Server server = new Server(
                    serverJson.get("id").getAsInt(),
                    serverJson.get("name").getAsString()
            );
            servers.add(server);
        }

        return new Package(
                jsonObject.get("id").getAsInt(),
                jsonObject.get("name").getAsString(),
                jsonObject.get("image").getAsString(),
                jsonObject.get("price").getAsDouble(),
                jsonObject.get("expiry_length").getAsInt(),
                jsonObject.get("expiry_period").getAsString(),
                jsonObject.get("type").getAsString(),
                category,
                jsonObject.get("global_limit").getAsInt(),
                jsonObject.get("global_limit_period").getAsString(),
                jsonObject.get("user_limit").getAsInt(),
                jsonObject.get("user_limit_period").getAsString(),
                servers,
                jsonObject.getAsJsonArray("required_packages").asList().stream().map(JsonElement::getAsInt).collect(Collectors.toList()),
                jsonObject.get("require_any").getAsBoolean(),
                jsonObject.get("create_giftcard").getAsBoolean(),
                jsonObject.get("show_until").getAsBoolean(),
                !jsonObject.get("gui_item").isJsonNull() && !jsonObject.get("gui_item").getAsString().isEmpty() ? jsonObject.get("gui_item").getAsString() : null,
                jsonObject.get("disabled").getAsBoolean(),
                jsonObject.get("disable_quantity").getAsBoolean(),
                jsonObject.get("custom_price").getAsBoolean(),
                jsonObject.get("choose_server").getAsBoolean(),
                jsonObject.get("limit_expires").getAsBoolean(),
                jsonObject.get("inherit_commands").getAsBoolean(),
                jsonObject.get("variable_giftcard").getAsBoolean()
        );
    }

    @Override
    public String toString() {
        return "Package{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", image='" + image + '\'' +
                ", price=" + price +
                ", expiryLength=" + expiryLength +
                ", expiryPeriod='" + expiryPeriod + '\'' +
                ", type='" + type + '\'' +
                ", category=" + category +
                ", globalLimit=" + globalLimit +
                ", globalLimitPeriod='" + globalLimitPeriod + '\'' +
                ", userLimit=" + userLimit +
                ", userLimitPeriod='" + userLimitPeriod + '\'' +
                ", servers=" + servers +
                ", requiredPackages=" + requiredPackages +
                ", requireAny=" + requireAny +
                ", createGiftcard=" + createGiftcard +
                ", showUtil=" + showUtil +
                ", itemId=" + itemId +
                ", disabled=" + disabled +
                ", disableQuantity=" + disableQuantity +
                ", customPrice=" + customPrice +
                ", chooseServer=" + chooseServer +
                ", limitExpires=" + limitExpires +
                ", inheritCommands=" + inheritCommands +
                ", variableGiftcard=" + variableGiftcard +
                '}';
    }
}
