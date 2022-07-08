package dev.quarris.bossraids.waves;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;

import java.util.List;

public class BossEntityDefinition {

    private String id;
    private EntityType<? extends LivingEntity> entity;
    private String name;
    private float health;
    private List<EffectDefinition> effects;
    private CompoundNBT nbt;

    public LivingEntity create(ServerWorld level, BlockPos pos) {
        LivingEntity boss = this.entity.create(level, this.nbt, StringUtils.isNullOrEmpty(this.name) ? null : new StringTextComponent(this.name), null, pos, SpawnReason.EVENT, true, false);
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
                '}';
    }
}
