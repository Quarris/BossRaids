package dev.quarris.bossraids.raid.data;

import dev.quarris.bossraids.raid.definitions.BossEntityDefinition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class RaidBoss {

    private final BlockPos spawnPos;
    private final UUID uuid;
    private final UUID mountUuid;
    private final UUID riderUuid;
    private final Set<Minions> minions = new HashSet<>();

    public RaidBoss(LivingEntity boss, LivingEntity mount, LivingEntity rider, BossEntityDefinition definition) {
        this.spawnPos = boss.blockPosition();
        this.uuid = boss.getUUID();
        this.mountUuid = mount != null ? mount.getUUID() : null;
        this.riderUuid = rider != null ? rider.getUUID() : null;
        if (definition.minions != null) {
            definition.minions.forEach(def -> this.minions.add(new Minions(def)));
        }
    }

    public RaidBoss(CompoundNBT tag) {
        this.spawnPos = NBTUtil.readBlockPos(tag.getCompound("SpawnPos"));
        this.uuid = tag.getUUID("Id");
        this.mountUuid = tag.hasUUID("MountId") ? tag.getUUID("MountId") : null;
        this.riderUuid = tag.hasUUID("RiderId") ? tag.getUUID("RiderId") : null;
        for (INBT nbt : tag.getList("Minions", Constants.NBT.TAG_COMPOUND)) {
            this.minions.add(new Minions((CompoundNBT) nbt));
        }
    }

    public void update(ServerWorld level) {
        this.resetEntityPosition(this.getBoss(level));
        this.resetEntityPosition(this.getRider(level));
        this.resetEntityPosition(this.getMount(level));
        for (Minions m : this.minions) {
            m.update(level);
            if (m.shouldSpawn()) {
                this.spawnMinions(level, m);
                m.resetCooldown();
            }
        }
    }

    private void resetEntityPosition(LivingEntity entity) {
        if (entity != null && !World.isInWorldBounds(entity.blockPosition())) {
            entity.setPos(this.spawnPos.getX(), this.spawnPos.getY(), this.spawnPos.getZ());
            entity.fallDistance = 0;
        }
    }

    public float getCurrentHealth(ServerWorld level) {
        float health = 0;

        LivingEntity boss = this.getBoss(level);
        if (boss != null) {
            health += boss.getHealth();
        }

        LivingEntity mount = this.getMount(level);
        if (mount != null) {
            health += mount.getHealth();
        }

        LivingEntity rider = this.getRider(level);
        if (rider != null) {
            health += rider.getHealth();
        }

        return health;
    }

    // Returns true if the cooldown should reset
    private boolean spawnMinions(ServerWorld level, Minions minions) {
        int toSpawn = minions.toSpawn();
        if (toSpawn > 0) {
            for (int i = 0; i < toSpawn; i++) {
                Entity spawn = minions.createSpawn(level, this.getMain(level).position());
                level.addFreshEntity(spawn);
            }
        }

        return true;
    }

    private LivingEntity getMain(ServerWorld level) {
        // Try boss first
        LivingEntity main = this.getBoss(level);
        if (main != null) {
            return main;
        }

        // Try rider second
        main = this.getRider(level);
        if (main != null) {
            return main;
        }

        // Try mount last
        main = this.getMount(level);
        return main;
    }

    public LivingEntity getBoss(ServerWorld level) {
        Entity boss = level.getEntity(this.uuid);
        if (boss != null && boss.isAlive()) {
            return (LivingEntity) boss;
        }

        return null;
    }

    public LivingEntity getRider(ServerWorld level) {
        if (this.riderUuid == null) {
            return null;
        }

        Entity boss = level.getEntity(this.riderUuid);
        if (boss != null && boss.isAlive()) {
            return (LivingEntity) boss;
        }

        return null;
    }

    public LivingEntity getMount(ServerWorld level) {
        if (this.mountUuid == null) {
            return null;
        }

        Entity boss = level.getEntity(this.mountUuid);
        if (boss != null && boss.isAlive()) {
            return (LivingEntity) boss;
        }

        return null;
    }

    public boolean shouldRemove(ServerWorld level) {
        LivingEntity boss = this.getMain(level);
        return boss == null || !boss.isAlive();
    }

    public void remove(ServerWorld level) {
        for (Minions m : this.minions) {
            m.remove(level);
        }

        LivingEntity boss = this.getBoss(level);
        if (boss != null) {
            boss.remove();
        }

        LivingEntity rider = this.getRider(level);
        if (rider != null) {
            rider.remove();
        }

        LivingEntity mount = this.getMount(level);
        if (mount != null) {
            mount.remove();
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

        if (this.riderUuid != null) {
            tag.putUUID("RiderId", this.riderUuid);
        }

        if (this.mountUuid != null) {
            tag.putUUID("MountId", this.mountUuid);
        }

        tag.put("SpawnPos", NBTUtil.writeBlockPos(this.spawnPos));
        return tag;
    }
}
