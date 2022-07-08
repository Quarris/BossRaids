package dev.quarris.bossraids.waves;

import net.minecraft.entity.EntityType;
import net.minecraft.util.RangedInteger;
import net.minecraft.util.ResourceLocation;

public class MinionEntityDefinition {
    private String leader;
    private EntityType<?> entity;
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
