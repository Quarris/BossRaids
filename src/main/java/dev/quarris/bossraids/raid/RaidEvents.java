package dev.quarris.bossraids.raid;

import com.google.gson.Gson;
import dev.quarris.bossraids.ModRef;
import dev.quarris.bossraids.raid.data.BossRaid;
import dev.quarris.bossraids.raid.definitions.EntityDefinition;
import dev.quarris.bossraids.util.BossRaidUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ModRef.ID)
public class RaidEvents {

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

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void removeRaidDrops(LivingDropsEvent event) {
        if (event.getEntityLiving().getTeam() == BossRaid.RAID_TEAM) {
            event.setCanceled(true);
        }
    }
}
