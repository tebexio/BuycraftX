package net.buycraft.plugin.data.responses;

import com.google.gson.annotations.SerializedName;

public final class BuycraftError {
    @SerializedName("error_code")
    private final int errorCode;
    @SerializedName("error_message")
    private final String errorMessage;

    public BuycraftError(final int errorCode, final String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public int getErrorCode() {
        return this.errorCode;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    @Override
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof BuycraftError)) return false;
        final BuycraftError other = (BuycraftError) o;
        if (this.getErrorCode() != other.getErrorCode()) return false;
        final java.lang.Object this$errorMessage = this.getErrorMessage();
        final java.lang.Object other$errorMessage = other.getErrorMessage();
        if (this$errorMessage == null ? other$errorMessage != null : !this$errorMessage.equals(other$errorMessage))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getErrorCode();
        final java.lang.Object $errorMessage = this.getErrorMessage();
        result = result * PRIME + ($errorMessage == null ? 43 : $errorMessage.hashCode());
        return result;
    }

    @Override
    public java.lang.String toString() {
        return "BuycraftError(errorCode=" + this.getErrorCode() + ", errorMessage=" + this.getErrorMessage() + ")";
    }
}
