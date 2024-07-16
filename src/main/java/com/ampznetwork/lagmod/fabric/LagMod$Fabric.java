package com.ampznetwork.lagmod.fabric;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContextBuilder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.kyori.adventure.text.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.*;

@Getter
@NoArgsConstructor
@Slf4j(topic = LagMod$Fabric.AddonName)
public class LagMod$Fabric implements ModInitializer, ServerLifecycleEvents.ServerStarted, ServerLifecycleEvents.ServerStopping {
    public static final String AddonName = "LagMod";

    public static Text component2text(Component component) {
        return Text.Serializer.fromJson(gson().serialize(component));
    }

    private final     ScheduledExecutorService     scheduler = Executors.newScheduledThreadPool(2);
    private final     Set<StandaloneCleanupCycler> cyclers   = new HashSet<>();
    private @NonFinal MinecraftServer              server;

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(this);
        ServerLifecycleEvents.SERVER_STOPPING.register(this);
    }

    @Override
    public void onServerStarted(MinecraftServer server) {
        this.server = server;

        for (var world : server.getWorlds()) {
            var cycler = new StandaloneCleanupCycler(this, world);
            cyclers.add(cycler);
        }
    }

    @Override
    public void onServerStopping(MinecraftServer server) {
        scheduler.shutdown();
    }

    public void executeCommand(World world, String command) {
        var mgr = server.getCommandManager();
        var out = new CommandOutput() {
            @Override
            public void sendMessage(Text message) {
                log.info(message.getString());
            }

            @Override
            public boolean shouldReceiveFeedback() {
                return false;
            }

            @Override
            public boolean shouldTrackOutput() {
                return false;
            }

            @Override
            public boolean shouldBroadcastConsoleToOps() {
                return false;
            }
        };
        var parse = getParseResults((ServerWorld) world, out, mgr);
        mgr.execute(parse, command);
    }

    private @NotNull ParseResults<ServerCommandSource> getParseResults(ServerWorld world, CommandOutput out, CommandManager mgr) {
        var src = new ServerCommandSource(out,
                Vec3d.ZERO,
                Vec2f.ZERO,
                world,
                0,
                LagMod$Fabric.AddonName,
                Text.of(LagMod$Fabric.AddonName),
                server,
                null);
        return new ParseResults<>(new CommandContextBuilder<>(
                mgr.getDispatcher(),
                src,
                mgr.getDispatcher().getRoot(),
                0));
    }
}
