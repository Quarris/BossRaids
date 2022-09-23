package dev.quarris.bossraids.world.structures;

import com.mojang.serialization.Codec;
import dev.quarris.bossraids.raid.arena.RaidArenaDefinition;
import dev.quarris.bossraids.raid.arena.RaidArenas;
import net.minecraft.block.BlockState;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.Rotation;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.event.world.BiomeLoadingEvent;

import java.util.Set;

public class RaidArenaStructure extends Structure<NoFeatureConfig> {

    private final RaidArenaDefinition arenaDefinition;

    public RaidArenaStructure(RaidArenaDefinition arenaDefinition, Codec<NoFeatureConfig> codec) {
        super(codec);
        this.arenaDefinition = arenaDefinition;
    }

    public Structure.IStartFactory<NoFeatureConfig> getStartFactory() {
        return Start::new;
    }

    @Override
    public GenerationStage.Decoration step() {
        return GenerationStage.Decoration.SURFACE_STRUCTURES;
    }

    @Override
    protected boolean isFeatureChunk(ChunkGenerator chunkGenerator, BiomeProvider biomeProvider, long seed, SharedSeedRandom chunkRandom, int chunkX, int chunkZ, Biome biome, ChunkPos chunkPos, NoFeatureConfig config) {
        BlockPos centerOfChunk = new BlockPos((chunkX << 4) + 7, 0, (chunkZ << 4) + 7);
        int landHeight = chunkGenerator.getBaseHeight(centerOfChunk.getX(), centerOfChunk.getZ(), Heightmap.Type.WORLD_SURFACE_WG);

        IBlockReader columnOfBlocks = chunkGenerator.getBaseColumn(centerOfChunk.getX(), centerOfChunk.getZ());
        BlockState topBlock = columnOfBlocks.getBlockState(centerOfChunk.above(landHeight));

        return topBlock.getFluidState().isEmpty();
    }

    public static void generateStructure(BiomeLoadingEvent event) {
        RegistryKey<Biome> key = RegistryKey.create(Registry.BIOME_REGISTRY, event.getName());
        Set<BiomeDictionary.Type> types = BiomeDictionary.getTypes(key);

        if (types.contains(BiomeDictionary.Type.PLAINS)) {
            RaidArenas.getArenaStructures().values().forEach(structure -> event.getGeneration().addStructureStart(structure.get().configured(IFeatureConfig.NONE)));
        }
    }

    public class Start extends StructureStart<NoFeatureConfig> {

        public Start(Structure<NoFeatureConfig> structure, int chunkX, int chunkZ, MutableBoundingBox aabb, int references, long seed) {
            super(structure, chunkX, chunkZ, aabb, references, seed);
        }

        public void generatePieces(DynamicRegistries registries, ChunkGenerator chunkGenerator, TemplateManager templates, int chunkX, int chunkZ, Biome biome, NoFeatureConfig config) {
            int centerX = (chunkX << 4) + 7;
            int centerY = (chunkZ << 4) + 7;
            BlockPos blockpos = new BlockPos(centerX, chunkGenerator.getBaseHeight(centerX, centerY, Heightmap.Type.WORLD_SURFACE_WG), centerY);
            Rotation rotation = Rotation.getRandom(this.random);
            this.pieces.add(new RaidArenaPieces.Piece(templates, RaidArenaStructure.this.arenaDefinition.structureId, RaidArenaStructure.this.arenaDefinition.getRaidId(), blockpos, rotation));
            this.calculateBoundingBox();
        }

    }
}
