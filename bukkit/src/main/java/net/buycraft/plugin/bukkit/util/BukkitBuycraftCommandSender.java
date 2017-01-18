package net.buycraft.plugin.bukkit.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.bukkit.BuycraftPlugin;
import net.buycraft.plugin.shared.commands.BuycraftCommandSender;
import net.buycraft.plugin.shared.commands.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

@RequiredArgsConstructor
public class BukkitBuycraftCommandSender implements BuycraftCommandSender {
    @Getter
    private final CommandSender bukkitSender;
    private final BuycraftPlugin plugin;

    @Override
    public UUID getUuid() {
        if (bukkitSender instanceof Player) {
            return ((Player) bukkitSender).getUniqueId();
        }
        return null;
    }

    @Override
    public String getName() {
        return bukkitSender.getName();
    }

    @Override
    public void sendMessage(String message, String... args) {
        bukkitSender.sendMessage(plugin.getI18n().get(message, (Object[]) args));
    }

    @Override
    public void sendMessage(ChatColor color, String message, String... args) {
        bukkitSender.sendMessage(color + plugin.getI18n().get(message, (Object[]) args));
    }

    @Override
    public boolean isConsole() {
        return bukkitSender instanceof ConsoleCommandSender;
    }
}
