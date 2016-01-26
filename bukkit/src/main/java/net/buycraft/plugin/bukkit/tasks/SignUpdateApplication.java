package net.buycraft.plugin.bukkit.tasks;

import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.bukkit.BuycraftPlugin;
import net.buycraft.plugin.bukkit.signs.PurchaseSignPosition;
import net.buycraft.plugin.data.RecentPayment;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;
import java.util.Map;

@RequiredArgsConstructor
public class SignUpdateApplication implements Runnable {
    private final BuycraftPlugin plugin;
    private final Map<PurchaseSignPosition, RecentPayment> signToPurchases;

    @Override
    public void run() {
        for (Map.Entry<PurchaseSignPosition, RecentPayment> entry : signToPurchases.entrySet()) {
            Block block = entry.getKey().getLocation().toBukkitLocation().getBlock();
            if (block.getType() == Material.SIGN_POST || block.getType() == Material.WALL_SIGN) {
                Sign sign = (Sign) block.getState();
                sign.setLine(1, entry.getValue().getPlayer().getName());

                // TODO: Make this better!
                NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
                format.setCurrency(Currency.getInstance(entry.getValue().getCurrency().getIso4217()));

                sign.setLine(2, NumberFormat.getCurrencyInstance().format(entry.getValue().getAmount().doubleValue()));
            } else {
                // TODO: Help the user by cleaning it up.
                plugin.getLogger().warning("Location " + entry.getKey().getLocation() + " doesn't have a sign!");
            }
        }
    }
}
