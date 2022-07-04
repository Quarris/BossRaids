package dev.quarris.bosswaves.waves;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class WaveDefinition {

    private ResourceLocation loot;
    private String bossbarName;
    private ItemStack activator;
    private List<BossEntityDefinition> bosses;
    private List<MinionEntityDefinition> minions;

    @Override
    public String toString() {
        return "Wave{" +
                "loot=" + loot +
                ", bossbarName='" + bossbarName + '\'' +
                ", activator=" + activator +
                ", bosses=" + bosses +
                ", minions=" + minions +
                '}';
    }
}
