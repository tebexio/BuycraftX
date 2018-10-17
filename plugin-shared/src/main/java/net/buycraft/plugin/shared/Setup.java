package net.buycraft.plugin.shared;


import com.google.common.base.Supplier;
import lombok.experimental.UtilityClass;
import net.buycraft.plugin.data.responses.ServerInformation;
import net.buycraft.plugin.shared.util.FakeProxySelector;
import net.buycraft.plugin.shared.util.Ipv4PreferDns;
import okhttp3.Cache;
import okhttp3.OkHttpClient;

import java.io.File;
import java.net.ProxySelector;
import java.util.concurrent.TimeUnit;

@UtilityClass
public class Setup {


    public static OkHttpClient okhttp(File base) {
        return okhttpBuilder()
                .cache(new Cache(new File(base, "cache"), 1024 * 1024 * 10))
                .build();
    }

    public static OkHttpClient.Builder okhttpBuilder() {
        return new OkHttpClient.Builder()
                .connectTimeout(3, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .dns(new Ipv4PreferDns())
                .proxySelector(ProxySelector.getDefault() == null ? FakeProxySelector.INSTANCE : ProxySelector.getDefault());
    }
}
