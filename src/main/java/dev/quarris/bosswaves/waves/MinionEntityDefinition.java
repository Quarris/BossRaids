package dev.quarris.bosswaves.waves;

import net.minecraft.util.RangedInteger;
import net.minecraft.util.ResourceLocation;

public class MinionEntityDefinition {
    private String leader;
    private ResourceLocation entity;
    private RangedInteger count;

    @Override
    public String toString() {
        return "MinionEntityDefinition{" +
                "leader='" + leader + '\'' +
                ", entity=" + entity +
                ", count=" + count +
                '}';
    }
}
