package dev.quarris.bossraids.raid.definitions;

import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class BossRaidDefinition {

    private ResourceLocation loot;
    private List<WaveDefinition> waves = new ArrayList<>();
    private int radius;
    private boolean disableFlight;


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

    public boolean isFlightDisabled() {
        return this.disableFlight;
    }

    @Override
    public String toString() {
        return "BossWaveDefinition{" +
                "waves=" + waves +
                '}';
    }

    public int getRadius() {
        return this.radius;
    }

    public int getWaveCount() {
        return this.waves.size();
    }
}
