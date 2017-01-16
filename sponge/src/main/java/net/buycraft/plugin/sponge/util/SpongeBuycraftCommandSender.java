package net.buycraft.plugin.sponge.util;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.shared.commands.BuycraftCommandSender;
import net.buycraft.plugin.shared.commands.ChatColor;
import net.buycraft.plugin.sponge.BuycraftPlugin;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class SpongeBuycraftCommandSender implements BuycraftCommandSender {
    private static final Map<ChatColor, TextColor> COLOR_MAP = ImmutableMap.<ChatColor, TextColor>builder()
            .put(ChatColor.GRAY, TextColors.GRAY)
            .put(ChatColor.GREEN, TextColors.GREEN)
            .put(ChatColor.RED, TextColors.RED)
            .put(ChatColor.YELLOW, TextColors.YELLOW)
            .build();
    private final CommandSource spongeCommandSource;
    private final BuycraftPlugin plugin;

    @Override
    public UUID getUuid() {
        if (spongeCommandSource instanceof Player) {
            return ((Player) spongeCommandSource).getUniqueId();
        }
        return null;
    }

    @Override
    public String getName() {
        return spongeCommandSource.getName();
    }

    @Override
    public void sendMessage(String message, String... args) {
        spongeCommandSource.sendMessage(Text.of(plugin.getI18n().get(message, (Object[]) args)));
    }

    @Override
    public void sendMessage(ChatColor color, String message, String... args) {
        spongeCommandSource.sendMessage(Text.builder(plugin.getI18n().get(message, (Object[]) args))
                .color(COLOR_MAP.get(color)).build());
    }
}
