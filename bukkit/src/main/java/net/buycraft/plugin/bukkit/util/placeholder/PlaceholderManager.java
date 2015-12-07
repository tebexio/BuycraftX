package net.buycraft.plugin.bukkit.util.placeholder;

import net.buycraft.plugin.data.QueuedCommand;

import java.util.ArrayList;
import java.util.List;

public class PlaceholderManager {
    private final List<Placeholder> placeholderList = new ArrayList<>();

    public void addPlaceholder(Placeholder placeholder) {
        placeholderList.add(placeholder);
    }

    public String doReplace(QueuedCommand command) {
        String c = command.getCommand();
        for (Placeholder placeholder : placeholderList) {
            c = placeholder.replace(c, command);
        }
        return c;
    }
}
