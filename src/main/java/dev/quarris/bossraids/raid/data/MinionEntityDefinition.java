package dev.quarris.bossraids.raid.data;

import net.minecraft.entity.EntityType;
import net.minecraft.util.RangedInteger;

public class MinionEntityDefinition {

    private EntityType<?> entity;
    private RangedInteger count;

    @Override
    public String toString() {
        return "MinionEntityDefinition{" +
                "entity='" + entity + '\'' +
                ", count=" + count +
                '}';
    }
}
