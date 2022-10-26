package dev.quarris.bossraids;

import dev.quarris.bossraids.client.render.KeystoneTileRenderer;
import dev.quarris.bossraids.init.ModContent;
import dev.quarris.bossraids.network.PacketHandler;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ModRef.ID)
public class ModRoot {

    public ModRoot() {
        //ModStructures.loadArenas();
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModContent.register(modBus);
        //ModStructures.register(modBus);
        modBus.addListener(this::clientSetup);
        modBus.addListener(this::commonSetup);
        //modBus.addGenericListener(Structure.class, EventPriority.LOW, (RegistryEvent.Register<Structure<?>> event) -> ModStructures.registerStructureFeatures());

    }

    public void clientSetup(FMLClientSetupEvent event) {
        ClientRegistry.bindTileEntityRenderer(ModContent.KEYSTONE_TILE.get(), KeystoneTileRenderer::new);
    }

    public void commonSetup(FMLCommonSetupEvent event) {
        //event.enqueueWork(ModStructures::setupStructures);
        PacketHandler.init();
    }
}
