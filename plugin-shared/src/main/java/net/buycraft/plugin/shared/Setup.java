package net.buycraft.plugin.shared;

import net.buycraft.plugin.shared.util.FakeProxySelector;
import net.buycraft.plugin.shared.util.Ipv4PreferDns;
import okhttp3.Cache;
import okhttp3.OkHttpClient;

import java.io.File;
import java.net.ProxySelector;
import java.util.concurrent.TimeUnit;

public final class Setup {
    private Setup() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static OkHttpClient okhttp(File cacheFolder) {
        return okhttpBuilder().cache(new Cache(cacheFolder, 1024 * 1024 * 10)).build();
    }

    public static OkHttpClient.Builder okhttpBuilder() {
        return new OkHttpClient.Builder()
                .connectTimeout(6, TimeUnit.SECONDS)
                .writeTimeout(7, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .dns(new Ipv4PreferDns())
                .proxySelector(ProxySelector.getDefault() == null ? FakeProxySelector.INSTANCE : ProxySelector.getDefault());
    }
}
