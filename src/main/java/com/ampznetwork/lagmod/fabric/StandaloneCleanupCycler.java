package com.ampznetwork.lagmod.fabric;

import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
    @NonFinal ScheduledFuture<?> recurring;

    public StandaloneCleanupCycler(LagMod$Fabric mod, World world) {
        this.mod       = mod;
        this.world     = world;
        this.recurring = mod.getScheduler().scheduleAtFixedRate(this::startCleanupCycle, 1, 30, TimeUnit.MINUTES);
    }

    private void startCleanupCycle() {
        if (world.getPlayers().isEmpty())
            return;
        mod.getScheduler().schedule(this::cycleWarn1, 0, TimeUnit.SECONDS);
        mod.getScheduler().schedule(this::cycleWarn2, 30, TimeUnit.SECONDS);
        mod.getScheduler().schedule(this::cleanupEntities, 1, TimeUnit.MINUTES);
    }

    public void cycleWarn1() {
        broadcast(warningText("1 minute"));
    }

    public void broadcast(Component message) {
        world.getPlayers().forEach(plr -> plr.sendMessage(component2text(message)));
    }

    public void cycleWarn2() {
        broadcast(warningText("30 seconds"));
    }

    public void cleanupEntities() {
        mod.executeCommand(world, "execute in %s run kill @e[type=item]".formatted(world.getRegistryKey().toString()));
    }

    private Component warningText(String remaining) {
        return text()
                .append(text("Warning: ").color(GOLD))
                // dont ask me why the fuck i have to qualify the class in the next call
                .append(text("All dropped items will be removed in " + remaining).color(NamedTextColor.YELLOW))
                .build();
    }
}
