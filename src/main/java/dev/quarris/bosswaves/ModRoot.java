package dev.quarris.bosswaves;

import dev.quarris.bosswaves.content.ModContent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ModRef.ID)
public class ModRoot {

    public ModRoot() {
        ModContent.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
