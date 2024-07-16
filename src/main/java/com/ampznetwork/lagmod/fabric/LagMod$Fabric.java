package com.ampznetwork.lagmod.fabric;

import com.ampznetwork.lagmod.fabric.config.Config;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.TypeFilter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static net.kyori.adventure.text.Component.*;
import static net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.*;

@Getter
@NoArgsConstructor
@Slf4j(topic = LagMod$Fabric.AddonName)
public class LagMod$Fabric implements ModInitializer, ServerLifecycleEvents.ServerStarted, ServerLifecycleEvents.ServerStopping, CommandRegistrationCallback {
    public static final String AddonName = "LagMod";
    public static final String AddonId = "lagmod";

    public static Text component2text(Component component) {
        return Text.Serializer.fromJson(gson().serialize(component));
    }

    private final     ScheduledExecutorService             scheduler = Executors.newScheduledThreadPool(2);
    private final     Map<String, StandaloneCleanupCycler> cyclers   = new ConcurrentHashMap<>();
    private final Config config = Config.createAndLoad();
    private @NonFinal MinecraftServer                      server;

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(this);
        ServerLifecycleEvents.SERVER_STOPPING.register(this);
        CommandRegistrationCallback.EVENT.register(this);
    }

    @Override
    public void register(
            CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment
    ) {
        dispatcher.register(LiteralArgumentBuilder.<ServerCommandSource>literal("cleanup")
                .then(LiteralArgumentBuilder.<ServerCommandSource>literal("items")
                        .requires(Permissions.require("lagmod.cleanup.items"))
                        .executes(cleanupCommand()))
                .then(LiteralArgumentBuilder.<ServerCommandSource>literal("mobs")
                        .requires(Permissions.require("lagmod.cleanup.mobs"))
                        .executes(cleanupCommand()))
                .then(LiteralArgumentBuilder.<ServerCommandSource>literal("animals")
                        .requires(Permissions.require("lagmod.cleanup.animals"))
                        .executes(cleanupCommand(EntityType.ITEM))));
    }

    private Command<ServerCommandSource> cleanupCommand(TypeFilter<Entity, ItemEntity> filter) {
        return ctx -> {
            var world  = ctx.getSource().getWorld().getRegistryKey().toString();
            var cycler = cyclers.get(world);
            cycler.cleanup(filter);
            if (filter == EntityType.ITEM) {
                cycler.resetTimer();
                ctx.getSource().sendMessage(component2text(text("The cleanup timer has been reset").color(NamedTextColor.AQUA)));
            }
            return Command.SINGLE_SUCCESS;
        }
    }

    @Override
    public void onServerStarted(MinecraftServer server) {
        this.server = server;

        for (var world : server.getWorlds()) {
            var cycler = new StandaloneCleanupCycler(this, world);
            cyclers.put(world.getRegistryKey().toString(), cycler);
        }
    }

    @Override
    public void onServerStopping(MinecraftServer server) {
        scheduler.shutdown();
    }
}
