package net.buycraft.plugin.fabric.command;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.buycraft.plugin.BuyCraftAPI;
import net.buycraft.plugin.data.Category;
import net.buycraft.plugin.data.Package;
import net.buycraft.plugin.data.responses.ServerInformation;
import net.buycraft.plugin.fabric.BuycraftPlugin;
import net.buycraft.plugin.fabric.tasks.SendCheckoutLinkTask;
import net.buycraft.plugin.shared.util.Node;
import net.buycraft.plugin.shared.util.ReportBuilder;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class TebexCommand {
    private final BuycraftPlugin plugin;

    public TebexCommand(BuycraftPlugin plugin) {
        this.plugin = plugin;
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        registerCommands(dispatcher, "tebex");
        registerCommands(dispatcher, "buycraft");
    }

    private void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, String command) {
        dispatcher.register(literal(command).executes(context -> {
                    if (!checkPermission(context.getSource())) return 0;

                    onBaseCommand(context);
                    return 1;
                }).then(literal("secret").then(argument("token", StringArgumentType.string()).executes(context -> {
                    if (!checkPermission(context.getSource())) return 0;

                    onSecretCommand(context);
                    return 1;
                }))).then(literal("forcecheck").executes(context -> {
                    if (!checkPermission(context.getSource())) return 0;

                    onForceCheckCommand(context);
                    return 1;
                })).then(literal("info").executes(context -> {
                    if (!checkPermission(context.getSource())) return 0;

                    onInfoCommand(context);
                    return 1;
                })).then(literal("refresh").executes(context -> {
                    if (!checkPermission(context.getSource())) return 0;

                    onRefreshCommand(context);
                    return 1;
                })).then(literal("report").executes(context -> {
                    if (!checkPermission(context.getSource())) return 0;

                    onReportCommand(context);
                    return 1;
                })).then(literal("packages").then(argument("package", IntegerArgumentType.integer()).executes(context -> {
                    if (!checkPermission(context.getSource())) return 0;

                    onPackagesCommand(context);
                    return 1;
                }))).then(literal("checkout").then(argument("package", IntegerArgumentType.integer()).executes(context -> {
                    if (!checkPermission(context.getSource())) return 0;

                    onCheckoutCommand(context);
                    return 1;
                })))
        );
    }

    private boolean checkPermission(ServerCommandSource source) {
        if (!Permissions.check(source, "buycraft.admin", 4)) {
            source.sendError(new LiteralText("You do not have permission to use this command."));
            return false;
        }

        return true;
    }

    private void onBaseCommand(CommandContext<ServerCommandSource> context) {
        String[][] commands = new String[][]{
                new String[]{"/tebex forcecheck", "Forces a purchase check."},
                new String[]{"/tebex secret <token>", "Sets the secret key to use for this server."},
                new String[]{"/tebex info", "Retrieves public information about the webstore this server is associated with."},
                new String[]{"/tebex refresh", "Refreshes the list of categories and packages."},
                new String[]{"/tebex signupdate", "Forces an update to your recent purchase signs."},
                new String[]{"/tebex report", "Generates a report with debugging information you can send to support."},
                new String[]{"/tebex coupon", "Manage server coupons."},
                new String[]{"/tebex sendlink", "Sends a package or category link to a player."},
        };

        context.getSource().sendFeedback(new LiteralText("Usage for the Tebex plugin:").formatted(Formatting.BLUE, Formatting.BOLD), false);
        for (String[] command : commands) {
            context.getSource().sendFeedback(new LiteralText(command[0]).formatted(Formatting.GREEN).append(new LiteralText(": " + command[1]).formatted(Formatting.GRAY)), false);
        }
    }

    private void onSecretCommand(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        if (source.getEntity() instanceof ServerPlayerEntity) {
            source.sendFeedback(new LiteralText(plugin.getI18n().get("secret_console_only")), false);
            return;
        }

        String token = context.getArgument("token", String.class);

        plugin.getPlatform().executeAsync(() -> {
            String currentKey = plugin.getConfiguration().getServerKey();
            BuyCraftAPI client;
            try {
                client = BuyCraftAPI.create(token, plugin.getHttpClient());
                plugin.updateInformation(client);
            } catch (IOException e) {
                plugin.getLogger().error("Unable to verify secret", e);
                source.sendFeedback(new LiteralText(plugin.getI18n().get("secret_does_not_work")).formatted(Formatting.RED), false);
                return;
            }

            ServerInformation information = plugin.getServerInformation();
            plugin.setApiClient(client);
            plugin.getListingUpdateTask().run();
            plugin.getConfiguration().setServerKey(token);
            try {
                plugin.saveConfiguration();
            } catch (IOException e) {
                source.sendFeedback(new LiteralText(plugin.getI18n().get("secret_cant_be_saved")).formatted(Formatting.RED), false);
            }
            source.sendFeedback(new LiteralText(plugin.getI18n().get("secret_success",
                    information.getServer().getName(), information.getAccount().getName())).formatted(Formatting.GREEN), false);

            boolean repeatChecks = currentKey.equals("INVALID");

            plugin.getDuePlayerFetcher().run(repeatChecks);
        });
    }

    private void onForceCheckCommand(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        if (plugin.getApiClient() == null) {
            source.sendFeedback(new LiteralText(plugin.getI18n().get("need_secret_key")).formatted(Formatting.RED), false);
            return;
        }

        if (plugin.getDuePlayerFetcher().inProgress()) {
            source.sendFeedback(new LiteralText(plugin.getI18n().get("already_checking_for_purchases")).formatted(Formatting.RED), false);
            return;
        }

        plugin.getPlatform().executeAsync(() -> plugin.getDuePlayerFetcher().run(false));
        source.sendFeedback(new LiteralText(plugin.getI18n().get("forcecheck_queued")).formatted(Formatting.GREEN), false);
    }

    private void onInfoCommand(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        if (plugin.getApiClient() == null) {
            source.sendFeedback(new LiteralText(plugin.getI18n().get("generic_api_operation_error")).formatted(Formatting.RED), false);
            return;
        }

        if (plugin.getServerInformation() == null) {
            source.sendFeedback(new LiteralText(plugin.getI18n().get("information_no_server")).formatted(Formatting.RED), false);
            return;
        }

        String webstoreURL = plugin.getServerInformation().getAccount().getDomain();
        LiteralText webstore = (LiteralText) new LiteralText(webstoreURL)
                .formatted(Formatting.GREEN)
                .styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, webstoreURL)).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(webstoreURL))));

        LiteralText server = (LiteralText) new LiteralText(plugin.getServerInformation().getServer().getName()).formatted(Formatting.GREEN);

        Arrays.asList(
                new LiteralText(plugin.getI18n().get("information_title") + " ").formatted(Formatting.GRAY),
                new LiteralText(plugin.getI18n().get("information_sponge_server") + " ").formatted(Formatting.GRAY).append(server),
                new LiteralText(plugin.getI18n().get("information_currency", plugin.getServerInformation().getAccount().getCurrency().getIso4217())).formatted(Formatting.GRAY),
                new LiteralText(plugin.getI18n().get("information_domain", "")).formatted(Formatting.GRAY).append(webstore)
        ).forEach(item -> source.sendFeedback(item, false));
    }

    private void onRefreshCommand(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        if (plugin.getApiClient() == null) {
            source.sendFeedback(new LiteralText(plugin.getI18n().get("need_secret_key")).formatted(Formatting.RED), false);
            return;
        }

        plugin.getPlatform().executeAsync(plugin.getListingUpdateTask());
        source.sendFeedback(new LiteralText(plugin.getI18n().get("refresh_queued")).formatted(Formatting.GREEN), false);
    }

    private void onReportCommand(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        source.sendFeedback(new LiteralText(plugin.getI18n().get("report_wait")).formatted(Formatting.RED), false);

        plugin.getPlatform().executeAsync(() -> {
            String serverIP = plugin.getServer().getServerIp();
            int serverPort = plugin.getServer().getServerPort();

            ReportBuilder builder = ReportBuilder.builder()
                    .client(plugin.getHttpClient())
                    .configuration(plugin.getConfiguration())
                    .platform(plugin.getPlatform())
                    .duePlayerFetcher(plugin.getDuePlayerFetcher())
                    .ip(serverIP)
                    .port(serverPort)
                    .listingUpdateTask(plugin.getListingUpdateTask())
                    .serverOnlineMode(plugin.getServer().isOnlineMode())
                    .build();

            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
            String filename = "report-" + f.format(new Date()) + ".txt";
            Path p = plugin.getBaseDirectory().resolve(filename);
            String generated = builder.generate();

            try (BufferedWriter w = Files.newBufferedWriter(p, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW)) {
                w.write(generated);
                source.sendFeedback(new LiteralText(plugin.getI18n().get("report_saved", p.toAbsolutePath().toString())).formatted(Formatting.YELLOW), false);
            } catch (IOException e) {
                source.sendFeedback(new LiteralText(plugin.getI18n().get("report_cant_save")).formatted(Formatting.RED), false);
                plugin.getLogger().info(generated);
            }
        });
    }

    private void onPackagesCommand(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        Integer packageId = context.getArgument("package", Integer.class);

        Node categories = new Node(plugin.getListingUpdateTask().getListing().getCategories(), ImmutableList.of(), plugin.getI18n().get("categories"), null);
        Optional<Category> category = categories.getSubcategories().stream().filter(categoryId -> categoryId.getId() == packageId).findFirst();

        plugin.getBuyCommand().sendPaginatedMessage(categories.getChild(category.get()), source);
    }

    private void onCheckoutCommand(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        Integer packageId = context.getArgument("package", Integer.class);

        try {
            Package packageById = plugin.getListingUpdateTask().getPackageById(packageId);
            plugin.getPlatform().executeAsync(new SendCheckoutLinkTask(plugin, packageById.getId(), source));
        } catch (Exception e) {
            source.sendFeedback(new LiteralText("Could not find package with id " + packageId).formatted(Formatting.RED), false);
        }
    }
}
