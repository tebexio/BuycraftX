package net.buycraft.plugin.bukkit.command;

import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.bukkit.BuycraftPlugin;
import net.buycraft.plugin.client.ApiException;
import net.buycraft.plugin.data.Coupon;
import net.buycraft.plugin.shared.util.CouponUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.util.Arrays;

@RequiredArgsConstructor
public class CouponSubcommand implements Subcommand {
    private static final int COUPON_PAGE_LIMIT = 10;

    private final BuycraftPlugin plugin;

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + plugin.getI18n().get("usage_coupon_subcommands"));
            return;
        }

        switch (args[0]) {
            case "create":
                createCoupon(sender, args);
                break;
            case "delete":
                deleteCoupon(sender, args);
                break;
            default:
                sender.sendMessage(ChatColor.RED + plugin.getI18n().get("usage_coupon_subcommands"));
                break;
        }
    }

    private void createCoupon(final CommandSender sender, String[] args) {
        String[] stripped = Arrays.copyOfRange(args, 1, args.length);
        final Coupon coupon;
        try {
            coupon = CouponUtil.parseArguments(stripped);
        } catch (Exception e) {
            sender.sendMessage(e.getMessage());
            sender.sendMessage(ChatColor.RED + plugin.getI18n().get("coupon_creation_arg_parse_failure", e.getMessage()));
            return;
        }

        plugin.getPlatform().executeAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    plugin.getApiClient().createCoupon(coupon);
                    sender.sendMessage(ChatColor.GREEN + plugin.getI18n().get("coupon_creation_success", coupon.getCode()));
                } catch (ApiException | IOException e) {
                    sender.sendMessage(ChatColor.RED + e.getMessage());

                }
            }
        });
    }

    private void deleteCoupon(final CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(ChatColor.RED + plugin.getI18n().get("no_coupon_specified"));
            return;
        }

        final String code = args[1];

        plugin.getPlatform().executeAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    plugin.getApiClient().deleteCoupon(code);
                    sender.sendMessage(ChatColor.GREEN + plugin.getI18n().get("coupon_deleted"));
                } catch (ApiException | IOException e) {
                    sender.sendMessage(ChatColor.RED + e.getMessage());
                    return;
                }
            }
        });
    }

    @Override
    public String getDescription() {
        return plugin.getI18n().get("usage_coupon");
    }
}
