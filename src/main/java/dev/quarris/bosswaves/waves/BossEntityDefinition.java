package dev.quarris.bosswaves.waves;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class BossEntityDefinition {

    private String id;
    private ResourceLocation entity;
    private String name;
    private float health;
    private List<EffectDefinition> effects;
    private CompoundNBT nbt;

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
