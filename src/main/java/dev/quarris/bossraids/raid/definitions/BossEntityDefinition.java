package dev.quarris.bossraids.raid.definitions;

import dev.quarris.bossraids.util.offsets.IOffset;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
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

    public BossEntityDefinition(CompoundNBT tag) {
        super(tag);
    }

    @Override
    protected void editEntity(ServerWorld level, Vector3d pos, LivingEntity boss) {
        if (this.offset != null) {
            Vector3d offPos = this.offset.getOffset(pos);
            if (World.isOutsideBuildHeight(MathHelper.floor(offPos.y))) {
                int height = level.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, MathHelper.floor(offPos.x), MathHelper.floor(offPos.z));
                offPos = new Vector3d(offPos.x, height, offPos.z);
            }
            boss.setPos(offPos.x, offPos.y, offPos.z);
        }
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
