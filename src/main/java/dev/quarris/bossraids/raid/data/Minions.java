package dev.quarris.bossraids.raid.data;

import dev.quarris.bossraids.raid.definitions.MinionEntityDefinition;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;

import java.util.*;

public class Minions {

    private final MinionEntityDefinition definition;
    private final Set<UUID> minions = new HashSet<>();
    private int cooldown = 30;

    public Minions(MinionEntityDefinition definition) {
        this.definition = definition;
    }

    public Minions(CompoundNBT tag) {
        this.definition = new MinionEntityDefinition(tag.getCompound("Definition"));
        this.cooldown = tag.getInt("Cooldown");
        for (INBT nbt : tag.getList("MinionIds", Constants.NBT.TAG_INT_ARRAY)) {
            minions.add(NBTUtil.loadUUID(nbt));
        }
    }

    public void update(ServerWorld level) {
        if (this.cooldown > 0) {
            this.cooldown--;
        }

        for (Iterator<UUID> iterator = this.minions.iterator(); iterator.hasNext(); ) {
            UUID minionUUID = iterator.next();
            Entity minion = level.getEntity(minionUUID);
            if (minion == null || !minion.isAlive()) {
                iterator.remove();
            }
        }
    }

    public boolean shouldSpawn() {
        return this.cooldown == 0;
    }

    public Entity createSpawn(ServerWorld level, Vector3d pos) {
        Entity minion = this.definition.create(level, pos);
        this.minions.add(minion.getUUID());
        return minion;
    }

    public int toSpawn() {
        return this.definition.amountToSpawn(this.minions.size());
    }

    public void resetCooldown() {
        this.cooldown = this.definition.cooldown();
    }

    public CompoundNBT serialize() {
        CompoundNBT tag = new CompoundNBT();
        tag.put("Definition", this.definition.serialize());
        tag.putInt("Cooldown", this.cooldown);
        ListNBT minionsList = new ListNBT();
        for (UUID uuid : this.minions) {
            minionsList.add(NBTUtil.createUUID(uuid));
        }
        tag.put("MinionIds", minionsList);
        return tag;
    }

    public void remove(ServerWorld level) {
        this.minions.forEach(uuid -> {
            Entity e = level.getEntity(uuid);
            if (e != null) {
                e.remove();
            }
        });
    }
}
