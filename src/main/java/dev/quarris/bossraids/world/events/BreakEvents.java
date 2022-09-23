package dev.quarris.bossraids.world.events;

import dev.quarris.bossraids.ModRef;
import dev.quarris.bossraids.init.ModContent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.gen.feature.structure.TemplateStructurePiece;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.*;

// TODO Configs to enable/disable indestructible structures
@Mod.EventBusSubscriber(modid = ModRef.ID)
public class BreakEvents {

    private static final Map<BlockPos, BlockPos> CACHED_KEYSTONE_STRUCTURE_POSES = new HashMap<>();
    private static final Map<UUID, List<BlockPos>> PROTECTED_POSES_BY_PLAYER = new HashMap<>();
    private static final List<BlockPos> PROTECTED_POSES = new ArrayList<>();

    //@SubscribeEvent
    public static void cancelBlockBreaking(BlockEvent.BreakEvent event) {
        if (!event.getWorld().isClientSide()) {
            ServerWorld serverWorld = (ServerWorld) event.getWorld();
            if (isPosProtected(serverWorld, event.getPlayer(), event.getPos())) {
                event.setCanceled(true);
            }
        }
    }

    //@SubscribeEvent
    public static void cancelBlockPlacing(BlockEvent.EntityPlaceEvent event) {
        if (!event.getWorld().isClientSide()) {
            ServerWorld serverWorld = (ServerWorld) event.getWorld();
            PlayerEntity player = event.getEntity() instanceof PlayerEntity ? (PlayerEntity) event.getEntity() : null;
            if (isPosProtected(serverWorld, player, event.getPos())) {
                event.setCanceled(true);
            }
        }
    }

    //@SubscribeEvent
    public static void cancelPlayerInteraction(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getWorld().isClientSide()) {
            ServerWorld serverWorld = (ServerWorld) event.getWorld();
            if (isPosProtected(serverWorld, event.getPlayer(), event.getPos())) {
                event.setCanceled(true);
            }
        }
    }

   // @SubscribeEvent
    public static void cancelExplosionBreaking(ExplosionEvent.Detonate event) {
        if (!event.getWorld().isClientSide()) {
            ServerWorld serverWorld = (ServerWorld) event.getWorld();
            PlayerEntity player = event.getExplosion().getExploder() instanceof PlayerEntity ? (PlayerEntity) event.getExplosion().getExploder() : null;
            event.getAffectedBlocks().removeIf(pos -> isPosProtected(serverWorld, player, pos));
        }
    }

    private static boolean isPosProtected(ServerWorld world, @Nullable PlayerEntity player, BlockPos pos) {
        if (player != null && player.isCreative()) {
            return false;
        }

        List<Structure<?>> testStructures = new ArrayList<>(ForgeRegistries.STRUCTURE_FEATURES.getValues());
        BlockPos structurePos = null;
        BlockPos tilePos = null;
        structureCheck:
        for (Structure<?> structure : testStructures) {
            StructureStart<?> structureStart = world.structureFeatureManager().getStructureAt(pos, true, structure);
            if (!structureStart.isValid()) {
                continue;
            }

            MutableBoundingBox box = structureStart.getBoundingBox();
            BlockPos checkStructurePos = new BlockPos(box.x0, box.y0, box.z0);


            if (CACHED_KEYSTONE_STRUCTURE_POSES.containsKey(checkStructurePos)) {
                structurePos = checkStructurePos;
                tilePos = CACHED_KEYSTONE_STRUCTURE_POSES.get(checkStructurePos);
                break;
            }

            for (StructurePiece piece : structureStart.getPieces()) {
                // If it's a template piece, we can find the block via template rather than iterating over each position
                if (piece instanceof TemplateStructurePiece) {
                    TemplateStructurePiece templatePiece = (TemplateStructurePiece) piece;
                    //List<Template.BlockInfo> infos = world.getStructureManager().get(structure.getRegistryName()).filterBlocks(templatePiece.templatePosition, templatePiece.placeSettings, ModContent.KEYSTONE_BLOCK.get());
                    List<Template.BlockInfo> infos = templatePiece.template.filterBlocks(templatePiece.templatePosition, templatePiece.placeSettings, ModContent.KEYSTONE_BLOCK.get());

                    if (!infos.isEmpty()) {
                        BlockPos checkPos = infos.get(0).pos;
                        if (world.getBlockState(checkPos).getBlock() == ModContent.KEYSTONE_BLOCK.get()) {
                            tilePos = checkPos;
                            structurePos = checkStructurePos;
                            break structureCheck;
                        }
                    }
                }

                MutableBoundingBox pieceBox = piece.getBoundingBox();
                for (int y = pieceBox.y0; y <= pieceBox.y1; y++) {
                    for (int z = pieceBox.z0; z <= pieceBox.z1; z++) {
                        for (int x = pieceBox.x0; x <= pieceBox.x1; x++) {
                            BlockPos checkPos = new BlockPos(x, y, z);
                            if (world.getBlockState(checkPos).getBlock() == ModContent.KEYSTONE_BLOCK.get()) {
                                tilePos = checkPos;
                                structurePos = checkStructurePos;
                                break structureCheck;
                            }
                        }
                    }
                }
            }
        }

        if (structurePos == null) {
            // No structure found at the position
            return false;
        }

        CACHED_KEYSTONE_STRUCTURE_POSES.putIfAbsent(structurePos, tilePos);
        return true;
    }
}
