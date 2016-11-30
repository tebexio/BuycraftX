package net.buycraft.plugin.shared.logging;

import com.bugsnag.delivery.HttpDelivery;
import com.bugsnag.serialization.SerializationException;
import com.bugsnag.serialization.Serializer;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Proxy;

public class OkHttpBugsnagDelivery implements HttpDelivery {
    private static final MediaType JSON = MediaType.parse("application/json");
    protected static final String DEFAULT_ENDPOINT = "https://notify.bugsnag.com";
    protected static final int DEFAULT_TIMEOUT = 5000;

    protected String endpoint = DEFAULT_ENDPOINT;
    protected int timeout = DEFAULT_TIMEOUT;

    private static final Logger logger = LoggerFactory.getLogger(OkHttpBugsnagDelivery.class);
    private final OkHttpClient client;

    public OkHttpBugsnagDelivery(OkHttpClient client) {
        this.client = client;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public void setProxy(Proxy proxy) {
        throw new UnsupportedOperationException();
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public void deliver(Serializer serializer, Object object) {
        // Ugly hack ahead
        ByteArrayOutputStream stream = new ByteArrayOutputStream(1024);
        try {
            serializer.writeToStream(stream, object);
        } catch (SerializationException e) {
            logger.warn("Unable to serialize data to send to Bugsnag", e);
            return;
        }

        client.newCall(new Request.Builder()
                .url(endpoint)
                .cacheControl(CacheControl.FORCE_NETWORK)
                .post(RequestBody.create(JSON, stream.toByteArray()))
                .build())
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        logger.warn("Unable to send data to Bugsnag", e);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() / 100 != 2) {
                            logger.warn(
                                    "Error not reported to Bugsnag - got non-200 response code: {}", response.code());
                        }
                        response.body().close();
                    }
                });
    }
}
