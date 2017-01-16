package net.buycraft.plugin.bungeecord;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.shared.commands.BuycraftCommandSender;
import net.buycraft.plugin.shared.commands.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

@RequiredArgsConstructor
public class BungeeCordBuycraftCommandSender implements BuycraftCommandSender {
    @Getter
    private final CommandSender bungeeSender;
    private final BuycraftPlugin plugin;

    @Override
    public UUID getUuid() {
        if (bungeeSender instanceof ProxiedPlayer) {
            return ((ProxiedPlayer) bungeeSender).getUniqueId();
        }
        return null;
    }

    @Override
    public String getName() {
        return bungeeSender.getName();
    }

    @Override
    public void sendMessage(String message, String... args) {
        bungeeSender.sendMessage(plugin.getI18n().get(message, (Object[]) args));
    }

    @Override
    public void sendMessage(ChatColor color, String message, String... args) {
        bungeeSender.sendMessage(color + plugin.getI18n().get(message, (Object[]) args));
    }
}
