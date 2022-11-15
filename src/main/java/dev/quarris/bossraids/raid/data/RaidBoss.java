package dev.quarris.bossraids.raid.data;

import dev.quarris.bossraids.raid.definitions.BossEntityDefinition;
import dev.quarris.bossraids.raid.definitions.EntityDefinition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;

import java.util.*;
import java.util.stream.Collectors;

public class RaidBoss {

    public final BossEntityDefinition definition;
    public final BlockPos spawnPos;
    private final UUID uuid;
    private final UUID mountUuid;
    private final UUID riderUuid;
    private final List<UUID> subBosses = new ArrayList<>();
    private final Set<Minions> minions = new HashSet<>();

    public RaidBoss(LivingEntity boss, LivingEntity mount, LivingEntity rider, BossEntityDefinition definition) {
        this.definition = definition;
        this.spawnPos = boss.blockPosition();
        this.uuid = boss.getUUID();
        this.mountUuid = mount != null ? mount.getUUID() : null;
        this.riderUuid = rider != null ? rider.getUUID() : null;
        if (definition.minions != null) {
            definition.minions.forEach(def -> this.minions.add(new Minions(def)));
        }
    }

    public RaidBoss(CompoundNBT tag) {
        this.definition = new BossEntityDefinition(tag.getCompound("Definition"));
        this.spawnPos = NBTUtil.readBlockPos(tag.getCompound("SpawnPos"));
        this.uuid = tag.getUUID("Id");
        this.mountUuid = tag.hasUUID("MountId") ? tag.getUUID("MountId") : null;
        this.riderUuid = tag.hasUUID("RiderId") ? tag.getUUID("RiderId") : null;
        for (INBT nbt : tag.getList("Minions", Constants.NBT.TAG_COMPOUND)) {
            this.minions.add(new Minions((CompoundNBT) nbt));
        }

        for (INBT nbt : tag.getList("SubBosses", Constants.NBT.TAG_INT_ARRAY)) {
            this.subBosses.add(NBTUtil.loadUUID(nbt));
        }
    }

    public void update(ServerWorld level, long raidId) {
        this.resetEntityPosition(this.getBoss(level));
        this.resetEntityPosition(this.getRider(level));
        this.resetEntityPosition(this.getMount(level));
        for (LivingEntity subBoss : this.getSubBosses(level)) {
            this.resetEntityPosition(subBoss);
        }
        
        for (Minions m : this.minions) {
            m.update(level);
            if (m.shouldSpawn()) {
                this.spawnMinions(level, raidId, m);
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

        for (LivingEntity subBoss : this.getSubBosses(level)) {
            health += subBoss.getHealth();
        }

        return health;
    }

    // Returns true if the cooldown should reset
    private boolean spawnMinions(ServerWorld level, long raidId, Minions minions) {
        int toSpawn = minions.toSpawn();
        if (toSpawn > 0) {
            for (int i = 0; i < toSpawn; i++) {
                Entity spawn = minions.createSpawn(level, raidId, this.getMain(level).position());
                level.addFreshEntity(spawn);
            }
        }

        return true;
    }

    private List<LivingEntity> getSubBosses(ServerWorld level) {
        return this.subBosses.stream().map(uuid -> this.getEntity(level, uuid)).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private LivingEntity getEntity(ServerWorld level, UUID uuid) {
        if (uuid == null) {
            return null;
        }

        Entity entity = level.getEntity(uuid);
        if (entity instanceof SlimeEntity) {
            SlimeEntity slimeEntity = ((SlimeEntity) entity);
            if (slimeEntity.isTiny() && !slimeEntity.isAlive()) {
                return null;
            }
            return slimeEntity;
        }

        if (entity != null && entity.isAlive()) {
            return (LivingEntity) entity;
        }

        return null;
    }

    private LivingEntity getMain(ServerWorld level) {
        // Try boss first
        LivingEntity main = this.getEntity(level, this.uuid);
        if (main != null) {
            return main;
        }

        // Try rider second
        main = this.getEntity(level, this.riderUuid);
        if (main != null) {
            return main;
        }

        // Try mount last
        main = this.getEntity(level, this.mountUuid);
        if (main != null) {
            return main;
        }

        List<LivingEntity> subBosses = this.getSubBosses(level);
        if (!subBosses.isEmpty()) {
            return subBosses.get(0);
        }

        return null;
    }

    public LivingEntity getBoss(ServerWorld level) {
        return this.getEntity(level, this.uuid);
    }

    public LivingEntity getRider(ServerWorld level) {
        return this.getEntity(level, this.riderUuid);
    }

    public LivingEntity getMount(ServerWorld level) {
        return this.getEntity(level, this.mountUuid);
    }

    public boolean shouldRemove(ServerWorld level) {
        LivingEntity boss = this.getMain(level);

        if (boss instanceof SlimeEntity) {
            SlimeEntity slimeBoss = ((SlimeEntity) boss);
            if (slimeBoss.isTiny() && !slimeBoss.isAlive()) {
                return true;
            }

            return false;
        }
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

        for (LivingEntity subBoss : this.getSubBosses(level)) {
            subBoss.remove();
        }
    }

    public void addSubBoss(UUID uuid) {
        this.subBosses.add(uuid);
    }

    public CompoundNBT serialize() {
        CompoundNBT tag = new CompoundNBT();
        tag.put("Definition", this.definition.serialize());
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

        ListNBT subBossesTag = new ListNBT();
        for (UUID subBossUuid : this.subBosses) {
            subBossesTag.add(NBTUtil.createUUID(subBossUuid));
        }

        tag.put("SubBosses", subBossesTag);
        return tag;
    }
}
