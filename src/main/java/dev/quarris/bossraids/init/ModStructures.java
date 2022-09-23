package dev.quarris.bossraids.init;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import dev.quarris.bossraids.ModRef;
import dev.quarris.bossraids.raid.arena.RaidArenaDefinition;
import dev.quarris.bossraids.raid.arena.RaidArenas;
import dev.quarris.bossraids.world.structures.RaidArenaPieces;
import dev.quarris.bossraids.world.structures.RaidArenaStructure;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.structure.IStructurePieceType;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;


// Structure generation helped with this video: https://www.youtube.com/watch?v=qKTkv5Rizyw
public class ModStructures {

    public static final DeferredRegister<Structure<?>> STRUCTURES =
        DeferredRegister.create(ForgeRegistries.STRUCTURE_FEATURES, ModRef.ID);

    //public static final RegistryObject<RaidArenaStructure> RAID_ARENA = STRUCTURES.register("arena", () -> new RaidArenaStructure(NoFeatureConfig.CODEC));

    public static StructureFeature<NoFeatureConfig, ? extends Structure<NoFeatureConfig>> RAID_ARENA_FEATURE;

    public static IStructurePieceType RAID_ARENA_PIECE_TYPE;

    public static void register(IEventBus bus) {
        STRUCTURES.register(bus);
    }

    public static void registerStructureFeatures() {
        //RAID_ARENA_FEATURE = feature(ModRef.res("arena"), RAID_ARENA.get().configured(NoFeatureConfig.INSTANCE));

        RAID_ARENA_PIECE_TYPE = Registry.register(Registry.STRUCTURE_PIECE, ModRef.res("arena"), RaidArenaPieces.Piece::new);

        /*WorldGenRegistries.NOISE_GENERATOR_SETTINGS.get(DimensionSettings.OVERWORLD).structureSettings().structureConfig().put(RAID_ARENA.get(), new StructureSeparationSettings(100, 50, 1982764981));
        Structure.STRUCTURES_REGISTRY.put(RAID_ARENA.getId().toString(), RAID_ARENA.get());*/
    }

    public static void loadArenas() {
        RaidArenas.load();

        for (Map.Entry<String, RaidArenaDefinition> entry : RaidArenas.getArenas().entrySet()) {
            RaidArenas.addStructure(entry.getKey(), STRUCTURES.register(entry.getKey(), () -> new RaidArenaStructure(entry.getValue(), NoFeatureConfig.CODEC)));
        }
    }

    /* average distance apart in chunks between spawn attempts */
    /* minimum distance apart in chunks between spawn attempts. MUST BE LESS THAN ABOVE VALUE*/
    /* this modifies the seed of the structure so no two structures always spawn over each-other.
    Make this large and unique. */
    public static void setupStructures() {
        //setupMapSpacingAndLand(RAID_ARENA.get(),
        //    new StructureSeparationSettings(100, 50, 1982764981),
        //    true);

        for (String key : RaidArenas.getKeys()) {
            RaidArenaDefinition def = RaidArenas.getDefinition(key);
            RegistryObject<RaidArenaStructure> structure = RaidArenas.getStructure(key);

            setupMapSpacingAndLand(structure.get(),
                new StructureSeparationSettings(def.spacing, def.separation, key.hashCode()),
                true);
        }
    }

    /**
     * Adds the provided structure to the registry, and adds the separation settings.
     * The rarity of the structure is determined based on the values passed into
     * this method in the structureSeparationSettings argument.
     * This method is called by setupStructures above.
     **/
    public static <F extends Structure<?>> void setupMapSpacingAndLand(F structure, StructureSeparationSettings structureSeparationSettings,
                                                                       boolean transformSurroundingLand) {
        //add our structures into the map in Structure class
        Structure.STRUCTURES_REGISTRY.put(structure.getRegistryName().toString(), structure);

        /*
         * Whether surrounding land will be modified automatically to conform to the bottom of the structure.
         * Basically, it adds land at the base of the structure like it does for Villages and Outposts.
         * Doesn't work well on structure that have pieces stacked vertically or change in heights.
         *
         */
        if (transformSurroundingLand) {
            Structure.NOISE_AFFECTING_FEATURES = ImmutableList.<Structure<?>>builder()
                .addAll(Structure.NOISE_AFFECTING_FEATURES)
                .add(structure)
                .build();
        }

        /*
         * This is the map that holds the default spacing of all structures.
         * Always add your structure to here so that other mods can utilize it if needed.
         *
         * However, while it does propagate the spacing to some correct dimensions from this map,
         * it seems it doesn't always work for code made dimensions as they read from this list beforehand.
         *
         * Instead, we will use the WorldEvent.Load event in ModWorldEvents to add the structure
         * spacing from this list into that dimension or to do dimension blacklisting properly.
         * We also use our entry in DimensionStructuresSettings.DEFAULTS in WorldEvent.Load as well.
         *
         * DEFAULTS requires AccessTransformer  (See resources/META-INF/accesstransformer.cfg)
         */
        DimensionStructuresSettings.DEFAULTS =
            ImmutableMap.<Structure<?>, StructureSeparationSettings>builder()
                .putAll(DimensionStructuresSettings.DEFAULTS)
                .put(structure, structureSeparationSettings)
                .build();

        /*
         * There are very few mods that relies on seeing your structure in the
         * noise settings registry before the world is made.
         *
         * You may see some mods add their spacings to DimensionSettings.BUILTIN_OVERWORLD instead of the
         * NOISE_GENERATOR_SETTINGS loop below but that field only applies for the default overworld and
         * won't add to other worldtypes or dimensions (like amplified or Nether).
         * So yeah, don't do DimensionSettings.BUILTIN_OVERWORLD. Use the NOISE_GENERATOR_SETTINGS loop
         * below instead if you must.
         */
        WorldGenRegistries.NOISE_GENERATOR_SETTINGS.entrySet().forEach(settings -> {
            // TODO add dimension type specific settings
            Map<Structure<?>, StructureSeparationSettings> structureMap =
                settings.getValue().structureSettings().structureConfig();
            /*
             * Pre-caution in case a mod makes the structure map immutable like datapacks do.
             * I take no chances myself. You never know what another mods does...
             *
             * structureConfig requires AccessTransformer  (See resources/META-INF/accesstransformer.cfg)
             */
            if (structureMap instanceof ImmutableMap) {
                Map<Structure<?>, StructureSeparationSettings> tempMap = new HashMap<>(structureMap);
                tempMap.put(structure, structureSeparationSettings);
                settings.getValue().structureSettings().structureConfig = tempMap;

            } else {
                structureMap.put(structure, structureSeparationSettings);
            }
        });
    }

    private static <FC extends IFeatureConfig, F extends Structure<FC>> StructureFeature<FC, F> feature(ResourceLocation id, StructureFeature<FC, F> feature) {
        return WorldGenRegistries.register(WorldGenRegistries.CONFIGURED_STRUCTURE_FEATURE, id, feature);
    }

}
