package dev.quarris.bossraids.raid.data;

import net.minecraft.entity.EntityType;
import net.minecraft.util.RangedInteger;

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
