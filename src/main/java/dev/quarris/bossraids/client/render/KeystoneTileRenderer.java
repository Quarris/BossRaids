package dev.quarris.bossraids.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.quarris.bossraids.content.KeystoneTileEntity;
import dev.quarris.bossraids.raid.BossRaidManager;
import dev.quarris.bossraids.raid.data.BossRaid;
import dev.quarris.bossraids.util.ItemRequirement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;

public class KeystoneTileRenderer extends TileEntityRenderer<KeystoneTileEntity> {

    public KeystoneTileRenderer(TileEntityRendererDispatcher manager) {
        super(manager);
    }

    @Override
    public void render(KeystoneTileEntity tile, float delta, MatrixStack matrix, IRenderTypeBuffer buffer, int light, int overlay) {
        Minecraft mc = Minecraft.getInstance();
        PlayerEntity player = mc.player;
        RayTraceResult rayTraceResult = mc.hitResult;
        if (rayTraceResult == null || rayTraceResult.getType() != RayTraceResult.Type.BLOCK) {
            return;
        }

        BlockPos lookPos = ((BlockRayTraceResult) rayTraceResult).getBlockPos();
        if (!tile.getBlockPos().equals(lookPos)) {
            return;
        }

        Direction face = ((BlockRayTraceResult) rayTraceResult).getDirection();

        if (tile.displayItems != null) {
            int l = WorldRenderer.getLightColor(tile.getLevel(), tile.getBlockPos().relative(face));
            int size = tile.displayItems.size();
            matrix.pushPose();
            matrix.translate(0.5, 0.5, 0.5);
            matrix.translate(face.getStepX(), face.getStepY(), face.getStepZ());
            matrix.scale(0.2f, 0.2f, 0.2f);
            matrix.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
            int square = MathHelper.ceil(Math.sqrt(size));
            for (int i = 0; i < size; i++) {
                matrix.pushPose();
                int x = i % square;
                int y = i / square;
                matrix.translate(x, y, 0);
                ItemRequirement.Instance item = tile.displayItems.get(i);
                mc.getItemRenderer().renderStatic(item.getLoopingItem(tile.getLevel().getGameTime()), ItemCameraTransforms.TransformType.FIXED, l, OverlayTexture.NO_OVERLAY, matrix, buffer);
                float countScale = 0.04f;
                matrix.scale(-countScale, -countScale, countScale);
                String countString = String.valueOf(item.getCount());
                int width = mc.font.width(countString);
                matrix.translate(0, -2, -7);
                mc.font.draw(matrix, countString,  -width / 2f, 6, 0xffffff);
                matrix.popPose();
            }
            matrix.popPose();
        }
    }
}
