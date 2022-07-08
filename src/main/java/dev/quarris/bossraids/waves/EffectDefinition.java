package dev.quarris.bossraids.waves;

import net.minecraft.potion.Effect;

public class EffectDefinition {

    private Effect effect;
    private int amplifier;

    @Override
    public String toString() {
        return "EffectDefinition{" +
                "effect=" + effect +
                ", amplifier=" + amplifier +
                '}';
    }
}
