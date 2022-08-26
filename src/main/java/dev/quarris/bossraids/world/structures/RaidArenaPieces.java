package dev.quarris.bossraids.world.structures;

import dev.quarris.bossraids.ModRef;
import dev.quarris.bossraids.content.KeystoneTileEntity;
import dev.quarris.bossraids.init.ModContent;
import dev.quarris.bossraids.init.ModStructures;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.gen.feature.structure.*;
import net.minecraft.world.gen.feature.template.BlockIgnoreStructureProcessor;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraftforge.common.util.Constants;

import java.util.List;
import java.util.Random;

public class RaidArenaPieces {

    public static final ResourceLocation STRUCTURE_LOCATION = ModRef.res("arena");

    public static final ResourceLocation RAID_ID = ModRef.res("raid");

    public static void addPieces(TemplateManager templates, BlockPos pos, Rotation rotation, List<StructurePiece> pieces, Random rand) {
        pieces.add(new Piece(templates, STRUCTURE_LOCATION, pos, rotation));
    }

    public static class Piece extends TemplateStructurePiece {
        private final ResourceLocation templateLocation;
        private final Rotation rotation;

        public Piece(TemplateManager templates, ResourceLocation id, BlockPos pos, Rotation rotation) {
            super(ModStructures.RAID_ARENA_PIECE_TYPE, 0);
            this.templateLocation = id;
            this.templatePosition = pos;
            this.rotation = rotation;
            this.loadTemplate(templates);
        }

        public Piece(TemplateManager templates, CompoundNBT nbt) {
            super(ModStructures.RAID_ARENA_PIECE_TYPE, nbt);
            this.templateLocation = new ResourceLocation(nbt.getString("Template"));
            this.rotation = Rotation.valueOf(nbt.getString("Rot"));
            this.loadTemplate(templates);
        }

        private void loadTemplate(TemplateManager templates) {
            Template template = templates.getOrCreate(this.templateLocation);
            PlacementSettings placementsettings = (new PlacementSettings()).setRotation(this.rotation).setMirror(Mirror.NONE).setRotationPivot(BlockPos.ZERO).addProcessor(BlockIgnoreStructureProcessor.AIR);
            this.setup(template, this.templatePosition, placementsettings);
        }

        protected void addAdditionalSaveData(CompoundNBT nbt) {
            super.addAdditionalSaveData(nbt);
            nbt.putString("Template", this.templateLocation.toString());
            nbt.putString("Rot", this.rotation.name());
        }

        protected void handleDataMarker(String data, BlockPos pos, IServerWorld level, Random rand, MutableBoundingBox aabb) {
            if ("keystone".equals(data)) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Constants.BlockFlags.DEFAULT);
                TileEntity tile = level.getBlockEntity(pos.below());
                if (tile instanceof KeystoneTileEntity) {
                    ((KeystoneTileEntity) tile).setRaid(RAID_ID);
                }
            }
        }

        /*public boolean postProcess(ISeedReader p_230383_1_, StructureManager p_230383_2_, ChunkGenerator p_230383_3_, Random p_230383_4_, MutableBoundingBox p_230383_5_, ChunkPos p_230383_6_, BlockPos p_230383_7_) {
            PlacementSettings placementsettings = (new PlacementSettings()).setRotation(this.rotation).setMirror(Mirror.NONE).setRotationPivot(IglooPieces.PIVOTS.get(this.templateLocation)).addProcessor(BlockIgnoreStructureProcessor.STRUCTURE_BLOCK);
            BlockPos blockpos = IglooPieces.OFFSETS.get(this.templateLocation);
            BlockPos blockpos1 = this.templatePosition.offset(Template.calculateRelativePosition(placementsettings, new BlockPos(3 - blockpos.getX(), 0, 0 - blockpos.getZ())));
            int i = p_230383_1_.getHeight(Heightmap.Type.WORLD_SURFACE_WG, blockpos1.getX(), blockpos1.getZ());
            BlockPos blockpos2 = this.templatePosition;
            this.templatePosition = this.templatePosition.offset(0, i - 90 - 1, 0);
            boolean flag = super.postProcess(p_230383_1_, p_230383_2_, p_230383_3_, p_230383_4_, p_230383_5_, p_230383_6_, p_230383_7_);
            if (this.templateLocation.equals(IglooPieces.STRUCTURE_LOCATION_IGLOO)) {
                BlockPos blockpos3 = this.templatePosition.offset(Template.calculateRelativePosition(placementsettings, new BlockPos(3, 0, 5)));
                BlockState blockstate = p_230383_1_.getBlockState(blockpos3.below());
                if (!blockstate.isAir() && !blockstate.is(Blocks.LADDER)) {
                    p_230383_1_.setBlock(blockpos3, Blocks.SNOW_BLOCK.defaultBlockState(), 3);
                }
            }

            this.templatePosition = blockpos2;
            return flag;
        }
        */
    }

}
