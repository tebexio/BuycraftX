package io.tebex.plugin.command;

import com.mojang.brigadier.context.CommandContext;
import io.tebex.plugin.TebexPlugin;
import net.minecraft.server.command.ServerCommandSource;

public abstract class SubCommand {
    private final TebexPlugin platform;
    private final String name;
    private final String permission;

    public SubCommand(TebexPlugin platform, String name, String permission) {
        this.platform = platform;
        this.name = name;
        this.permission = permission;
    }

    public abstract void execute(final CommandContext<ServerCommandSource> context);

    public TebexPlugin getPlatform() {
        return platform;
    }

    public String getName() {
        return name;
    }

    public String getPermission() {
        return permission;
    }

    public abstract String getDescription();
    public String getUsage() {
        return "";
    }
}
