package dev.quarris.bosswaves.waves;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BossWaveDefinition {


    private List<WaveDefinition> waves = new ArrayList<>();
    private List<WaveDefinition> unmodifiableWaves;

    public List<WaveDefinition> getWaves() {
        if (this.unmodifiableWaves == null) {
            this.unmodifiableWaves = Collections.unmodifiableList(this.waves);
        }

        return this.unmodifiableWaves;
    }

    @Override
    public String toString() {
        return "BossWaveDefinition{" +
                "waves=" + waves +
                '}';
    }
}
