package net.buycraft.plugin.bukkit.httplistener;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import net.buycraft.plugin.bukkit.BuycraftPlugin;
import net.buycraft.plugin.data.QueuedCommand;
import net.buycraft.plugin.data.QueuedPlayer;
import net.buycraft.plugin.execution.strategy.ToRunQueuedCommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

class Handler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private JsonObject body;
    private BuycraftPlugin plugin;

    public Handler(BuycraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.valueOf(422));
        ChannelPromise promise = ctx.channel().newPromise();

        String body = request.content().toString(Charset.defaultCharset());

        String hash = Hashing.sha256().hashString(body.concat(plugin.getConfiguration().getServerKey()), Charsets.UTF_8).toString();
        if (hash.equals(request.headers().get("X-Signature"))) {
            try {
                this.body = new JsonParser().parse(body).getAsJsonObject();
            } catch (Exception e) {
                response.content().writeBytes(Charsets.UTF_8.encode("Invalid JSON"));
                this.body = null;
            }

            if (this.body != null) {
                Object[] pc = pushCommand();
                response.content().writeBytes(Charsets.UTF_8.encode(String.valueOf(pc[1])));
                response.setStatus(HttpResponseStatus.valueOf(Integer.valueOf(pc[0].toString())));
            }
        } else {
            response.content().writeBytes(Charsets.UTF_8.encode("Invalid signature"));
            response.setStatus(HttpResponseStatus.valueOf(422));
        }

        ctx.channel().writeAndFlush(response, promise);
        promise.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                future.channel().close();
            }
        });
    }

    private Object[] pushCommand() {
        if (!body.has("uuid") || !body.has("commands") || body.get("commands").getAsJsonArray().size() == 0) {
            return new Object[]{422, "Invalid JSON format"};
        }

        QueuedPlayer qp = new QueuedPlayer(body.get("id").getAsInt(),
                body.get("name").getAsString(),
                body.get("uuid").getAsString().replace("-", ""));

        Player p = Bukkit.getPlayer(UUID.fromString(body.get("uuid").getAsString()));
        if ((p == null || p.isOnline() == false) && body.get("require_online").getAsInt() == 1) {
            return new Object[]{422, "Player is not online"};
        }

        if (plugin.getPlatform().getFreeSlots(qp) < body.get("conditions").getAsJsonObject().get("slots").getAsInt()) {
            return new Object[]{422, "Player doesn't have enough slots"};
        }

        JsonArray commands = body.get("commands").getAsJsonArray();
        int ci = 0;
        for (JsonElement command : commands) {
            Map<String, Integer> map = new ConcurrentHashMap<String, Integer>();
            map.put("delay", body.get("conditions").getAsJsonObject().get("delay").getAsInt());
            if(body.get("conditions").getAsJsonObject().get("slots").getAsInt() > 0) {
                map.put("slots", body.get("conditions").getAsJsonObject().get("slots").getAsInt());
            }
            QueuedCommand qc = new QueuedCommand(ci,
                    body.get("payment").getAsInt(),
                    body.get("package").getAsInt(),
                    map,
                    command.getAsString(),
                    qp);
            plugin.getPlatform().getExecutor().queue(new ToRunQueuedCommand(qp, qc, body.get("require_online").getAsInt() == 1 ? true : false));
            ci += 1;
        }
        return new Object[]{200, "Commands executed"};
    }

    private boolean validateRequest() {
        if (!body.has("id")) {
            return false;
        }
        if (!body.has("uuid")) {
            return false;
        }
        if (!body.has("name")) {
            return false;
        }
        if (!body.has("commands")) {
            return false;
        }
        if (!body.has("require_online")) {
            return false;
        }
        if (!body.has("payment")) {
            return false;
        }
        if (!body.has("package")) {
            return false;
        }
        if (!body.has("conditions")) {
            return false;
        }
        if (body.get("require_online").getAsInt() != 0 && body.get("require_online").getAsInt() != 1) {
            return false;
        }
        if (!body.get("commands").isJsonArray()) {
            return false;
        }
        if (body.get("commands").getAsJsonArray().size() == 0) {
            return false;
        }
        if (!body.get("conditions").isJsonObject()) {
            return false;
        }
        for(JsonElement command : body.get("commands").getAsJsonArray()){
            try{
                command.getAsString();
            }catch(Throwable e){
                return false;
            }
        }
        JsonObject conditions = body.get("conditions").getAsJsonObject();
        if(!conditions.has("delay")){
            return false;
        }
        if(!conditions.has("slots")){
            return false;
        }
        if (!Pattern.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}", body.get("uuid").getAsString())) {
            return false;
        }
        return true;
    }
}