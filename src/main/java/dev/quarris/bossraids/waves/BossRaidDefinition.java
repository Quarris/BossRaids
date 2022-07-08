package dev.quarris.bossraids.waves;

import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class BossRaidDefinition {

    private ResourceLocation loot;
    private List<WaveDefinition> waves = new ArrayList<>();

    public boolean isEmpty() {
        return this.waves.isEmpty();
    }

    public ResourceLocation getLootTableId() {
        return this.loot;
    }

    public WaveDefinition getWave(int index) {
        if (index >= this.waves.size()) {
            return null;
        }

        return this.waves.get(index);
    }

    @Override
    public String toString() {
        return "BossWaveDefinition{" +
                "waves=" + waves +
                '}';
    }
}
