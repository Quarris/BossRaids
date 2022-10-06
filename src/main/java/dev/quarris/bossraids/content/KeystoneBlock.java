package dev.quarris.bossraids.content;

import dev.quarris.bossraids.raid.data.RaidState;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class KeystoneBlock extends Block {

    public static final EnumProperty<RaidState> RAID_STATE = EnumProperty.create("raid_state", RaidState.class);
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    private static final VoxelShape SHAPE = VoxelShapes.block(); //makeShape();

    public KeystoneBlock() {
        super(AbstractBlock.Properties.copy(Blocks.BEDROCK));

        this.registerDefaultState(this.getStateDefinition().any().setValue(ACTIVE, false).setValue(RAID_STATE, RaidState.INACTIVE));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE, RAID_STATE);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return state.getValue(ACTIVE);
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, World level, BlockPos pos) {
        return state.getValue(RAID_STATE).ordinal();
    }

    @Override
    public ActionResultType use(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult result) {
        if (level.isClientSide()) {
            return ActionResultType.sidedSuccess(level.isClientSide());
        }

        TileEntity tile = level.getBlockEntity(pos);
        if (!(tile instanceof KeystoneTileEntity)) {
            return ActionResultType.PASS;
        }

        KeystoneTileEntity keystone = (KeystoneTileEntity) tile;
        ItemStack held = player.getItemInHand(hand);

        if (keystone.activateWithItem(player, held) != KeystoneAction.INVALID) {

        }

        return ActionResultType.sidedSuccess(level.isClientSide());
    }

    @Override
    public VoxelShape getShape(BlockState p_220053_1_, IBlockReader p_220053_2_, BlockPos p_220053_3_, ISelectionContext p_220053_4_) {
        return SHAPE;
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

    private static VoxelShape makeShape() {
        VoxelShape shape = VoxelShapes.empty();
        shape = VoxelShapes.join(shape, VoxelShapes.box(0.0625, 0, 0.0625, 0.9375, 0.125, 0.9375), IBooleanFunction.OR);
        shape = VoxelShapes.join(shape, VoxelShapes.box(0.125, 0.125, 0.125, 0.875, 0.25, 0.875), IBooleanFunction.OR);
        shape = VoxelShapes.join(shape, VoxelShapes.box(0.1875, 0.25, 0.1875, 0.8125, 0.375, 0.8125), IBooleanFunction.OR);
        shape = VoxelShapes.join(shape, VoxelShapes.box(0.25, 0.375, 0.25, 0.75, 0.5, 0.75), IBooleanFunction.OR);

        return shape;
    }

    public enum KeystoneAction {
        INSERT, RENAME, DISPLAY_REQUIREMENTS, CLIENT, ACTIVATE, IN_PROGRESS, INVALID
    }
}
