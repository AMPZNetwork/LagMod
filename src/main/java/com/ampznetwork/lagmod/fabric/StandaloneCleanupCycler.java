package com.ampznetwork.lagmod.fabric;

import com.ampznetwork.lagmod.fabric.config.Config;
import com.ampznetwork.lagmod.fabric.config.ListMode;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.ampznetwork.lagmod.fabric.LagMod$Fabric.*;
import static net.kyori.adventure.text.Component.*;
import static net.kyori.adventure.text.format.NamedTextColor.*;

@Value
@Slf4j(topic = LagMod$Fabric.AddonName)
public class StandaloneCleanupCycler {
    LagMod$Fabric mod;
    World         world;
    Config config;
    @NonFinal ScheduledFuture<?> recurring;
    @NonFinal ScheduledFuture<?> countdown;

    public StandaloneCleanupCycler(LagMod$Fabric mod, World world) {
        this.mod    = mod;
        this.world  = world;
        this.config = mod.getConfig();

        resetTimer();
    }

    public void resetTimer() {
        if (recurring != null) recurring.cancel(true);
        if (countdown != null) countdown.cancel(true);

        this.recurring = mod.getScheduler().scheduleAtFixedRate(this::startCleanupCycle, config.intervalMinutes(), config.intervalMinutes(), TimeUnit.MINUTES);
    }

    private void startCleanupCycle() {
        if (world.getPlayers().isEmpty()) return;
        cycleWarn1();
        countdown = mod.getScheduler().schedule(this::cycleWarn2, 30, TimeUnit.SECONDS);
    }

    public void cycleWarn1() {
        broadcast(warningText("1 minute"));
    }

    public void cycleWarn2() {
        broadcast(warningText("30 seconds"));
        countdown = mod.getScheduler().schedule(() -> cleanup(EntityType.ITEM), 30, TimeUnit.SECONDS);
    }

    public void cleanup(TypeFilter<Entity, ItemEntity> entityType) {
        long c = 0;
        for (var item : world.getEntitiesByType(entityType, Box.of(Vec3d.ZERO, 60_000_000, 1000, 60_000_000), this::applyItemFilter)) {
            item.kill();
            c += 1;
        }
        broadcast(text()
                .append(text("Removed ").color(AQUA))
                .append(text(c).color(GREEN))
                .append(text(" items").color(AQUA))
                .build());
    }

    public void broadcast(Component message) {
        world.getPlayers().forEach(plr -> plr.sendMessage(component2text(message)));
    }

    private Component warningText(String remaining) {
        return text().append(text("Warning: ").color(GOLD))
                // dont ask me why the fuck i have to qualify the class in the next call
                .append(text("All dropped items will be removed in " + remaining).color(NamedTextColor.YELLOW)).build();
    }

    private boolean applyItemFilter(ItemEntity item) {
        var key = item.getStack().getItem().toString();
        var idx = key.indexOf(':');
        if (idx != -1) key = key.substring(idx + 1);
        var result = config.itemList().contains(key);
        return (config.itemListMode() == ListMode.whitelist) == result;
    }
}
