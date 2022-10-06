package dev.quarris.bossraids.raid.data;

import dev.quarris.bossraids.raid.definitions.BossEntityDefinition;
import dev.quarris.bossraids.raid.definitions.MinionEntityDefinition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class RaidBoss {

    private final UUID uuid;
    private final Set<Minions> minions = new HashSet<>();

    public RaidBoss(LivingEntity entity, BossEntityDefinition definition) {
        this.uuid = entity.getUUID();
        if (definition.minions != null) {
            definition.minions.forEach(def -> this.minions.add(new Minions(def)));
        }
    }

    public RaidBoss(CompoundNBT tag) {
        this.uuid = tag.getUUID("Id");
        for (INBT nbt : tag.getList("Minions", Constants.NBT.TAG_COMPOUND)) {
            this.minions.add(new Minions((CompoundNBT) nbt));
        }
    }

    public void update(ServerWorld level) {
        for (Minions m : this.minions) {
            m.update(level);
            if (m.shouldSpawn()) {
                this.spawnMinions(level, m);
                m.resetCooldown();
            }
        }
    }

    // Returns true if the cooldown should reset
    private boolean spawnMinions(ServerWorld level, Minions minions) {
        int toSpawn = minions.toSpawn();
        if (toSpawn > 0) {
            for (int i = 0; i < toSpawn; i++) {
                Entity spawn = minions.createSpawn(level, this.getEntity(level).position());
                level.addFreshEntity(spawn);
            }
        }

        return true;
    }

    public LivingEntity getEntity(ServerWorld level) {
        Entity boss = level.getEntity(this.uuid);
        if (boss != null) {
            return (LivingEntity) boss;
        }

        return null;
    }

    public boolean shouldRemove(ServerWorld level) {
        LivingEntity boss = this.getEntity(level);
        return boss == null || !boss.isAlive();
    }

    public void remove(ServerWorld level) {
        for (Minions m : this.minions) {
            m.remove(level);
        }
        LivingEntity boss = this.getEntity(level);
        if (boss != null) {
            boss.remove();
        }
    }

    public CompoundNBT serialize() {
        CompoundNBT tag = new CompoundNBT();
        tag.putUUID("Id", this.uuid);
        ListNBT minionsNbt = new ListNBT();
        for (Minions minions : this.minions) {
            minionsNbt.add(minions.serialize());
        }
        tag.put("Minions", minionsNbt);
        return tag;
    }
}
