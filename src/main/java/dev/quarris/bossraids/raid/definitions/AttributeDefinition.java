package dev.quarris.bossraids.raid.definitions;

import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class AttributeDefinition {

    public final Attribute attribute;
    public final double level;

    public AttributeDefinition(Attribute attribute, float level) {
        this.attribute = attribute;
        this.level = level;
    }

    public AttributeDefinition(CompoundNBT tag) {
        this.attribute = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(tag.getString("Attribute")));
        this.level = tag.getDouble("Level");
    }

    public CompoundNBT serialize() {
        CompoundNBT tag = new CompoundNBT();
        tag.putString("Attribute", this.attribute.getRegistryName().toString());
        tag.putDouble("Level", this.level);
        return tag;
    }
}
