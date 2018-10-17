package net.buycraft.plugin.bukkit.httplistener;
import java.nio.charset.Charset;
import java.util.List;
import java.util.NoSuchElementException;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import net.buycraft.plugin.bukkit.BuycraftPlugin;

public class Decoder extends ByteToMessageDecoder {

    BuycraftPlugin plugin;

    public Decoder(BuycraftPlugin plugin){
        this.plugin = plugin;
    }
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> list) throws Exception {
        // use 4 bytes to detect HTTP or abort
        if (buf.readableBytes() < 4) {
            return;
        }

        buf.retain(2);

        ChannelPipeline p = ctx.channel().pipeline();

        String verb = buf.toString(Charset.defaultCharset()).substring(0,4);

        if (verb.equals("POST")) {
            ByteBuf copy = buf.copy();
            ctx.channel().config().setOption(ChannelOption.TCP_NODELAY, true);

            try {
                while (p.removeLast() != null);
            } catch (NoSuchElementException e) {

            }

            p.addLast("decoder", new HttpRequestDecoder());
            p.addLast("encoder", new HttpResponseEncoder());
            p.addLast("aggregator", new HttpObjectAggregator(1048576));
            p.addLast("handler", new Handler(this.plugin));

            p.fireChannelRead(copy);
            buf.release();
            buf.release();
        } else {
            try {
                p.remove(this);
            } catch (NoSuchElementException e) {
                // probably okay, it just needs to be off
                System.out.println("NoSuchElementException");
            }

            buf.release();
            buf.release();
        }
    }

}