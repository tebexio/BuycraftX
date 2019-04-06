package net.buycraft.plugin.data.responses;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BuycraftError that = (BuycraftError) o;

        if (errorCode != that.errorCode) return false;
        return Objects.equals(errorMessage, that.errorMessage);

    }

    @Override
    public int hashCode() {
        int result = errorCode;
        result = 31 * result + (errorMessage != null ? errorMessage.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "BuycraftError(errorCode=" + this.getErrorCode() + ", errorMessage=" + this.getErrorMessage() + ")";
    }
}
