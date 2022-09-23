package dev.quarris.bossraids.raid.data;

import dev.quarris.bossraids.ModRef;
import dev.quarris.bossraids.raid.offsets.IOffset;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;

import java.util.List;

public class BossEntityDefinition {

    private String id;
    private EntityType<? extends LivingEntity> entity;
    private String name;
    private float health;
    private List<EffectDefinition> effects;
    private CompoundNBT nbt;

    public List<MinionEntityDefinition> minions;

    private IOffset offset;

    public LivingEntity create(ServerWorld level, BlockPos pos) {
        if (this.entity == EntityType.ENDER_DRAGON) {
            // TODO Special case for ender dragon, start a dragon fight (if in valid dim).
            ModRef.LOGGER.error("Ender Dragon is not a valid boss entity.");
            return null;
        }

        LivingEntity boss = this.entity.create(level, this.nbt, StringUtils.isNullOrEmpty(this.name) ? null : new StringTextComponent(this.name), null, pos, SpawnReason.EVENT, true, false);

        if (boss == null) {
            ModRef.LOGGER.warn("Could not create boss entity {} for id {}.", this.entity, this.id);
            return null;
        }

        if (this.offset != null) {
            Vector3d offPos = this.offset.getOffset(Vector3d.atCenterOf(pos));
            if (World.isOutsideBuildHeight(MathHelper.floor(offPos.y))) {
                int height = level.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, MathHelper.floor(offPos.x), MathHelper.floor(offPos.z));
                offPos = new Vector3d(offPos.x, height, offPos.z);
            }
            boss.setPos(offPos.x, offPos.y, offPos.z);
        }

        return boss;
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
            ", health=" + health +
            ", effects=" + effects +
            ", nbt=" + nbt +
            ", minions=" + minions +
            '}';
    }
}
