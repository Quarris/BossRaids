package dev.quarris.bosswaves;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import dev.quarris.bosswaves.waves.BossWaveDefinition;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class BossWaveManager extends JsonReloadListener {

    public static BossWaveManager INST;
    private Gson gson;

    private Map<ResourceLocation, BossWaveDefinition> bossWaves = new HashMap();

    public BossWaveManager(Gson gson) {
        super(gson, "boss_waves");
        this.gson = gson;
        INST = this;
    }

    public BossWaveDefinition getBossWave(ResourceLocation id) {
        return this.bossWaves.get(id);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> entries, IResourceManager manager, IProfiler profiler) {
        entries.forEach((name, baseJson) -> {
            if (!baseJson.isJsonObject()) {
                ModRef.LOGGER.error("BossWave definition has to be an object: {}", name);
                return;
            }
            try {
                BossWaveDefinition bossWave = this.gson.fromJson(baseJson, BossWaveDefinition.class);
                if (bossWave.getWaves().isEmpty()) {
                    ModRef.LOGGER.error("Boss Wave Definition '{}' cannot have an empty list of waves.", name);
                    return;
                }
                this.bossWaves.put(name, bossWave);
            } catch (JsonParseException e) {
                ModRef.LOGGER.error("Could not parse BossWave " + name, e);
            }
        });
    }
}
