package net.buycraft.plugin.sponge.command;

import net.buycraft.plugin.data.Coupon;
import net.buycraft.plugin.shared.util.CouponUtil;
import net.buycraft.plugin.sponge.BuycraftPlugin;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.util.Color;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

public class CouponCmd {
    private static final int COUPON_PAGE_LIMIT = 10;
    private final BuycraftPlugin plugin;

    public CouponCmd(final BuycraftPlugin plugin) {
        this.plugin = plugin;
    }

    public CommandResult createCoupon(CommandContext ctx) {
        Audience source = (Audience) ctx.cause().root();
        Collection<? extends String> argsList = ctx.all(Parameter.string().key("args").build());

        String[] argsArray = argsList.toArray(new String[0]);
        final Coupon coupon;
        try {
            coupon = CouponUtil.parseArguments(argsArray);
        } catch (Exception e) {
            source.sendMessage(Component.text(plugin.getI18n().get("coupon_creation_arg_parse_failure", e.getMessage())).color(TextColor.color(Color.RED)));
            return CommandResult.success();
        }

        plugin.getPlatform().executeAsync(() -> {
            try {
                plugin.getApiClient().createCoupon(coupon).execute();
                source.sendMessage(Component.text(plugin.getI18n().get("coupon_creation_success", coupon.getCode())).color(TextColor.color(Color.GREEN)));
            } catch (IOException e) {
                source.sendMessage(Component.text(plugin.getI18n().get("generic_api_operation_error")).color(TextColor.color(Color.RED)));
            }
        });

        return CommandResult.success();
    }

    public CommandResult deleteCoupon(CommandContext ctx) {
        Audience source = (Audience) ctx.cause().root();

        Optional<String> codeOptional = ctx.one(Parameter.string().key("code").build());

        if (!codeOptional.isPresent()) {
            source.sendMessage(Component.text(plugin.getI18n().get("no_coupon_specified")).color(TextColor.color(Color.RED)));
            return CommandResult.success();
        }
        final String code = codeOptional.get();

        plugin.getPlatform().executeAsync(() -> {
            try {
                plugin.getApiClient().deleteCoupon(code).execute();
                source.sendMessage(Component.text(plugin.getI18n().get("coupon_deleted")).color(TextColor.color(Color.GREEN)));
            } catch (IOException e) {
                source.sendMessage(Component.text(e.getMessage()).color(TextColor.color(Color.RED)));
            }
        });

        return CommandResult.success();
    }
}
