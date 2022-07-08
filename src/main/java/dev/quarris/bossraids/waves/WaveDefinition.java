package dev.quarris.bossraids.waves;

import dev.quarris.bossraids.util.ItemRequirement;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.List;

public class WaveDefinition {

    public final ResourceLocation loot;
    public final String bossbar;
    private final List<ItemRequirement> requirements;
    public final List<BossEntityDefinition> bosses;
    public final List<MinionEntityDefinition> minions;

    public WaveDefinition(ResourceLocation loot, String bossbar, List<ItemRequirement> requirements, List<BossEntityDefinition> bosses, List<MinionEntityDefinition> minions) {
        this.loot = loot;
        this.bossbar = bossbar;
        this.requirements = requirements;
        this.bosses = bosses;
        this.minions = minions;
    }

    public List<ItemRequirement> getRequirements() {
        if (this.requirements == null) {
            return Collections.emptyList();
        }

        return this.requirements;
    }

    @Override
    public String toString() {
        return "Wave{" +
                "loot=" + loot +
                ", bossbarName='" + bossbar + '\'' +
                ", requirements=" + requirements +
                ", bosses=" + bosses +
                ", minions=" + minions +
                '}';
    }
}
