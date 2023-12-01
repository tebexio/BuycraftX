package io.tebex.sdk.obj;

import java.util.Date;

public class CheckoutUrl {
    private final String url;
    private final Date expires;

    public CheckoutUrl(String url, Date expires) {
        this.url = url;
        this.expires = expires;
    }

    public String getUrl() {
        return url;
    }

    public Date getExpiry() {
        return expires;
    }

    @Override
    public String toString() {
        return "CheckoutUrl{" +
                "url='" + url + '\'' +
                ", expires=" + expires +
                '}';
    }
}
