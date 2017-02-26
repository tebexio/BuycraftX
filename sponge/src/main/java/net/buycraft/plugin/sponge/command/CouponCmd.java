package net.buycraft.plugin.sponge.command;

import com.google.common.base.Joiner;
import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.client.ApiException;
import net.buycraft.plugin.data.Coupon;
import net.buycraft.plugin.shared.util.CouponUtil;
import net.buycraft.plugin.sponge.BuycraftPlugin;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.io.IOException;
import java.util.*;

@RequiredArgsConstructor
public class CouponCmd {
    private static final int COUPON_PAGE_LIMIT = 10;

    private final BuycraftPlugin plugin;

    public CommandResult listCoupons(CommandSource source, CommandContext ctx) throws CommandException {
        List<Coupon> couponList = plugin.getCouponUpdateTask().getListing();

        List<String> codes = new ArrayList<>();
        for (Coupon coupon : couponList) {
            codes.add(coupon.getCode());
        }

        source.sendMessage(Text.builder(plugin.getI18n().get("coupon_listing", Joiner.on(", ").join(codes)))
                .color(TextColors.YELLOW)
                .build());

        return CommandResult.queryResult(codes.size());
    }

    public CommandResult createCoupon(CommandSource source, CommandContext ctx) throws CommandException {
        Collection<String> argsList = ctx.getAll("args");
        String[] argsArray = argsList.toArray(new String[argsList.size()]);
        final Coupon coupon;
        try {
            coupon = CouponUtil.parseArguments(argsArray);
        } catch (Exception e) {
            source.sendMessage(Text.builder(plugin.getI18n().get("coupon_creation_arg_parse_failure", e.getMessage()))
                    .color(TextColors.RED)
                    .build());
            return CommandResult.empty();
        }

        plugin.getPlatform().executeAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    plugin.getApiClient().createCoupon(coupon);
                    source.sendMessage(Text.builder(plugin.getI18n().get("coupon_creation_success", coupon.getCode()))
                            .color(TextColors.GREEN)
                            .build());

                    plugin.getPlatform().executeAsync(plugin.getCouponUpdateTask());
                } catch (ApiException | IOException e) {
                    source.sendMessage(Text.builder(plugin.getI18n().get("generic_api_operation_error"))
                            .color(TextColors.RED)
                            .build());
                }
            }
        });

        return CommandResult.empty();
    }

    public CommandResult deleteCoupon(CommandSource source, CommandContext ctx) throws CommandException {
        Optional<String> codeOptional = ctx.getOne("code");
        if (!codeOptional.isPresent()) {
            source.sendMessage(Text.builder(plugin.getI18n().get("no_coupon_specified"))
                    .color(TextColors.RED)
                    .build());
            return CommandResult.empty();
        }

        final Coupon coupon = plugin.getCouponUpdateTask().getCouponByCode(codeOptional.get());
        if (coupon == null) {
            source.sendMessage(Text.builder(plugin.getI18n().get("coupon_not_found"))
                    .color(TextColors.RED)
                    .build());
            return CommandResult.empty();
        }

        plugin.getPlatform().executeAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    plugin.getApiClient().deleteCoupon(coupon.getId());
                    source.sendMessage(Text.builder(plugin.getI18n().get("coupon_deleted"))
                            .color(TextColors.GREEN)
                            .build());
                    plugin.getPlatform().executeAsync(plugin.getCouponUpdateTask());
                } catch (ApiException | IOException e) {
                    source.sendMessage(Text.builder(plugin.getI18n().get("generic_api_operation_error"))
                            .color(TextColors.RED)
                            .build());
                }
            }
        });

        return CommandResult.empty();
    }
}
