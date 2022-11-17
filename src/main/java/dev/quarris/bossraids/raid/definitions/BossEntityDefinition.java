package dev.quarris.bossraids.raid.definitions;

import dev.quarris.bossraids.util.offsets.IOffset;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;

import java.util.HashSet;
import java.util.Set;

public class BossEntityDefinition extends EntityDefinition<LivingEntity> {

    private String id;
    public Set<MinionEntityDefinition> minions = new HashSet<>();
    public EntityDefinition<? extends LivingEntity> mount;
    public EntityDefinition<? extends LivingEntity> rider;
    private IOffset offset;

    public BossEntityDefinition(String id, Set<MinionEntityDefinition> minions, EntityDefinition<? extends LivingEntity> mount, EntityDefinition<? extends LivingEntity> rider, IOffset offset) {
        this.id = id;
        this.minions = minions;
        this.mount = mount;
        this.rider = rider;
        this.offset = offset;
    }

    public BossEntityDefinition(CompoundNBT tag) {
        super(tag);
    }

    @Override
    public void customiseEntity(long raidId, ServerWorld level, Vector3d pos, LivingEntity boss) {
        if (this.offset != null) {
            Vector3d offPos = this.offset.getOffset(pos);
            if (World.isOutsideBuildHeight(MathHelper.floor(offPos.y))) {
                int height = level.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, MathHelper.floor(offPos.x), MathHelper.floor(offPos.z));
                offPos = new Vector3d(offPos.x, height, offPos.z);
            }
            boss.setPos(offPos.x, offPos.y, offPos.z);
        }

        this.applyBossData(raidId, level, boss);
    }

    public CompoundNBT applyBossData(long raidId, ServerWorld level, LivingEntity boss, String bossType) {
        CompoundNBT bossData = new CompoundNBT();
        bossData.putString("BossId", this.id);
        bossData.putLong("RaidId", raidId);
        if (!StringUtils.isNullOrEmpty(bossType)) {
            bossData.putString("BossType", bossType);
        }
        boss.getPersistentData().put("BossRaidData", bossData);
        return bossData;
    }

    public CompoundNBT applyBossData(long raidId, ServerWorld level, LivingEntity boss) {
        return this.applyBossData(raidId, level, boss, "");
    }

    public String getId() {
        return this.id;
    }

    @Override
    public String toString() {
        return "BossEntityDefinition{" +
            "id='" + id + '\'' +
            ", entity=" + entity +
            ", name='" + name + '\'' +
            ", effects=" + effects +
            ", nbt=" + nbt +
            ", minions=" + minions +
            '}';
    }
}
