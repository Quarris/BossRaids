package dev.quarris.bosswaves.content;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class KeystoneBlock extends Block {

    public KeystoneBlock() {
        super(AbstractBlock.Properties.of(Material.STONE));
    }

    @Override
    public ActionResultType use(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult result) {
        TileEntity tile = level.getBlockEntity(pos);
        if (!(tile instanceof KeystoneTileEntity)) {
            return ActionResultType.PASS;
        }

        KeystoneTileEntity keystone = (KeystoneTileEntity) tile;
        if (keystone.activateWithItem(player, player.getItemInHand(hand)) != KeystoneTileEntity.KeystoneAction.INVALID) {
            return ActionResultType.sidedSuccess(level.isClientSide());
        }

        return ActionResultType.PASS;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new KeystoneTileEntity();
    }
}
