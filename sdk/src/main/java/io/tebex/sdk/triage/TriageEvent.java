package io.tebex.sdk.triage;

import com.google.gson.Gson;
import io.tebex.sdk.platform.Platform;
import io.tebex.sdk.request.TebexRequest;
import io.tebex.sdk.request.response.ServerInformation;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * A TriageEvent is an indicator of a potential problem that is automatically reported to Tebex.
 *   Information about the Platform at runtime is provided as well as an error message and/or trace
 *   of the error if applicable.
 */
public class TriageEvent {
    private final Platform _platform;

    private String gameId;
    private String frameworkId;
    private String pluginVersion;
    private String serverIp;
    private String errorMessage;
    private String trace;
    private Map<String, String> metadata;
    private String storeName;
    private String storeUrl;

    private TriageEvent(Platform platform){
        this._platform = platform;
    }

    public static TriageEvent fromPlatform(Platform platform) {
        TriageEvent event = new TriageEvent(platform);

        event.gameId = platform.getTelemetry().getServerSoftware();
        event.frameworkId = platform.getTelemetry().getServerSoftware()
                + " " + platform.getTelemetry().getServerVersion()
                + " " + platform.getTelemetry().getJavaVersion();
        event.pluginVersion = platform.getTelemetry().getPluginVersion();

        event.serverIp = ""; //TODO

        // Assign store info if secret key is set
        if (platform.getSDK().getSecretKey() != null) {
            ServerInformation serverInfo;
            try {
                serverInfo = platform.getSDK().getServerInformation().get();
                event.storeName = serverInfo.getStore().getName();
                event.storeUrl = serverInfo.getStore().getDomain();
            } catch (InterruptedException | ExecutionException e) {
                // store name and info will remain unfilled
            }
        }

        return event;
    }

    public TriageEvent withGameId(String value) {
        this.gameId = value;
        return this;
    }

    public TriageEvent withFrameworkId(String value) {
        this.frameworkId = value;
        return this;
    }

    public TriageEvent withPluginVersion(String value) {
        this.pluginVersion = value;
        return this;
    }

    public TriageEvent withServerIp(String value) {
        this.serverIp = value;
        return this;
    }

    public TriageEvent withErrorMessage(String value) {
        this.errorMessage = value;
        return this;
    }

    public TriageEvent withTrace(String value) {
        this.trace = trace;
        return this;
    }

    public TriageEvent withMetadata(HashMap<String, String> value) {
        this.metadata = value;
        return this;
    }

    public TriageEvent withStoreName(String value) {
        this.storeName = value;
        return this;
    }

    public TriageEvent withStoreUrl(String value) {
        this.storeUrl = value;
        return this;
    }

    public String toJsonString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static TriageEvent fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, TriageEvent.class);
    }

    public void send() {
        TebexRequest triageEventRequest = _platform.getSDK().request("https://plugin-logs.tebex.io/", false)
                 .withBody(this.toJsonString(), "POST");

        // Store name is set automatically by fromPlatform
        if (this.storeName.equals("")) {
            _platform.debug("No store info while sending triage event, ignoring event");
            return;
        }

        // Send the event to plugin logs
        try {
            Response triageResponse = triageEventRequest.send();

            if (!triageResponse.isSuccessful()) {
                _platform.debug("Failed to send triage event!");
                ResponseBody responseBody = triageResponse.body();

                if (responseBody != null) {
                    _platform.debug(responseBody.string());
                } else {
                    _platform.debug("Empty response from plugin logs when sending triage event");
                }
            }

            triageResponse.close();
        } catch (IOException e) {
            _platform.debug("Unexpected error sending triage event!");
            _platform.debug(e.getMessage());
        }
    }
}
