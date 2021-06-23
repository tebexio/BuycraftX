package net.buycraft.plugin.bukkit.httplistener;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.netty.channel.*;
import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public abstract class NettyInjector {

    private static final String NMS_VERSION;
    private static final String NMS_PACKAGE;

    static {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        NMS_VERSION = packageName.substring(packageName.lastIndexOf(".") + 1);

        if (getNmsVersion() != null && getNmsVersion() >= 17) {
            NMS_PACKAGE = "net.minecraft.server.";
        } else {
            NMS_PACKAGE = "net.minecraft.server." + NMS_VERSION + ".";
        }
    }

    private static Integer getNmsVersion() {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        String nmsVersion = packageName.substring(packageName.lastIndexOf(".") + 1);

        String[] nmsVersionParts = nmsVersion.split("_");

        if (nmsVersionParts.length == 3) {
            return Integer.parseInt(nmsVersionParts[1]);
        }

        return null;
    }

    // The temporary player factory
    private List<Field> bootstrapFields = Lists.newArrayList();
    private Map<Field, List<Object>> oldBootStrapFields = Maps.newHashMap();

    // List of network managers
    private Object serverConnection; // used for restore
    private volatile List<Object> networkManagers;
    private boolean injected;
    private boolean closed;

    /**
     * Inject into the spigot connection class.
     */
    @SuppressWarnings("unchecked")
    public synchronized final void inject() {
        if (injected)
            throw new IllegalStateException("Cannot inject twice.");
        try {
            Class fuzzyServer = Class.forName(NMS_PACKAGE + "MinecraftServer");
            Method serverConnectionMethod = fuzzyServer.getDeclaredMethod("getServerConnection");
            serverConnectionMethod.setAccessible(true);

            // Get the server connection
            Object server = null;
            {
                Method serverInstanceMethod = Arrays.stream(fuzzyServer.getDeclaredMethods())
                        .filter(method -> method.getReturnType() == fuzzyServer && method.getParameterCount() == 0 && Modifier.isStatic(method.getModifiers()))
                        .findFirst()
                        .orElse(null);
                if (serverInstanceMethod != null) {
                    if (!serverInstanceMethod.isAccessible()) serverInstanceMethod.setAccessible(true);
                    server = serverInstanceMethod.invoke(null);
                } else {
                    Field serverInstanceField = fuzzyServer.getDeclaredField("instance");
                    if (serverInstanceField != null && serverInstanceField.getType() == fuzzyServer && Modifier.isStatic(serverInstanceField.getModifiers())) {
                        server = serverInstanceField.get(null);
                    }
                }
                if (server == null) throw new IllegalStateException("Failed to get server!");
            }

            serverConnection = serverConnectionMethod.invoke(server);

            // Handle connected channels
            final ChannelInboundHandler endInitProtocol = new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel channel) throws Exception {
                    try {
                        // This can take a while, so we need to stop the main thread from interfering
                        synchronized (networkManagers) {
                            injectChannel(channel);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            // This is executed before Minecraft's channel handler
            final ChannelInboundHandler beginInitProtocol = new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel channel) throws Exception {
                    // Our only job is to add init protocol
                    channel.pipeline().addLast(endInitProtocol);
                }
            };

            // Add our handler to newly created channels
            final ChannelHandler connectionHandler = new ChannelInboundHandlerAdapter() {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                    Channel channel = (Channel) msg;

                    // Prepare to initialize ths channel
                    channel.pipeline().addFirst(beginInitProtocol);
                    ctx.fireChannelRead(msg);
                }
            };

            // Get the current NetworkMananger list

            Class networkManagerClass;

            if (getNmsVersion() != null && getNmsVersion() >= 17) {
                networkManagerClass = Class.forName("net.minecraft.network.NetworkManager");
            } else {
                networkManagerClass = Class.forName(NMS_PACKAGE + "NetworkManager");
            }

            Field networkManagersField = Arrays.stream(serverConnection.getClass().getDeclaredFields())
                    .filter(field -> field.getType() == List.class && ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0] == networkManagerClass)
                    .findFirst()
                    .orElse(null);
            networkManagersField.setAccessible(true);
            networkManagers = (List<Object>) networkManagersField.get(serverConnection);

            //Inject handler
            for (Field field : getBootstrapFields()) {
                final List<Object> list = (List<Object>) field.get(serverConnection);
                // We don't have to override this list
                if (list == networkManagers) {
                    continue;
                }

                // Synchronize with each list before we attempt to replace them.
                oldBootStrapFields.put(field, (List<Object>) field.get(serverConnection));
                field.set(serverConnection, new BootstrapList(list, connectionHandler));
            }

            injected = true;
        } catch (Exception e) {
            throw new RuntimeException("Unable to inject channel futures.", e);
        }
    }

    /**
     * Invoked when a channel is ready to be injected.
     *
     * @param channel - the channel to inject.
     */
    protected abstract void injectChannel(Channel channel);

    /**
     * Retrieve a list of every field with a list of channel futures.
     *
     * @return List of fields.
     */
    private List<Field> getBootstrapFields() {
        List<Field> result = Lists.newArrayList();

        // Find and (possibly) proxy every list
        for (Field[] fields : new Field[][]{serverConnection.getClass().getFields(), serverConnection.getClass().getDeclaredFields()}) {
            Arrays.stream(fields).peek(field -> field.setAccessible(true)).filter(field -> {
                try {
                    if (field.getType() == List.class) {
                        List<Object> list = (List<Object>) field.get(serverConnection);
                        if (list.size() == 0 || list.get(0) instanceof ChannelFuture) {
                            return true;
                        }
                    }
                    return false;
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }).forEach(result::add);
        }
        return result;
    }

    /**
     * Clean up any remaning injections.
     */
    public synchronized void close() {
        if (!closed) {
            closed = true;

            try {
                for (Field field : bootstrapFields) {
                    Object value = field.get(serverConnection);

                    // Undo the processed channels, if any
                    if (value instanceof BootstrapList) {
                        ((BootstrapList) value).close();
                    }
                    field.set(serverConnection, oldBootStrapFields.get(field));
                }
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
    }
}