package net.buycraft.plugin.shared.commands;

import java.util.UUID;

public interface BuycraftCommandSender {
    UUID getUuid();

    String getName();

    void sendMessage(String message, String... args);

    void sendMessage(ChatColor color, String message, String... args);

    boolean isConsole();
}
