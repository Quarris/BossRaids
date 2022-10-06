package dev.quarris.bossraids.raid;

import com.google.common.collect.Maps;
import dev.quarris.bossraids.ModRef;
import dev.quarris.bossraids.content.KeystoneTileEntity;
import dev.quarris.bossraids.raid.data.BossRaid;
import dev.quarris.bossraids.raid.data.RaidState;
import dev.quarris.bossraids.raid.definitions.BossRaidDefinition;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

public class BossRaidManager extends WorldSavedData {

    private final Map<Long, BossRaid> raidMap = Maps.newHashMap();
    private final ServerWorld level;
    private int tick;

    public static BossRaidManager getBossRaids(ServerWorld level) {
        return level.getDataStorage().computeIfAbsent(() -> new BossRaidManager(level), getFileId(level));
    }

    public BossRaidManager(ServerWorld level) {
        super(getFileId(level));
        this.level = level;
        this.setDirty();
    }

    public Optional<Long> tryActivateRaid(ServerPlayerEntity player, BlockPos pos, BossRaidDefinition definition, ItemStack item) {
        if (this.raidMap.containsKey(pos.asLong())) {
            ModRef.LOGGER.warn("Raid already exists at position {}", pos);
            return Optional.empty();
        }

        // Check if the first wave's requirement is matched by the activation item
        if (definition.getWave(0).getRequirements().stream().anyMatch(req -> req.getIngredient().test(item))) {
            BossRaid raid = this.createRaid(player, pos, BossRaidDataManager.INST.getId(definition));

            if (!raid.tryInsertRequirement(player, item)) {
                this.raidMap.remove(raid.getId());
                return Optional.empty();
            }

            return Optional.of(raid.getId());
        }

        // Failed to activate raid with given item.
        return Optional.empty();
    }

    public BossRaid get(long id) {
        if (raidMap.containsKey(id)) {
            return this.raidMap.get(id);
        }

        return null;
    }

    public void update() {
        this.tick++;
        Iterator<BossRaid> ite = this.raidMap.values().iterator();

        while (ite.hasNext()) {
            BossRaid raid = ite.next();
            // If the block entity got removed, remove the raid
            TileEntity tile = this.level.getBlockEntity(raid.getCenter());
            if (!(tile instanceof KeystoneTileEntity)) {
                this.stopRaid(raid, null);
                ite.remove();
                continue;
            }

            // If the keystone is no longer active or has different raid id, remove this raid.
            KeystoneTileEntity keystone = (KeystoneTileEntity) tile;
            if (!keystone.isActive() || keystone.getRaidId() != raid.getId()) {
                this.stopRaid(raid, keystone);
                ite.remove();
                continue;
            }

            if (raid.getState() == RaidState.INACTIVE) {
                this.stopRaid(raid, keystone);
                ite.remove();
            }

            // If the raid has finished, remove it
            if (raid.getState() == RaidState.COMPLETED) {
                if (!this.level.isClientSide()) {
                    raid.spawnFinalLoot();
                }
                this.stopRaid(raid, keystone);
                ite.remove();
                continue;
            }
            raid.update();
        }

        if (this.tick % 200 == 0) {
            this.setDirty();
        }
    }

    private void stopRaid(BossRaid raid, KeystoneTileEntity tile) {
        raid.stop();
        this.setDirty();
        if (tile != null) {
            tile.clearRaid();
        }
    }

    public BossRaid createRaid(ServerPlayerEntity player, BlockPos center, ResourceLocation defId) {
        if (!this.raidMap.containsKey(center.asLong())) {
            BossRaid raid = new BossRaid(this.level, center, center.asLong(), defId);
            raid.setup(player);
            this.raidMap.put(raid.getId(), raid);
            this.setDirty();
            return raid;
        }

        return null;
    }

    public void load(CompoundNBT tag) {
        this.tick = tag.getInt("Tick");
        ListNBT listnbt = tag.getList("Raids", Constants.NBT.TAG_COMPOUND);

        for (int i = 0; i < listnbt.size(); ++i) {
            CompoundNBT compoundnbt = listnbt.getCompound(i);
            BossRaid raid = new BossRaid(this.level, compoundnbt);
            this.raidMap.put(raid.getId(), raid);
        }
    }

    public CompoundNBT save(CompoundNBT tag) {
        tag.putInt("Tick", this.tick);
        ListNBT listnbt = new ListNBT();

        for (BossRaid raid : this.raidMap.values()) {
            listnbt.add(raid.serialize());
        }

        tag.put("Raids", listnbt);
        return tag;
    }

    public static String getFileId(ServerWorld world) {
        return "bossraids_" + world.dimension().location().toString().replace(":", "_");
    }

    /*@Nullable
    public Raid getNearbyRaid(BlockPos p_215174_1_, int p_215174_2_) {
        Raid raid = null;
        double d0 = (double)p_215174_2_;

        for(Raid raid1 : this.raidMap.values()) {
            double d1 = raid1.getCenter().distSqr(p_215174_1_);
            if (raid1.isActive() && d1 < d0) {
                raid = raid1;
                d0 = d1;
            }
        }

        return raid;
    }*/
}
