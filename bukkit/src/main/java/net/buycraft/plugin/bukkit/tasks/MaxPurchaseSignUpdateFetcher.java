package net.buycraft.plugin.bukkit.tasks;

import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.bukkit.BuycraftPlugin;
import net.buycraft.plugin.client.ApiException;
import net.buycraft.plugin.data.RecentPayment;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import net.buycraft.plugin.data.MaxPayment;
import net.buycraft.plugin.data.QueuedPlayer;
import net.buycraft.plugin.data.responses.ServerInformation;
import net.buycraft.plugin.shared.config.signs.storage.MaxPurchaseSignPosition;

@RequiredArgsConstructor
public class MaxPurchaseSignUpdateFetcher implements Runnable {
    private final BuycraftPlugin plugin;
    
    private static HashMap<Date, ArrayList<MaxPayment>> cacheLastMax = new HashMap<Date, ArrayList<MaxPayment>>();
    
    @Override
    public void run() {
        // Figure out how many signs we should get
        List<MaxPurchaseSignPosition> signs = plugin.getMaxPurchaseSignStorage().getSigns();
        
        if (plugin.getApiClient() == null) {
            return;
        }

        List<RecentPayment> payments;
        try {
            payments = plugin.getApiClient().getRecentPayments(100);
        } catch (IOException | ApiException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not fetch recent purchases", e);
            return;
        }
        
        // init var
        Map<MaxPurchaseSignPosition, MaxPayment> signToPurchases = new HashMap<>();
        // reset cache
        cacheLastMax = new HashMap<Date, ArrayList<MaxPayment>>();
        
        // for all sign position
        for (MaxPurchaseSignPosition sign : signs) {
            
            // calculate limite
            Calendar calLimite = Calendar.getInstance();
            calLimite.add(Calendar.DATE, -sign.getTime());
            
            ArrayList<MaxPayment> listMaxPayment = getLastMax(payments, calLimite.getTime());
            
            int pos = sign.getPosition()-1;
            if(pos < listMaxPayment.size() && pos >= 0) {
                signToPurchases.put(sign, listMaxPayment.get(pos));
            } else {
                plugin.getLogger().log(Level.WARNING, "Position invalid {0}", pos);
            }
        }

        Bukkit.getScheduler().runTask(plugin, new MaxPurchaseSignUpdateApplication(plugin, signToPurchases));
    }
    
    
    /**
     * Get the last max payement before a specific date
     * 
     * @param payments list of all Recent payment
     * @param limiteDate the limite date
     * @return all payement order by amount
     */
    private static ArrayList<MaxPayment> getLastMax(List<RecentPayment> payments, Date limiteDate) {
        // TODO debug
        if(cacheLastMax.containsKey(limiteDate)) {
            return cacheLastMax.get(limiteDate);
        }
        
        HashMap<QueuedPlayer, MaxPayment> dicMaxPay = new HashMap<QueuedPlayer, MaxPayment>();
        
        for(RecentPayment recPay : payments) {
            float amount = recPay.getAmount().floatValue();
            
            Date payDate = recPay.getDate();
            if(!payDate.after(limiteDate)) {
                continue;
            }
            
            ServerInformation.AccountCurrency currency = recPay.getCurrency();
            QueuedPlayer player = recPay.getPlayer();
            
            if(dicMaxPay.containsKey(player)) {
                MaxPayment maxPay = dicMaxPay.get(player);
                maxPay.setAmount(maxPay.getAmount() + amount);
                maxPay.setNbrpayment(maxPay.getNbrpayment()+1);
            } else {
                dicMaxPay.put(player, new MaxPayment(amount, 1, currency, player));
            }
            
        }
        
        ArrayList<MaxPayment> listMaxPay = new ArrayList<MaxPayment>(dicMaxPay.values());
        
        Collections.sort(listMaxPay, new Comparator<MaxPayment>() {
            @Override
            public int compare(MaxPayment o1, MaxPayment o2) {
                return (o1.getAmount() > o2.getAmount() ? -1 : (o1.getAmount() == o2.getAmount() ? 0 : 1));
            }
        });
        
        // save to cache
        cacheLastMax.put(limiteDate, listMaxPay);
        
        return listMaxPay;
    }
    
    
}
