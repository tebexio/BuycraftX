package net.buycraft.plugin.shared.logging;

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
                try {
                    exception.getReceivedResponse().close();
                } catch (Exception e) {
                    // not much we can do... gotta catch them all, I guess
                }
            }
            if (exception.getResponseBody() != null) {
                report.addToTab("http", "receivedBody", exception.getResponseBody());
            }
        }
    }
}
