package com.ampznetwork.lagmod.fabric;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;

public class Quasimodo extends Entity {
    public Quasimodo(World world) {
        super(EntityType.PLAYER, world);
    }

    @Override
    protected void initDataTracker() {
        //nop
    }

    @Override
    public void tick() {

        super.tick();
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        //nop
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        //nop
    }
}
