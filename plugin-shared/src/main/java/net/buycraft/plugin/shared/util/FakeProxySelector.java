package net.buycraft.plugin.shared.util;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FakeProxySelector extends ProxySelector {
    public static final ProxySelector INSTANCE = new FakeProxySelector();
    private static final List<Proxy> SIMPLE_PROXY_LIST = ImmutableList.of(Proxy.NO_PROXY);

    @Override
    public List<Proxy> select(URI uri) {
        return SIMPLE_PROXY_LIST;
    }

    @Override
    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {

    }
}
