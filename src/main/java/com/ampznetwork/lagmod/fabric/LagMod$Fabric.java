package com.ampznetwork.lagmod.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class LagMod$Fabric implements ModInitializer, ServerLifecycleEvents.ServerStarted {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private       ScheduledFuture<?>       task;

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(this);
    }

    @Override
    public void onServerStarted(MinecraftServer server) {
        task = scheduler.scheduleAtFixedRate(this::cleanupEntities, 15, 15, TimeUnit.MINUTES);
    }

    private void cleanupEntities() {
        
    }
}
