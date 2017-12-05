package net.buycraft.plugin.bungeecord.httplistener;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.org.apache.xpath.internal.operations.Bool;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import net.buycraft.plugin.bungeecord.BuycraftPlugin;
import net.buycraft.plugin.data.QueuedCommand;
import net.buycraft.plugin.data.QueuedPlayer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.nio.charset.Charset;
import java.util.UUID;
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

        String key = plugin.getConfiguration().getServerKey();

        String hash = Hashing.sha256().hashString(body.concat(key), Charsets.UTF_8).toString();
        if (hash.equals(hash)){//request.headers().get("X-Signature"))) {
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
        if (!body.has("uuid")
                || !body.has("commands")
                || body.get("commands").getAsJsonArray().size() == 0
                || !body.has("require_online")
                || !(body.get("require_online").getAsString().equals("0")
        || body.get("require_online").getAsString().equals("1"))) {
            return new Object[]{422, "Invalid JSON format"};
        }

        if (!Pattern.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}", body.get("uuid").getAsString())) {
            return new Object[]{422, "Invalid UUID format"};
        }

        ProxiedPlayer p = plugin.getProxy().getPlayer(UUID.fromString(body.get("uuid").getAsString()));
        if(Boolean.parseBoolean(body.get("require_online").getAsString())) {
            if (p == null || p.isConnected() == false) {
                return new Object[]{422, "Player is not online or invalid UUID"};
            }
        }

        JsonArray commands = body.get("commands").getAsJsonArray();
        for (JsonElement command : commands) {
            QueuedPlayer qp = new QueuedPlayer(0, p.getName(), p.getUniqueId().toString().replace("-", ""));
            QueuedCommand qc = new QueuedCommand(0, 0, 0, null, command.getAsString(), qp);
            plugin.getPlatform().dispatchCommand(plugin.getPlaceholderManager().doReplace(qp, qc));
        }

        return new Object[]{200, "Commands executed"};
    }
}