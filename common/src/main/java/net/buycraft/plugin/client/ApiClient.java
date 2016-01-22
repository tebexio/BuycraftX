package net.buycraft.plugin.client;

import net.buycraft.plugin.data.responses.*;

import java.io.IOException;
import java.util.List;

public interface ApiClient {
    ServerInformation getServerInformation() throws IOException, ApiException;

    Listing retrieveListing() throws IOException, ApiException;

    QueueInformation retrieveOfflineQueue() throws IOException, ApiException;

    DueQueueInformation retrieveDueQueue() throws IOException, ApiException;

    QueueInformation getPlayerQueue(int id) throws IOException, ApiException;

    void deleteCommand(List<Integer> ids) throws IOException, ApiException;

    CheckoutUrlResponse getCheckoutUri(String username, int packageId) throws IOException, ApiException;
}
