package net.buycraft.plugin.bukkit.command;

import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.bukkit.BuycraftPlugin;
import net.buycraft.plugin.data.Coupon;
import net.buycraft.plugin.shared.util.CouponUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

@RequiredArgsConstructor
public class CouponSubcommand implements Subcommand {
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

    private void listCoupons(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + plugin.getI18n().get("no_params"));
            return;
        }

        // TODO: Handle listing coupons.
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

    private void deleteCoupon(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(ChatColor.RED + plugin.getI18n().get("no_coupon_specified"));
            return;
        }

        // TODO: Handle coupon deletion.
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
