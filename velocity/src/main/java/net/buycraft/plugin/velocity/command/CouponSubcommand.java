package net.buycraft.plugin.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import net.buycraft.plugin.data.Coupon;
import net.buycraft.plugin.shared.util.CouponUtil;
import net.buycraft.plugin.velocity.BuycraftPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.IOException;
import java.util.Arrays;

public class CouponSubcommand implements Subcommand {
    private static final int COUPON_PAGE_LIMIT = 10;

    private final BuycraftPlugin plugin;

    public CouponSubcommand(final BuycraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSource sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Component.text(plugin.getI18n().get("usage_coupon_subcommands"), NamedTextColor.RED));
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
                sender.sendMessage(Component.text(plugin.getI18n().get("usage_coupon_subcommands"), NamedTextColor.RED));
                break;
        }
    }

    private void createCoupon(final CommandSource sender, String[] args) {
        String[] stripped = Arrays.copyOfRange(args, 1, args.length);
        final Coupon coupon;
        try {
            coupon = CouponUtil.parseArguments(stripped);
        } catch (Exception e) {
            sender.sendMessage(Component.text(plugin.getI18n().get("coupon_creation_arg_parse_failure", e.getMessage()), NamedTextColor.RED));
            return;
        }

        plugin.getPlatform().executeAsync(() -> {
            try {
                plugin.getApiClient().createCoupon(coupon).execute().body();
                sender.sendMessage(Component.text(plugin.getI18n().get("coupon_creation_success", coupon.getCode()), NamedTextColor.GREEN));
            } catch (IOException e) {
                sender.sendMessage(Component.text(plugin.getI18n().get("generic_api_operation_error"), NamedTextColor.RED));
            }
        });
    }

    private void deleteCoupon(final CommandSource sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(Component.text(plugin.getI18n().get("no_coupon_specified"), NamedTextColor.RED));
            return;
        }

        final String code = args[1];
        plugin.getPlatform().executeAsync(() -> {
            try {
                plugin.getApiClient().deleteCoupon(code).execute().body();
                sender.sendMessage(Component.text(plugin.getI18n().get("coupon_deleted"), NamedTextColor.GREEN));
            } catch (IOException e) {
                sender.sendMessage(Component.text(e.getMessage(), NamedTextColor.RED));
            }
        });
    }

    @Override
    public String getDescription() {
        return plugin.getI18n().get("usage_coupon");
    }
}
