package net.buycraft.plugin.bukkit.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Field;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommandInjector {
    public static boolean injectCommand(String name, final CommandExecutor executor) {
        Command command = new Command(name) {
            @Override
            public boolean execute(CommandSender commandSender, String s, String[] strings) {
                return executor.onCommand(commandSender, this, s, strings);
            }
        };

        try {
            Field cmdMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            cmdMapField.setAccessible(true);
            CommandMap map = (CommandMap) cmdMapField.get(Bukkit.getServer());
            return map.register("", command);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException("Can't add command to Bukkit command map", e);
        }
    }
}
