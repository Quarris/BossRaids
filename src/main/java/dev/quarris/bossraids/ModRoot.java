package dev.quarris.bossraids;

import dev.quarris.bossraids.client.render.KeystoneTileRenderer;
import dev.quarris.bossraids.content.ModContent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ModRef.ID)
public class ModRoot {

    public ModRoot() {
        ModContent.register(FMLJavaModLoadingContext.get().getModEventBus());
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
    }

    public void clientSetup(FMLClientSetupEvent event) {
        ClientRegistry.bindTileEntityRenderer(ModContent.KEYSTONE_TILE.get(), KeystoneTileRenderer::new);
    }
}
