package net.buycraft.plugin.bukkit.command;

import net.buycraft.plugin.bukkit.BuycraftPluginBase;
import net.buycraft.plugin.data.Coupon;
import net.buycraft.plugin.shared.util.CouponUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.util.Arrays;

public class CouponSubcommand implements Subcommand {
    private static final int COUPON_PAGE_LIMIT = 10;

    private final BuycraftPluginBase plugin;

    public CouponSubcommand(final BuycraftPluginBase plugin) {
        this.plugin = plugin;
    }

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

        plugin.getPlatform().executeAsync(() -> {
            try {
                plugin.getApiClient().createCoupon(coupon).execute();
                sender.sendMessage(ChatColor.GREEN + plugin.getI18n().get("coupon_creation_success", coupon.getCode()));
            } catch (IOException e) {
                sender.sendMessage(ChatColor.RED + e.getMessage());

            }
        });
    }

    private void deleteCoupon(final CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(ChatColor.RED + plugin.getI18n().get("no_coupon_specified"));
            return;
        }

        final String code = args[1];

        plugin.getPlatform().executeAsync(() -> {
            try {
                plugin.getApiClient().deleteCoupon(code).execute();
                sender.sendMessage(ChatColor.GREEN + plugin.getI18n().get("coupon_deleted"));
            } catch (IOException e) {
                sender.sendMessage(ChatColor.RED + e.getMessage());
            }
        });
    }

    @Override
    public String getDescription() {
        return plugin.getI18n().get("usage_coupon");
    }
}
