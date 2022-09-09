package net.buycraft.plugin.fabric.tasks;

import net.buycraft.plugin.data.responses.CheckoutUrlResponse;
import net.buycraft.plugin.fabric.BuycraftPlugin;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class SendCheckoutLinkTask implements Runnable {
    @NotNull
    private final BuycraftPlugin plugin;
    private final int pkgId;
    @NotNull
    private final ServerCommandSource player;

    public SendCheckoutLinkTask(@NotNull final BuycraftPlugin plugin, final int pkgId, @NotNull final ServerCommandSource player) {
        this.plugin = Objects.requireNonNull(plugin);
        this.pkgId = pkgId;
        this.player = Objects.requireNonNull(player);
    }

    @Override
    public void run() {
        CheckoutUrlResponse response;
        try {
            response = plugin.getApiClient().getCheckoutUri(player.getName(), pkgId).execute().body();
        } catch (IOException e) {
            player.sendFeedback(new LiteralText(plugin.getI18n().get("cant_check_out")).formatted(Formatting.RED), false);
            return;
        }
        if (response != null) {
                player.sendFeedback(new LiteralText("                                            ").formatted(Formatting.STRIKETHROUGH), false);
            Arrays.asList(
                    new LiteralText(plugin.getI18n().get("to_buy_this_package")).formatted(Formatting.GREEN),
                    new LiteralText(response.getUrl()).formatted(Formatting.BLUE, Formatting.UNDERLINE).styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, response.getUrl())))
            ).forEach(msg -> player.sendFeedback(msg, false));
            player.sendFeedback(new LiteralText("                                            ").formatted(Formatting.STRIKETHROUGH), false);
        }
    }
}