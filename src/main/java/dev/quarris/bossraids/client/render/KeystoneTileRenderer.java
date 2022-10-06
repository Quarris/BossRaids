package dev.quarris.bossraids.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.quarris.bossraids.content.KeystoneTileEntity;
import dev.quarris.bossraids.raid.BossRaidManager;
import dev.quarris.bossraids.raid.data.BossRaid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;

public class KeystoneTileRenderer extends TileEntityRenderer<KeystoneTileEntity> {

    public KeystoneTileRenderer(TileEntityRendererDispatcher manager) {
        super(manager);
    }

    @Override
    public void render(KeystoneTileEntity tile, float delta, MatrixStack matrix, IRenderTypeBuffer buffer, int light, int overlay) {
        PlayerEntity player = Minecraft.getInstance().player;
        RayTraceResult rayTraceResult = Minecraft.getInstance().hitResult;
        if (rayTraceResult == null || rayTraceResult.getType() != RayTraceResult.Type.BLOCK) {
            return;
        }

        BlockPos lookPos = ((BlockRayTraceResult) rayTraceResult).getBlockPos();
        if (!tile.getBlockPos().equals(lookPos)) {
            return;
        }
    }
}
