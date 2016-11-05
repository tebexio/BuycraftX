package net.buycraft.plugin.util;

import com.bugsnag.Report;
import com.bugsnag.callbacks.Callback;
import net.buycraft.plugin.client.ApiException;

public class BuycraftBeforeNotify implements Callback {
    @Override
    public void beforeNotify(Report report) {
        if (report.getException() instanceof ApiException) {
            ApiException exception = (ApiException) report.getException();
            if (exception.getSentRequest() != null) {
                report.addToTab("http", "requestSent", exception.getSentRequest().toString());
            }
            if (exception.getReceivedResponse() != null) {
                report.addToTab("http", "receivedResponse", exception.getReceivedResponse().toString());
                report.addToTab("http", "receivedHeaders", exception.getReceivedResponse().headers().toString());
            }
            if (exception.getResponseBody() != null) {
                report.addToTab("http", "receivedBody", exception.getResponseBody());
            }
        }
    }
}
