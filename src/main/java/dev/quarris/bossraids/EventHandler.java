package dev.quarris.bossraids;

import com.google.gson.Gson;
import dev.quarris.bossraids.raid.BossRaidDataManager;
import dev.quarris.bossraids.raid.BossRaidManager;
import dev.quarris.bossraids.raid.definitions.EntityDefinition;
import dev.quarris.bossraids.util.BossRaidUtils;
import dev.quarris.bossraids.world.events.WorldEvents;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = ModRef.ID)
public class EventHandler {

    @SubscribeEvent
    public static void registerBossWaveReloadListener(AddReloadListenerEvent event) {
        Gson gson = BossRaidUtils.getBossRaidGson();
        event.addListener(new BossRaidDataManager(gson));
    }

    @SubscribeEvent
    public static void cancelDamages(LivingAttackEvent event) {
        CompoundNBT tag = event.getEntityLiving().getPersistentData();
        if (!tag.contains(EntityDefinition.DAMAGE_IMMUNITIES_TAG)) {
            return;
        }

        boolean shouldCancel = tag.getList(EntityDefinition.DAMAGE_IMMUNITIES_TAG, Constants.NBT.TAG_STRING).stream()
            .map(INBT::getAsString)
            .anyMatch(event.getSource().getMsgId()::equals);

        if (shouldCancel) event.setCanceled(true);
    }
}
