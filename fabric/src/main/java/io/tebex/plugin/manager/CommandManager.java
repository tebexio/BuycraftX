package io.tebex.plugin.manager;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.tebex.plugin.TebexPlugin;
import io.tebex.plugin.command.BuyCommand;
import io.tebex.plugin.command.SubCommand;
import io.tebex.plugin.command.sub.HelpCommand;
import io.tebex.plugin.command.sub.RecheckCommand;
import io.tebex.plugin.command.sub.ReloadCommand;
import io.tebex.plugin.command.sub.SecretCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class CommandManager {
    private final TebexPlugin platform;
    private final List<SubCommand> commands;

    public CommandManager(TebexPlugin  platform) {
        this.platform = platform;
        this.commands = ImmutableList.of(
                new SecretCommand(platform),
                new ReloadCommand(platform),
                new RecheckCommand(platform),
                new HelpCommand(platform, this)
        );
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> baseCommand = literal("tebex").executes(context -> {
            final ServerCommandSource source = context.getSource();
            source.sendFeedback(new LiteralText("§8[Tebex] §7Welcome to Tebex!"), false);
            source.sendFeedback(new LiteralText("§8[Tebex] §7This server is running version §fv" + platform.getVersion() + "§7."), false);

            return 1;
        });

        BuyCommand buyCommand = new BuyCommand(platform);
        dispatcher.register(literal(platform.getPlatformConfig().getBuyCommandName()).executes(buyCommand::execute));

        commands.forEach(command -> {
            LiteralArgumentBuilder<ServerCommandSource> subCommand = literal(command.getName());

            if(command.getName().equalsIgnoreCase("secret")) {
                baseCommand.then(subCommand.then(argument("key", StringArgumentType.string()).executes(context -> {
                    command.execute(context);
                    return 1;
                })));

                return;
            }

            baseCommand.then(subCommand.executes(context -> {
                command.execute(context);
                return 1;
            }));
        });

        dispatcher.register(baseCommand);
    }

    public TebexPlugin getPlatform() {
        return platform;
    }

    public List<SubCommand> getCommands() {
        return commands;
    }
}
