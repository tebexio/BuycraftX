package net.buycraft.plugin.client;

import net.buycraft.plugin.data.Coupon;
import net.buycraft.plugin.data.GiftCard;
import net.buycraft.plugin.data.RecentPayment;
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

    CheckoutUrlResponse getCategoryUri(String username, int categoryId) throws IOException, ApiException;

    List<RecentPayment> getRecentPayments(int limit) throws IOException, ApiException;

    List<Coupon> getAllCoupons() throws IOException, ApiException;

    Coupon getCoupon(int id) throws IOException, ApiException;

    void deleteCoupon(int id) throws IOException, ApiException;

    void deleteCoupon(String id) throws IOException, ApiException;

    Coupon createCoupon(Coupon coupon) throws IOException, ApiException;

    List<GiftCard> getAllGiftCards() throws IOException, ApiException;

    GiftCard createGiftCard(String amount, String note) throws IOException, ApiException;

    GiftCard getGiftCard(int id) throws IOException, ApiException;

    GiftCard topOffGiftCard(int id, String amount)  throws IOException, ApiException;

    GiftCard voidGiftCard(int id) throws IOException, ApiException;
}
