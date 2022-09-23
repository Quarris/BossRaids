package dev.quarris.bossraids.raid.arena;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class RaidArenaDefinition {

    public final ResourceLocation structureId;
    public final int spacing;
    public final int separation;
    public final ArenaKeystone keystone;
    public final StructureFilters filters;

    public RaidArenaDefinition(ResourceLocation structureId, int spacing, int separation, ArenaKeystone keystone, StructureFilters filters) {
        this.structureId = structureId;
        this.spacing = spacing;
        this.separation = separation;
        this.keystone = keystone;
        this.filters = filters;
    }

    public ResourceLocation getRaidId() {
        if (this.keystone == null) {
            return null;
        }

        return this.keystone.raidId;
    }

    @Override
    public String toString() {
        return "RaidArenaDefinition{" +
            "structureId=" + structureId +
            ", spacing=" + spacing +
            ", separation=" + separation +
            ", keystone=" + keystone +
            ", filters=" + filters +
            '}';
    }

    public static class ArenaKeystone {
        private final ResourceLocation raidId;
        private final BlockPos position;

        public ArenaKeystone(ResourceLocation raidId, BlockPos position) {
            this.raidId = raidId;
            this.position = position;
        }

        @Override
        public String toString() {
            return "ArenaKeystone{" +
                "raidId=" + raidId +
                ", position=" + position +
                '}';
        }
    }

    public static class StructureFilters {
        private final List<ResourceLocation> dimensionList;
        private final boolean dimensionBlacklist;
        private final List<ResourceLocation> biomeList;
        private final boolean biomeBlacklist;

        public StructureFilters(List<ResourceLocation> dimensionList, boolean dimensionBlacklist, List<ResourceLocation> biomeList, boolean biomeBlacklist) {
            this.dimensionList = dimensionList;
            this.dimensionBlacklist = dimensionBlacklist;
            this.biomeList = biomeList;
            this.biomeBlacklist = biomeBlacklist;
        }

        @Override
        public String toString() {
            return "StructureFilters{" +
                "dimensionList=" + dimensionList +
                ", dimensionBlacklist=" + dimensionBlacklist +
                ", biomeList=" + biomeList +
                ", biomeBlacklist=" + biomeBlacklist +
                '}';
        }
    }
}
