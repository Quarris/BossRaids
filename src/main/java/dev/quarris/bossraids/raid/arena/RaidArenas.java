package dev.quarris.bossraids.raid.arena;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.quarris.bossraids.ModRef;
import dev.quarris.bossraids.util.JsonUtils;
import dev.quarris.bossraids.world.structures.RaidArenaStructure;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class RaidArenas {

    private static final Map<String, RaidArenaDefinition> ARENAS = new HashMap<>();
    private static final Map<String, RegistryObject<RaidArenaStructure>> ARENA_STRUCTURES = new HashMap<>();

    public static ImmutableMap<String, RaidArenaDefinition> getArenas() {
        return ImmutableMap.copyOf(ARENAS);
    }

    public static ImmutableMap<String, RegistryObject<RaidArenaStructure>> getArenaStructures() {
        return ImmutableMap.copyOf(ARENA_STRUCTURES);
    }

    public static void addStructure(String key, RegistryObject<RaidArenaStructure> structure) {
        ARENA_STRUCTURES.put(key, structure);
    }

    public static void load() {
        File raidArenasJsonFile = FMLPaths.CONFIGDIR.get().resolve("raid_arenas.json").toFile();

        try {
            if (raidArenasJsonFile.createNewFile()) {
                generateDefault(raidArenasJsonFile);
                return;
            }
        } catch (IOException e) {
            ModRef.LOGGER.error("Could not create raid_arenas.json file", e);
            return;
        }

        JsonElement json = JsonUtils.parseJsonFile(raidArenasJsonFile);

        if (!json.isJsonObject()) {
            ModRef.LOGGER.error("Raid Arenas json must start with a JsonObject root");
            return;
        }
        JsonObject rootJson = json.getAsJsonObject();
        if (!rootJson.has("arenas")) {
            ModRef.LOGGER.info("No arenas found");
        }

        JsonObject arenasJson = rootJson.getAsJsonObject("arenas");

        arenasJson.entrySet().forEach(entry -> {
            String name = entry.getKey();
            try {
                JsonObject arenaJson = entry.getValue().getAsJsonObject();

                ResourceLocation structureId = new ResourceLocation(arenaJson.get("structureId").getAsString());
                int spacing = arenaJson.get("spacing").getAsInt();
                int separation = arenaJson.get("separation").getAsInt();
                RaidArenaDefinition.ArenaKeystone keystone = null;
                if (arenaJson.has("keystone")) {
                    JsonObject keystoneJson = arenaJson.getAsJsonObject("keystone");
                    ResourceLocation keystoneRaidId = new ResourceLocation(keystoneJson.get("raidId").getAsString());
                    JsonArray keystonePosJson = keystoneJson.getAsJsonArray("position");
                    BlockPos keystonePosition = new BlockPos(keystonePosJson.get(0).getAsInt(),
                        keystonePosJson.get(1).getAsInt(),
                        keystonePosJson.get(2).getAsInt());

                    keystone = new RaidArenaDefinition.ArenaKeystone(keystoneRaidId, keystonePosition);
                }

                RaidArenaDefinition.StructureFilters filters = null;
                if (arenaJson.has("filters")) {
                    JsonObject filtersJson = arenaJson.getAsJsonObject("filters");
                    JsonObject dimJson = filtersJson.getAsJsonObject("dimension");
                    JsonArray dimListJson = dimJson.getAsJsonArray("list");
                    JsonObject biomeJson = filtersJson.getAsJsonObject("biome");
                    JsonArray biomeListJson = biomeJson.getAsJsonArray("list");

                    boolean blacklistDims = dimJson.get("blacklist").getAsBoolean();
                    List<ResourceLocation> dimList = new ArrayList<>();
                    dimListJson.forEach(item -> dimList.add(new ResourceLocation(item.getAsString())));

                    boolean blacklistBiomes = biomeJson.get("blacklist").getAsBoolean();
                    List<ResourceLocation> biomeList = new ArrayList<>();
                    biomeListJson.forEach(item -> biomeList.add(new ResourceLocation(item.getAsString())));

                    filters = new RaidArenaDefinition.StructureFilters(dimList, blacklistDims, biomeList, blacklistBiomes);
                }

                ARENAS.put(name, new RaidArenaDefinition(structureId, spacing, separation, keystone, filters));
            } catch (Exception e) {
                ModRef.LOGGER.warn("Could not load arena " + name + "; skipping.", e);
            }
        });

        ModRef.LOGGER.info("Loaded {} arenas", ARENAS.size());
    }

    private static void generateDefault(File file) {
        JsonObject root = new JsonObject();
        root.add("arenas", new JsonObject());
        if (!JsonUtils.writeJsonToFile(file, root)) {
            ModRef.LOGGER.error("Couldn't generate default raid arenas json file.");
        }
    }

    public static Set<String> getKeys() {
        return Collections.unmodifiableSet(ARENAS.keySet());
    }

    public static RegistryObject<RaidArenaStructure> getStructure(String key) {
        return ARENA_STRUCTURES.get(key);
    }

    public static RaidArenaDefinition getDefinition(String key) {
        return ARENAS.get(key);
    }
}
