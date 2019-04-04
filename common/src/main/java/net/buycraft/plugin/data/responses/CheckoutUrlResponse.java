package net.buycraft.plugin.data.responses;

public final class CheckoutUrlResponse {
    private final String url;

    public CheckoutUrlResponse(final String url) {
        this.url = url;
    }

    public String getUrl() {
        return this.url;
    }

    @Override
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof CheckoutUrlResponse)) return false;
        final CheckoutUrlResponse other = (CheckoutUrlResponse) o;
        final java.lang.Object this$url = this.getUrl();
        final java.lang.Object other$url = other.getUrl();
        if (this$url == null ? other$url != null : !this$url.equals(other$url)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final java.lang.Object $url = this.getUrl();
        result = result * PRIME + ($url == null ? 43 : $url.hashCode());
        return result;
    }

    @Override
    public java.lang.String toString() {
        return "CheckoutUrlResponse(url=" + this.getUrl() + ")";
    }
}
