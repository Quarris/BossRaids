package dev.quarris.bossraids;

import com.google.gson.Gson;
import dev.quarris.bossraids.raid.BossRaidDataManager;
import dev.quarris.bossraids.raid.BossRaidManager;
import dev.quarris.bossraids.util.BossRaidUtils;
import dev.quarris.bossraids.world.events.WorldEvents;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ModRef.ID)
public class EventHandler {

    @SubscribeEvent
    public static void registerBossWaveReloadListener(AddReloadListenerEvent event) {
        Gson gson = BossRaidUtils.getBossRaidGson();
        event.addListener(new BossRaidDataManager(gson));
    }

    @SubscribeEvent
    public static void saveBossRaidData(WorldEvent.Save event) {
        if (event.getWorld() instanceof ServerWorld) {
            BossRaidManager.getBossRaids((ServerWorld) event.getWorld()).setDirty();
        }
    }
}
