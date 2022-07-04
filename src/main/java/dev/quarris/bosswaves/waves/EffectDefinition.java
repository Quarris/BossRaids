package dev.quarris.bosswaves.waves;

import net.minecraft.util.ResourceLocation;

public class EffectDefinition {

    private ResourceLocation effect;
    private int amplifier;

    @Override
    public String toString() {
        return "EffectDefinition{" +
                "effect=" + effect +
                ", amplifier=" + amplifier +
                '}';
    }
}
