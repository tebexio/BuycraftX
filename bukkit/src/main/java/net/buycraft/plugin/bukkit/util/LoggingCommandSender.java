package net.buycraft.plugin.bukkit.util;

import com.google.common.collect.ImmutableSet;
import lombok.Getter;
import net.buycraft.plugin.util.CommandOutput;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

import java.util.Set;

public class LoggingCommandSender implements ConsoleCommandSender {
    @Getter
    private final CommandOutput output = new CommandOutput();

    @Override
    public void sendMessage(String s) {
        output.addLine(ChatColor.stripColor(s));
    }

    @Override
    public void sendMessage(String[] strings) {
        for (String s : strings) {
            sendMessage(s);
        }
    }

    @Override
    public Server getServer() {
        return Bukkit.getServer();
    }

    @Override
    public String getName() {
        return "CONSOLE^BC";
    }

    @Override
    public boolean isConversing() {
        return false;
    }

    @Override
    public void acceptConversationInput(String s) {

    }

    @Override
    public boolean beginConversation(Conversation conversation) {
        return false;
    }

    @Override
    public void abandonConversation(Conversation conversation) {

    }

    @Override
    public void abandonConversation(Conversation conversation, ConversationAbandonedEvent conversationAbandonedEvent) {

    }

    @Override
    public void sendRawMessage(String s) {
        sendMessage(s);
    }

    @Override
    public boolean isPermissionSet(String s) {
        return true;
    }

    @Override
    public boolean isPermissionSet(Permission permission) {
        return true;
    }

    @Override
    public boolean hasPermission(String s) {
        return true;
    }

    @Override
    public boolean hasPermission(Permission permission) {
        return true;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b) {
        return null;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin) {
        return null;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b, int i) {
        return null;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int i) {
        return null;
    }

    @Override
    public void removeAttachment(PermissionAttachment permissionAttachment) {

    }

    @Override
    public void recalculatePermissions() {

    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return ImmutableSet.of();
    }

    @Override
    public boolean isOp() {
        return true;
    }

    @Override
    public void setOp(boolean b) {

    }
}
