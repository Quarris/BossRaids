package dev.quarris.bossraids.world.events;

import com.mojang.serialization.Codec;
import dev.quarris.bossraids.ModRef;
import dev.quarris.bossraids.raid.arena.RaidArenas;
import dev.quarris.bossraids.world.structures.RaidArenaStructure;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.FlatChunkGenerator;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.apache.logging.log4j.LogManager;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = ModRef.ID)
public class WorldEvents {

    //@SubscribeEvent
    public static void onBiomeLoad(BiomeLoadingEvent event) {
        RaidArenaStructure.generateStructure(event);
    }

    //@SubscribeEvent
    public static void addDimensionalSpacing(WorldEvent.Load event) {
        if (event.getWorld() instanceof ServerWorld) {
            ServerWorld world = (ServerWorld) event.getWorld();

            try {
                Method GETCODEC_METHOD =
                    ObfuscationReflectionHelper.findMethod(ChunkGenerator.class, "func_230347_a_");
                ResourceLocation cgRL = Registry.CHUNK_GENERATOR.getKey(
                    (Codec<? extends ChunkGenerator>) GETCODEC_METHOD.invoke(world.getChunkSource().generator));

                if (cgRL != null && cgRL.getNamespace().equals("terraforged")) {
                    return;
                }
            } catch (Exception e) {
                LogManager.getLogger().error("Was unable to check if " + world.dimension().location()
                    + " is using Terraforged's ChunkGenerator.");
            }
            
            // Prevent spawning our structure in Vanilla's superflat world
            if (world.getChunkSource().generator instanceof FlatChunkGenerator &&
                world.dimension().equals(World.OVERWORLD)) {
                return;
            }

            // Adding our Structure to the Map
            Map<Structure<?>, StructureSeparationSettings> tempMap =
                new HashMap<>(world.getChunkSource().generator.getSettings().structureConfig());

            RaidArenas.getArenaStructures().values().forEach(structure ->
                tempMap.putIfAbsent(structure.get(), DimensionStructuresSettings.DEFAULTS.get(structure.get()))
            );

            world.getChunkSource().generator.getSettings().structureConfig = tempMap;
        }
    }
}
