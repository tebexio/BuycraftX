package net.buycraft.plugin.bungeecord.command;

import com.google.common.base.Joiner;
import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.bungeecord.BuycraftPlugin;
import net.buycraft.plugin.client.ApiException;
import net.buycraft.plugin.data.Coupon;
import net.buycraft.plugin.shared.util.CouponUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
            case "list":
                listCoupons(sender, args);
                break;
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

    private void listCoupons(final CommandSender sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + plugin.getI18n().get("no_params"));
            return;
        }

        List<Coupon> couponList = plugin.getCouponUpdateTask().getListing();

        List<String> codes = new ArrayList<>();
        for (Coupon coupon : couponList) {
            codes.add(coupon.getCode());
        }

        sender.sendMessage(ChatColor.YELLOW + plugin.getI18n().get("coupon_listing", Joiner.on(", ").join(codes)));
    }

    private void createCoupon(final CommandSender sender, String[] args) {
        String[] stripped = Arrays.copyOfRange(args, 1, args.length);
        final Coupon coupon;
        try {
            coupon = CouponUtil.parseArguments(stripped);
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + plugin.getI18n().get("coupon_creation_arg_parse_failure", e.getMessage()));
            return;
        }

        plugin.getPlatform().executeAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    plugin.getApiClient().createCoupon(coupon);
                    sender.sendMessage(ChatColor.GREEN + plugin.getI18n().get("coupon_creation_success", coupon.getCode()));

                    plugin.getPlatform().executeAsync(plugin.getCouponUpdateTask());
                } catch (ApiException | IOException e) {
                    sender.sendMessage(ChatColor.RED + plugin.getI18n().get("generic_api_operation_error"));
                }
            }
        });
    }

    private void deleteCoupon(final CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(ChatColor.RED + plugin.getI18n().get("no_coupon_specified"));
            return;
        }

        final Coupon coupon = plugin.getCouponUpdateTask().getCouponByCode(args[1]);
        if (coupon == null) {
            sender.sendMessage(ChatColor.RED + plugin.getI18n().get("coupon_not_found"));
            return;
        }

        plugin.getPlatform().executeAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    plugin.getApiClient().deleteCoupon(coupon.getId());
                    sender.sendMessage(ChatColor.GREEN + plugin.getI18n().get("coupon_deleted"));

                    plugin.getPlatform().executeAsync(plugin.getCouponUpdateTask());
                } catch (ApiException | IOException e) {
                    sender.sendMessage(ChatColor.RED + plugin.getI18n().get("generic_api_operation_error"));
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
