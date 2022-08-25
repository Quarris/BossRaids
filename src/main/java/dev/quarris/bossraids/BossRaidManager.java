package dev.quarris.bossraids;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import dev.quarris.bossraids.raid.data.BossRaidDefinition;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class BossRaidManager extends JsonReloadListener {

    public static BossRaidManager INST;
    private Gson gson;

    private Map<ResourceLocation, BossRaidDefinition> bossraids = new HashMap();

    public BossRaidManager(Gson gson) {
        super(gson, "boss_waves");
        this.gson = gson;
        INST = this;
    }

    public BossRaidDefinition getBossWave(ResourceLocation id) {
        return this.bossraids.get(id);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> entries, IResourceManager manager, IProfiler profiler) {
        entries.forEach((name, baseJson) -> {
            if (!baseJson.isJsonObject()) {
                ModRef.LOGGER.error("BossWave definition has to be an object: {}", name);
                return;
            }
            try {
                BossRaidDefinition bossWave = this.gson.fromJson(baseJson, BossRaidDefinition.class);
                if (bossWave.isEmpty()) {
                    ModRef.LOGGER.error("Boss Wave Definition '{}' cannot have an empty list of waves.", name);
                    return;
                }
                this.bossraids.put(name, bossWave);
            } catch (JsonParseException e) {
                ModRef.LOGGER.error("Could not parse BossWave " + name, e);
            }
        });
        System.out.println(this.bossraids);
    }
}
