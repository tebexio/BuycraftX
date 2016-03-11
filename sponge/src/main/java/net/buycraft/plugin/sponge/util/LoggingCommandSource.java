package net.buycraft.plugin.sponge.util;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import net.buycraft.plugin.util.CommandOutput;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.util.Tristate;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class LoggingCommandSource implements ConsoleSource {
    @Getter
    private final CommandOutput output = new CommandOutput();

    @Override
    public String getName() {
        return "CONSOLE^BC";
    }

    @Override
    public String getIdentifier() {
        return Sponge.getServer().getConsole().getIdentifier();
    }

    @Override
    public Set<Context> getActiveContexts() {
        return Sponge.getServer().getConsole().getActiveContexts();
    }

    @Override
    public void sendMessage(Text text) {
        output.addLine(text.toPlain());
    }

    @Override
    public MessageChannel getMessageChannel() {
        return MessageChannel.fixed(this);
    }

    @Override
    public void setMessageChannel(MessageChannel messageChannel) {

    }

    @Override
    public Optional<CommandSource> getCommandSource() {
        return Optional.of(this);
    }

    @Override
    public SubjectCollection getContainingCollection() {
        return Sponge.getServer().getConsole().getContainingCollection();
    }

    @Override
    public SubjectData getSubjectData() {
        return Sponge.getServer().getConsole().getSubjectData();
    }

    @Override
    public SubjectData getTransientSubjectData() {
        return Sponge.getServer().getConsole().getTransientSubjectData();
    }

    @Override
    public boolean hasPermission(Set<Context> set, String s) {
        return true;
    }

    @Override
    public Tristate getPermissionValue(Set<Context> set, String s) {
        return Tristate.TRUE;
    }

    @Override
    public boolean isChildOf(Set<Context> set, Subject subject) {
        return true;
    }

    @Override
    public List<Subject> getParents(Set<Context> set) {
        return ImmutableList.of();
    }
}
