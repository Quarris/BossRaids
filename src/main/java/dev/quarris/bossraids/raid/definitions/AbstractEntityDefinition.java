package dev.quarris.bossraids.raid.definitions;

import dev.quarris.bossraids.ModRef;
import net.minecraft.entity.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractEntityDefinition<T extends Entity> {

    protected EntityType<T> entity;
    protected String name;
    protected float health;
    protected Set<EffectDefinition> effects = new HashSet<>();
    protected CompoundNBT nbt;

    public AbstractEntityDefinition() {
    }

    @SuppressWarnings("unchecked")
    public AbstractEntityDefinition(CompoundNBT tag) {
        this.entity = (EntityType<T>) ForgeRegistries.ENTITIES.getValue(new ResourceLocation(tag.getString("Entity")));
        if (tag.contains("Name", Constants.NBT.TAG_STRING)) {
            this.name = tag.getString("Name");
        }
        if (tag.contains("Health", Constants.NBT.TAG_FLOAT)) {
            this.health = tag.getFloat("Health");
        }
        if (tag.contains("Nbt", Constants.NBT.TAG_COMPOUND)) {
            this.nbt = tag.getCompound("Nbt");
        }
        if (tag.contains("Effects", Constants.NBT.TAG_LIST)) {
            for (INBT nbt : tag.getList("Effects", Constants.NBT.TAG_COMPOUND)) {
                this.effects.add(new EffectDefinition((CompoundNBT) nbt));
            }
        }
    }

    public T create(ServerWorld level, Vector3d pos) {
        if (this.entity == EntityType.ENDER_DRAGON) {
            // TODO Special case for ender dragon, start a dragon fight (if in valid dim).
            ModRef.LOGGER.error("Ender Dragon is not a valid boss entity.");
            return null;
        }

        T e = this.entity.create(level, this.nbt, StringUtils.isNullOrEmpty(this.name) ? null : new StringTextComponent(this.name), null, new BlockPos(pos), SpawnReason.EVENT, true, false);

        if (e instanceof MobEntity) {
            ((MobEntity) e).setPersistenceRequired();
        }

        if (e == null) {
            ModRef.LOGGER.warn("Could not create boss entity {}", this);
            return null;
        }

        this.editEntity(level, pos, e);

        return e;
    }

    protected abstract void editEntity(ServerWorld level, Vector3d pos, T entity);

    public CompoundNBT serialize() {
        CompoundNBT tag = new CompoundNBT();
        tag.putString("Entity", this.entity.getRegistryName().toString());
        if (!StringUtils.isNullOrEmpty(this.name)) {
            tag.putString("Name", this.name);
        }
        if (this.health > 0) {
            tag.putFloat("Health", this.health);
        }
        if (this.nbt != null) {
            tag.put("Nbt", this.nbt);
        }
        if (this.effects != null && !this.effects.isEmpty()) {
            ListNBT effectsList = new ListNBT();
            for (EffectDefinition effectDefinition : this.effects) {
                effectsList.add(effectDefinition.serialize());
            }
            tag.put("Effects", effectsList);
        }
        return tag;
    }

}
