package net.buycraft.plugin.shared;

import com.bugsnag.Bugsnag;
import com.bugsnag.Report;
import com.bugsnag.callbacks.Callback;
import com.google.common.base.Supplier;
import lombok.experimental.UtilityClass;
import net.buycraft.plugin.data.responses.ServerInformation;
import net.buycraft.plugin.shared.logging.BuycraftBeforeNotify;
import net.buycraft.plugin.shared.logging.OkHttpBugsnagDelivery;
import net.buycraft.plugin.shared.util.FakeProxySelector;
import net.buycraft.plugin.shared.util.Ipv4PreferDns;
import okhttp3.Cache;
import okhttp3.OkHttpClient;

import java.io.File;
import java.net.ProxySelector;
import java.util.concurrent.TimeUnit;

@UtilityClass
public class Setup {
    public static Bugsnag bugsnagClient(OkHttpClient client, String platform, String version, final String serverVersion,
                                        final Supplier<ServerInformation> serverInformation) {
        Bugsnag bugsnag = new Bugsnag("cac4ea0fdbe89b5004d8ab8d5409e594", false);
        bugsnag.setDelivery(new OkHttpBugsnagDelivery(client));
        bugsnag.setAppVersion(version);
        bugsnag.setProjectPackages("net.buycraft.plugin");
        bugsnag.addCallback(new BuycraftBeforeNotify());
        bugsnag.setAppType(platform);
        bugsnag.addCallback(new Callback() {
            @Override
            public void beforeNotify(Report report) {
                report.setAppInfo("serverVersion", serverVersion);
                ServerInformation information = serverInformation.get();
                if (information != null) {
                    report.addToTab("user", "account_id", information.getAccount().getId());
                    report.addToTab("user", "server_id", information.getServer().getId());
                }
            }
        });
        return bugsnag;
    }

    public static OkHttpClient okhttp(File base) {
        return okhttpBuilder()
                .cache(new Cache(new File(base, "cache"), 1024 * 1024 * 10))
                .build();
    }

    public static OkHttpClient.Builder okhttpBuilder() {
        return new OkHttpClient.Builder()
                .connectTimeout(3, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .dns(new Ipv4PreferDns())
                .proxySelector(ProxySelector.getDefault() == null ? FakeProxySelector.INSTANCE : ProxySelector.getDefault());
    }
}
