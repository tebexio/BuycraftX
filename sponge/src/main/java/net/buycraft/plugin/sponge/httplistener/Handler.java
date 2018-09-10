package net.buycraft.plugin.sponge.httplistener;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import net.buycraft.plugin.data.QueuedCommand;
import net.buycraft.plugin.data.QueuedPlayer;
import net.buycraft.plugin.execution.strategy.ToRunQueuedCommand;
import net.buycraft.plugin.sponge.BuycraftPlugin;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Handler implements HttpHandler {

    private BuycraftPlugin plugin;

    public Handler(BuycraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        Object[] response = this.handleRequest(httpExchange);

        httpExchange.sendResponseHeaders(Integer.parseInt(response[0].toString()), String.valueOf(response[1]).getBytes().length);
        OutputStream os = httpExchange.getResponseBody();
        os.write(String.valueOf(response[1]).getBytes());
        os.flush();
        os.close();
        httpExchange.close();
    }


    private Object[] handleRequest(HttpExchange ex) {
        if (ex.getRequestURI().toString().equalsIgnoreCase("/ping")) {
            return new Object[]{200, "Connected"};
        } else {
            try {
                String body = IOUtils.toString(ex.getRequestBody(), Charsets.UTF_8);
                String hash = Hashing.sha256().hashString(body.concat(plugin.getConfiguration().getServerKey()), Charsets.UTF_8).toString();

                if(!ex.getRequestHeaders().containsKey("X-Signature")) {
                    return new Object[]{422, "X-Signature header missing"};
                }

                if (hash.equals(ex.getRequestHeaders().get("X-Signature").get(0))) {
                    JsonArray jsonBody;
                    try {
                        jsonBody = new JsonParser().parse(body).getAsJsonArray();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return new Object[]{422, "Invalid JSON: " + e.getMessage()};
                    }

                    if (jsonBody != null) {
                        return pushCommand(jsonBody);
                    }
                } else {
                    return new Object[]{422, "Invalid signature"};
                }

            } catch (Exception e) {
                e.printStackTrace();
                return new Object[]{422, "Error: " + e.getMessage()};
            }
        }
        return new Object[]{422, "Error"};
    }


    private Object[] pushCommand(JsonArray jsonBody) {
        int playerId = 0;

        for (JsonElement command : jsonBody) {
            if (command instanceof JsonObject) {
                JsonObject commandObject = ((JsonObject) command).getAsJsonObject();

                QueuedPlayer qp = new QueuedPlayer(playerId,
                        commandObject.get("username_name").getAsString(),
                        commandObject.get("username").getAsString().replace("-", ""));


                Map<String, Integer> map = new ConcurrentHashMap<String, Integer>();
                map.put("delay", commandObject.get("delay").getAsInt());

                if (commandObject.get("require_slots").getAsInt() > 0) {
                    map.put("slots", commandObject.get("require_slots").getAsInt());
                }

                int packageId = 0;

                if (commandObject.has("package") && !commandObject.get("package").isJsonNull()) {
                    packageId = commandObject.get("package").getAsInt();
                }

                QueuedCommand qc = new QueuedCommand(commandObject.get("id").getAsInt(),
                        commandObject.get("payment").getAsInt(),
                        packageId,
                        map,
                        commandObject.get("command").getAsString(),
                        qp);


                plugin.getCommandExecutor().queue(new ToRunQueuedCommand(qp, qc, commandObject.get("require_online").getAsInt() == 1 ? true : false));

                playerId += 1;
            }
        }

        return new Object[]{200, "Commands executed"};
    }
}
