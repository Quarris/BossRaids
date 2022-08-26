package dev.quarris.bossraids.content;

import dev.quarris.bossraids.init.ModContent;
import dev.quarris.bossraids.raid.BossRaid;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

public class KeystoneTileEntity extends TileEntity implements ITickableTileEntity {


    private BossRaid raid;

    public KeystoneTileEntity() {
        super(ModContent.KEYSTONE_TILE.get());
    }

    public KeystoneAction activateWithItem(PlayerEntity player, ItemStack item) {
        if (player.isCreative() && item.getItem() == Items.NAME_TAG) {
            if (item.hasCustomHoverName()) {
                try {
                    ResourceLocation id = new ResourceLocation(item.getHoverName().getString());
                    this.setRaid(id);
                    return KeystoneAction.RENAME;
                } catch (ResourceLocationException e) {
                    // Name is not a valid resource name, do not try to change the boss raid id
                }
            }
        }

        if (this.raid == null) {
            return KeystoneAction.INVALID;
        }

        if (item.isEmpty()) {
            if (!player.level.isClientSide()) {
                player.displayClientMessage(new StringTextComponent(String.valueOf(this.raid.getId())), false);
                player.displayClientMessage(new StringTextComponent(String.valueOf(this.raid.getRequirements())), false);
                player.displayClientMessage(new StringTextComponent(this.raid.getState().toString()), false);
            }
            return KeystoneAction.DISPLAY_REQUIREMENTS;
        }

        if (this.raid.getState().inProgress()) {
            return KeystoneAction.IN_PROGRESS;
        }



        if (this.raid.getState().inactive()) {
            return KeystoneAction.INVALID;
        }

        if (this.raid.getState().awaiting()) {
            // Add required item
            if (this.raid.tryInsertRequirement(player, item)) {
                return KeystoneAction.INSERT;
            }
        }

        return KeystoneAction.INVALID;
    }

    @Override
    public void tick() {
        if (this.raid != null) {
            this.raid.update();
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (this.raid != null) {
            this.raid.onRemoved();
        }
    }

    @Override
    public void setLevelAndPosition(World level, BlockPos pos) {
        super.setLevelAndPosition(level, pos);
        if (this.raid != null) {
            this.raid.setLevelAndPos(level, pos);
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        nbt = super.save(nbt);
        if (this.raid != null) {
            nbt.put("Raid", this.raid.serialize());
        }
        return nbt;
    }

    @Override
    public void load(BlockState state, CompoundNBT nbt) {
        super.load(state, nbt);
        if (nbt.contains("Raid")) {
            this.raid = new BossRaid(this, nbt.getCompound("Raid"));
        }
    }

    public void setRaid(ResourceLocation id) {
        if (this.raid != null) {
            this.raid.setId(id);
        } else {
            this.raid = new BossRaid(this, id);
        }
    }

    public enum KeystoneAction {
        INSERT, RENAME, DISPLAY_REQUIREMENTS, IN_PROGRESS, INVALID
    }
}
