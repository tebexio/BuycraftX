package net.buycraft.plugin.bungeecord.util;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import net.buycraft.plugin.util.CommandOutput;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;

import java.util.Collection;

public class LoggingCommandSender implements CommandSender {
    @Getter
    private final CommandOutput output = new CommandOutput();

    @Override
    public String getName() {
        return "CONSOLE^BC";
    }

    @Override
    public void sendMessage(String s) {
        output.addLine(ChatColor.stripColor(s));
    }

    @Override
    public void sendMessages(String... strings) {
        for (String s : strings) {
            output.addLine(s);
        }
    }

    @Override
    public void sendMessage(BaseComponent... baseComponents) {
        output.addLine(BaseComponent.toPlainText(baseComponents));
    }

    @Override
    public void sendMessage(BaseComponent baseComponent) {
        output.addLine(BaseComponent.toPlainText(baseComponent));
    }

    @Override
    public Collection<String> getGroups() {
        return ImmutableList.of();
    }

    @Override
    public void addGroups(String... strings) {

    }

    @Override
    public void removeGroups(String... strings) {

    }

    @Override
    public boolean hasPermission(String s) {
        return true;
    }

    @Override
    public void setPermission(String s, boolean b) {

    }

    @Override
    public Collection<String> getPermissions() {
        return ImmutableList.of();
    }
}
