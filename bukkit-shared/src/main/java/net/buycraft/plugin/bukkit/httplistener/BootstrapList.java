package net.buycraft.plugin.bukkit.httplistener;

import com.google.common.collect.ForwardingList;
import com.google.common.collect.Lists;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;

import java.util.Collection;
import java.util.List;

class BootstrapList extends ForwardingList<Object> {
    private List<Object> delegate;
    private ChannelHandler handler;

    /**
     * Construct a new bootstrap list.
     *
     * @param delegate - the delegate.
     * @param handler  - the channel handler to add.
     */
    public BootstrapList(List<Object> delegate, ChannelHandler handler) {
        this.delegate = delegate;
        this.handler = handler;

        // Process all existing bootstraps
        for (Object item : this) {
            processElement(item);
        }
    }

    @Override
    public synchronized boolean add(Object element) {
        processElement(element);
        return delegate.add(element);
    }

    @Override
    public synchronized boolean addAll(Collection<? extends Object> collection) {
        List<Object> copy = Lists.newArrayList(collection);

        // Process the collection before we pass it on
        for (Object element : copy) {
            processElement(element);
        }
        return delegate.addAll(copy);
    }

    @Override
    public synchronized Object set(int index, Object element) {
        Object old = delegate.set(index, element);

        // Handle the old future, and the newly inserted future
        if (old != element) {
            unprocessElement(old);
            processElement(element);
        }
        return old;
    }

    /**
     * Process a single element.
     *
     * @param element - the element.
     */
    protected void processElement(Object element) {
        if (element instanceof ChannelFuture) {
            processBootstrap((ChannelFuture) element);
        }
    }

    /**
     * Unprocess a single element.
     *
     * @param element - the element to unprocess.
     */
    protected void unprocessElement(Object element) {
        if (element instanceof ChannelFuture) {
            unprocessBootstrap((ChannelFuture) element);
        }
    }

    /**
     * Process a single channel future.
     *
     * @param future - the future.
     */
    protected void processBootstrap(ChannelFuture future) {
        // Important: Must be addFirst()
        future.channel().pipeline().addFirst(handler);
    }

    /**
     * Revert any changes we made to the channel future.
     *
     * @param future - the future.
     */
    protected void unprocessBootstrap(ChannelFuture future) {
        final Channel channel = future.channel();

        // For thread safety - see ChannelInjector.close()
        channel.eventLoop().submit(() -> {
            channel.pipeline().remove(handler);
            return null;
        });
    }

    /**
     * Close and revert all changes.
     */
    public synchronized void close() {
        for (Object element : this)
            unprocessElement(element);
    }

    @Override
    protected List<Object> delegate() {
        return delegate;
    }
}