package net.buycraft.plugin.bungeecord.httplistener;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.google.gson.*;
import com.sun.org.apache.xpath.internal.operations.Bool;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import net.buycraft.plugin.bungeecord.BuycraftPlugin;
import net.buycraft.plugin.data.QueuedCommand;
import net.buycraft.plugin.data.QueuedPlayer;
import net.buycraft.plugin.execution.strategy.ToRunQueuedCommand;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

class Handler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private JsonArray body;
    private BuycraftPlugin plugin;

    public Handler(BuycraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.valueOf(422));
        ChannelPromise promise = ctx.channel().newPromise();

        if (request.getUri().equalsIgnoreCase("/ping")) {
            response.content().writeBytes(Charsets.UTF_8.encode("Connection Established"));
            response.setStatus(HttpResponseStatus.valueOf(200));
        } else {
            String body = request.content().toString(Charsets.UTF_8);
            String hash = Hashing.sha256().hashString(body.concat(plugin.getConfiguration().getServerKey()), Charsets.UTF_8).toString();
            if (hash.equals(request.headers().get("X-Signature"))) {
                try {
                    this.body = new JsonParser().parse(body).getAsJsonArray();
                } catch (Exception e) {
                    response.content().writeBytes(Charsets.UTF_8.encode("Invalid JSON"));
                    response.setStatus(HttpResponseStatus.valueOf(422));
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
        int playerId = 0;

        for (JsonElement command : this.body) {
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

                if(commandObject.has("package") && !commandObject.get("package").isJsonNull()){
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