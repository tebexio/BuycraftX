package net.buycraft.plugin.fabric.command;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.buycraft.plugin.BuyCraftAPI;
import net.buycraft.plugin.data.Category;
import net.buycraft.plugin.data.Package;
import net.buycraft.plugin.data.responses.ServerInformation;
import net.buycraft.plugin.fabric.BuycraftPlugin;
import net.buycraft.plugin.shared.util.Node;
import net.buycraft.plugin.shared.util.ReportBuilder;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class BuyCommand {
    private final BuycraftPlugin plugin;

    public BuyCommand(BuycraftPlugin plugin) {
        this.plugin = plugin;
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        dispatcher.register(literal("buy")
                .executes(context -> {
                    ServerCommandSource source = context.getSource();

                    if (plugin.getApiClient() == null) {
                        source.sendFeedback(new LiteralText(plugin.getI18n().get("generic_api_operation_error")).formatted(Formatting.RED), false);
                        return 1;
                    }

                    if (plugin.getListingUpdateTask().getListing() == null) {
                        source.sendFeedback(new LiteralText("We're currently retrieving the listing. Sit tight!").formatted(Formatting.RED), false);
                        return 1;
                    }

                    sendPaginatedMessage(new Node(plugin.getListingUpdateTask().getListing().getCategories(), ImmutableList.of(), plugin.getI18n().get("categories"), null), source);

                    return 1;
                })
        );
    }

    public void sendPaginatedMessage(Node node, ServerCommandSource source) {
        List<Category> subcategories = node.getSubcategories();

        List<MutableText> contents;
        if(subcategories.size() > 0) {
            contents = subcategories.stream().map(category -> {
                return new LiteralText("> " + category.getName())
                        .formatted(Formatting.GRAY)
                        .styled(style -> style
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tebex packages " + category.getId()))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Click to view packages in this category")))
                        );
            }).collect(Collectors.toList());
        } else {
            contents = new ArrayList<>();
            for (Package p : node.getPackages()) {
                contents.add(
                        new LiteralText(p.getName())
                                .formatted(Formatting.WHITE)
                                .append(new LiteralText(" - ").formatted(Formatting.GRAY)
                                        .append(new LiteralText("$x".replace("$", plugin.getServerInformation().getAccount().getCurrency().getSymbol())
                                                .replace("x", "" + p.getEffectivePrice())).formatted(Formatting.GREEN))
                                        .styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tebex checkout " + p.getId()))))
                                );
            }
        }

        MutableText packageHeader = new LiteralText(plugin.getI18n().get("sponge_listing")).formatted(Formatting.BLUE);
        source.sendFeedback(packageHeader, false);
        contents.forEach(item -> source.sendFeedback(item, false));
    }
}
