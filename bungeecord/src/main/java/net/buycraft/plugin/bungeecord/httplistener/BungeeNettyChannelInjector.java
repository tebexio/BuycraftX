package net.buycraft.plugin.bungeecord.httplistener;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import net.buycraft.plugin.bungeecord.BuycraftPlugin;
import net.md_5.bungee.netty.PipelineUtils;
import net.md_5.bungee.protocol.Varint21LengthFieldPrepender;

public class BungeeNettyChannelInjector extends Varint21LengthFieldPrepender {
    private BuycraftPlugin plugin;

    public BungeeNettyChannelInjector(BuycraftPlugin plugin) {
        this.plugin = plugin;
    }

    public static void inject(BuycraftPlugin plugin) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        ReflectionUtils.setStaticFinalField(PipelineUtils.class.getDeclaredField("framePrepender"), new BungeeNettyChannelInjector(plugin));
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
        ctx.channel().pipeline().addFirst(new ChannelInitializerEntryPoint(this.plugin));
    }

    private static class ChannelInitializerEntryPoint extends ChannelInitializer {
        private BuycraftPlugin plugin;

        public ChannelInitializerEntryPoint(BuycraftPlugin plugin) {
            this.plugin = plugin;
        }

        @Override
        protected void initChannel(Channel channel) throws Exception {
            channel.pipeline().addFirst(new Decoder(this.plugin));
        }
    }
}