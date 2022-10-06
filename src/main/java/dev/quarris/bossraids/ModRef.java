package dev.quarris.bossraids;

import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

public class ModRef {
    public static final String ID = "bossraids";
    public static final Logger LOGGER = LogManager.getLogger("BossRaids");
    public static final Random RANDOM = new Random();

    public static ResourceLocation res(String name) {
        return new ResourceLocation(ID, name);
    }
}
