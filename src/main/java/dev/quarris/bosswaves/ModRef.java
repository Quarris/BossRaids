package dev.quarris.bosswaves;

import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModRef {
    public static final String ID = "bosswaves";
    public static final Logger LOGGER = LogManager.getLogger("BossWaves");

    public static ResourceLocation res(String name) {
        return new ResourceLocation(ID, name);
    }
}
