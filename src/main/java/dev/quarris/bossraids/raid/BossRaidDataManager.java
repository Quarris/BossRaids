package dev.quarris.bossraids.raid;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import dev.quarris.bossraids.ModRef;
import dev.quarris.bossraids.raid.definitions.BossRaidDefinition;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class BossRaidDataManager extends JsonReloadListener {

    private BiMap<ResourceLocation, BossRaidDefinition> bossRaidData = HashBiMap.create();
    public static BossRaidDataManager INST;
    private Gson gson;

    public BossRaidDataManager(Gson gson) {
        super(gson, "boss_raids");
        this.gson = gson;
        INST = this;
    }

    public BossRaidDefinition getRaidDefinition(ResourceLocation id) {
        return this.bossRaidData.get(id);
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
                this.bossRaidData.put(name, bossWave);
            } catch (JsonParseException e) {
                ModRef.LOGGER.error("Could not parse BossWave " + name, e);
            }
        });
        System.out.println(this.bossRaidData);
    }

    public ResourceLocation getId(BossRaidDefinition definition) {
        return this.bossRaidData.inverse().get(definition);
    }
}
