package dev.quarris.bosswaves.content;

import dev.quarris.bosswaves.BossWaveManager;
import dev.quarris.bosswaves.waves.BossWaveDefinition;
import dev.quarris.bosswaves.waves.WaveDefinition;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public class KeystoneTileEntity extends TileEntity {

    private ResourceLocation bossWaveId;
    private BossWaveDefinition bossWaveDefinition;

    private int wave;
    private WaveDefinition currentWave;

    private boolean isActive;

    public KeystoneTileEntity() {
        super(ModContent.KEYSTONE_TILE.get());
    }

    public KeystoneAction activateWithItem(PlayerEntity player, ItemStack item) {
        if (player.isShiftKeyDown() && player.isCreative() && item.getItem() == Items.NAME_TAG) {
            if (item.hasCustomHoverName()) {
                this.setBossWaveId(new ResourceLocation(item.getHoverName().toString()));
            }
        }

        if (this.bossWaveDefinition == null) {
            return KeystoneAction.INVALID;
        }

        return KeystoneAction.INVALID;
    }

    public void setBossWaveId(ResourceLocation id) {
        this.bossWaveId = id;
        this.bossWaveDefinition = BossWaveManager.INST.getBossWave(id);
    }

    public enum KeystoneAction {
        ACTIVATE, RENAME, INVALID
    }
}
