package dev.quarris.bossraids.raid.definitions;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.Effect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class EffectDefinition {

    public final Effect effect;
    public final int amplifier;

    public EffectDefinition(Effect effect, int amplifier) {
        this.effect = effect;
        this.amplifier = amplifier;
    }

    public EffectDefinition(CompoundNBT tag) {
        this.effect = ForgeRegistries.POTIONS.getValue(new ResourceLocation(tag.getString("Effect")));
        this.amplifier = tag.getInt("Amplifier");
    }

    public CompoundNBT serialize() {
        CompoundNBT tag = new CompoundNBT();
        tag.putString("Effect", this.effect.getRegistryName().toString());
        tag.putInt("Amplifier", this.amplifier);
        return tag;
    }

    @Override
    public String toString() {
        return "EffectDefinition{" +
                "effect=" + effect +
                ", amplifier=" + amplifier +
                '}';
    }
}
