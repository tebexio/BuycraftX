package net.buycraft.plugin.bukkit.gui.actions;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.bukkit.gui.Action;
import net.buycraft.plugin.bukkit.gui.BaseGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

@RequiredArgsConstructor
public class OpenGUIAction implements Action {
    @NonNull
    private final BaseGUI gui;

    @Override
    public void onClick(Player player, ClickType clickType) {
        gui.open(player);
    }
}
