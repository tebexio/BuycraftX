package net.buycraft.plugin.bukkit.command;

import com.google.common.base.Joiner;
import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.bukkit.BuycraftPlugin;
import net.buycraft.plugin.client.ApiException;
import net.buycraft.plugin.data.Coupon;
import net.buycraft.plugin.shared.util.CouponUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

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
            case "information":
                getCouponInfo(sender, args);
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

        plugin.getPlatform().executeAsync(new Runnable() {
            @Override
            public void run() {
                List<Coupon> couponList;
                try {
                    couponList = plugin.getApiClient().getAllCoupons();
                } catch (IOException | ApiException e) {
                    sender.sendMessage(ChatColor.RED + plugin.getI18n().get("generic_api_operation_error"));
                    return;
                }

                List<String> codes = new ArrayList<>();
                for (Coupon coupon : couponList) {
                    codes.add(coupon.getCode());
                }

                sender.sendMessage(ChatColor.YELLOW + plugin.getI18n().get("coupon_listing_header", Joiner.on(", ").join(codes)));
            }
        });
    }

    private void createCoupon(CommandSender sender, String[] args) {
        String[] stripped = Arrays.copyOfRange(args, 1, args.length);
        Coupon coupon;
        try {
            coupon = CouponUtil.parseArguments(stripped);
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + plugin.getI18n().get("coupon_creation_arg_parse_failure", e.getMessage()));
            return;
        }

        // TODO: Handle creation.
        sender.sendMessage(ChatColor.GREEN + plugin.getI18n().get("coupon_creation_success", coupon.getCode()));
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
                } catch (ApiException | IOException e) {
                    sender.sendMessage(ChatColor.RED + plugin.getI18n().get("generic_api_operation_error"));
                    return;
                }
            }
        });
    }

    private void getCouponInfo(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(ChatColor.RED + plugin.getI18n().get("no_coupon_specified"));
            return;
        }

        // TODO: Handle displaying coupon information.
    }

    @Override
    public String getDescription() {
        return plugin.getI18n().get("usage_coupon");
    }
}
