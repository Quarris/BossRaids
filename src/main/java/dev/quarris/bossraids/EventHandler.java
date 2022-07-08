package dev.quarris.bossraids;

import com.google.gson.Gson;
import dev.quarris.bossraids.util.BossRaidUtils;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ModRef.ID)
public class EventHandler {

    @SubscribeEvent
    public static void registerBossWaveReloadListener(AddReloadListenerEvent event) {
        Gson gson = BossRaidUtils.getBossRaidGson();
        event.addListener(new BossRaidManager(gson));
    }
}
