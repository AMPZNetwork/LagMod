package com.ampznetwork.lagmod.fabric;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.kyori.adventure.text.Component;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.*;

@Getter
@NoArgsConstructor
@Slf4j(topic = LagModFabric.AddonName)
public class LagModFabric implements ModInitializer, ServerLifecycleEvents.ServerStarted, ServerLifecycleEvents.ServerStopping, CommandRegistrationCallback {
    public static final String AddonName = "LagMod";

    public static Text component2text(Component component) {
        return Text.Serializer.fromJson(gson().serialize(component));
    }

    private final     ScheduledExecutorService             scheduler = Executors.newScheduledThreadPool(2);
    private final     Map<String, StandaloneCleanupCycler> cyclers   = new ConcurrentHashMap<>();
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
        dispatcher.register(LiteralArgumentBuilder.<ServerCommandSource>literal("cleanup-items").executes(ctx -> {
            var world = ctx.getSource().getWorld().getRegistryKey().toString();
            cyclers.get(world).cleanupEntities();
            return Command.SINGLE_SUCCESS;
        }));
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
