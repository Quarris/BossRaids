package dev.quarris.bossraids.raid.definitions;

import dev.quarris.bossraids.ModRef;
import dev.quarris.bossraids.raid.data.BossRaid;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashSet;
import java.util.Set;

public class EntityDefinition<T extends Entity> {

    public static final String DAMAGE_IMMUNITIES_TAG = ModRef.res("damage_immunities").toString();

    protected EntityType<T> entity;
    protected String name;
    protected Set<EffectDefinition> effects;
    protected Set<AttributeDefinition> attributes;
    protected CompoundNBT nbt;
    protected Set<String> damageImmunities;

    public EntityDefinition() {
    }

    @SuppressWarnings("unchecked")
    public EntityDefinition(CompoundNBT tag) {
        this.entity = (EntityType<T>) ForgeRegistries.ENTITIES.getValue(new ResourceLocation(tag.getString("Entity")));
        if (tag.contains("Name", Constants.NBT.TAG_STRING)) {
            this.name = tag.getString("Name");
        }
        if (tag.contains("Attributes", Constants.NBT.TAG_LIST)) {
            this.attributes = new HashSet<>();
            for (INBT nbt : tag.getList("Attributes", Constants.NBT.TAG_COMPOUND)) {
                this.attributes.add(new AttributeDefinition((CompoundNBT) nbt));
            }
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

    public T create(ServerWorld level, long raidId, Vector3d pos) {
        if (this.entity == EntityType.ENDER_DRAGON) {
            // TODO Special case for ender dragon, start a dragon fight (if in valid dim).
            ModRef.LOGGER.error("Ender Dragon is not a valid boss entity.");
            return null;
        }

        T entity = this.entity.create(level, this.nbt, StringUtils.isNullOrEmpty(this.name) ? null : new StringTextComponent(this.name), null, new BlockPos(pos), SpawnReason.EVENT, true, false);


        if (entity == null) {
            ModRef.LOGGER.warn("Could not create boss entity {}", this);
            return null;
        }

        this.configureEntity(raidId, level, pos, entity);
        this.customiseEntity(raidId, level, pos, entity);
        return entity;
    }

    public void configureEntity(long raidId, ServerWorld level, Vector3d pos, T entity) {
        if (entity instanceof LivingEntity) {
            LivingEntity le = (LivingEntity) entity;
            if (this.attributes != null) {
                for (AttributeDefinition attrDef : this.attributes) {
                    ModifiableAttributeInstance inst = le.getAttribute(attrDef.attribute);
                    if (inst != null) {
                        AttributeDefinition.Operation operation = attrDef.operation;
                        if (operation == null) operation = AttributeDefinition.Operation.BASE;
                        if (operation == AttributeDefinition.Operation.BASE) {
                            inst.setBaseValue(attrDef.level);
                        } else {
                            inst.addPermanentModifier(new AttributeModifier("Boss Raid Attribute", attrDef.level, attrDef.operation.getOp()));
                        }
                    } else {
                        ModRef.LOGGER.warn("Entity '{}' does not have attribute '{}'", this.entity.getRegistryName(), attrDef.attribute.getRegistryName());
                    }
                }
                le.setHealth(le.getMaxHealth());
            }

            if (this.effects != null) {
                for (EffectDefinition effectDef : this.effects) {
                    le.addEffect(new EffectInstance(effectDef.effect, Integer.MAX_VALUE, effectDef.amplifier, false, false));
                }
            }

            if (this.damageImmunities != null && !this.damageImmunities.isEmpty()) {
                ListNBT damageImmnunities = new ListNBT();
                for (String damage : this.damageImmunities) {
                    damageImmnunities.add(StringNBT.valueOf(damage));
                }
                le.getPersistentData().put(DAMAGE_IMMUNITIES_TAG, damageImmnunities);
            }

            level.getScoreboard().addPlayerToTeam(entity.getStringUUID(), BossRaid.RAID_TEAM);

            if (entity instanceof MobEntity) {
                ((MobEntity) entity).setPersistenceRequired();
            }
        }

        if (this.nbt != null) {
            CompoundNBT saved = new CompoundNBT();
            entity.save(saved);
            saved.merge(this.nbt);
            entity.load(saved);
        }
    }

    public void customiseEntity(long raidId, ServerWorld level, Vector3d pos, T entity) {
    }

    public CompoundNBT serialize() {
        CompoundNBT tag = new CompoundNBT();
        tag.putString("Entity", this.entity.getRegistryName().toString());
        if (!StringUtils.isNullOrEmpty(this.name)) {
            tag.putString("Name", this.name);
        }
        if (this.attributes != null && !this.attributes.isEmpty()) {
            ListNBT attirbutesList = new ListNBT();
            for (AttributeDefinition attributeDefinition : this.attributes) {
                attirbutesList.add(attributeDefinition.serialize());
            }
            tag.put("Attributes", attirbutesList);
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
