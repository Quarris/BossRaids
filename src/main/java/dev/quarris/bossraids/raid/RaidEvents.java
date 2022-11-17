package dev.quarris.bossraids.raid;

import com.google.gson.Gson;
import dev.quarris.bossraids.ModRef;
import dev.quarris.bossraids.raid.data.BossRaid;
import dev.quarris.bossraids.raid.definitions.EntityDefinition;
import dev.quarris.bossraids.util.BossRaidUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = ModRef.ID)
public class RaidEvents {

    @SubscribeEvent
    public static void cancelExplosionBlockDamage(ExplosionEvent.Detonate event) {
        LivingEntity sourceMob = event.getExplosion().getSourceMob();
        if (sourceMob != null && sourceMob.getTeam() == BossRaid.RAID_TEAM) {
            event.getAffectedBlocks().clear();
        }
    }

    @SubscribeEvent
    public static void cancelDamages(LivingAttackEvent event) {
        if (event.getSource().getEntity() != null) {
            if (event.getEntityLiving().getTeam() == BossRaid.RAID_TEAM && event.getSource().getEntity().getTeam() == BossRaid.RAID_TEAM) {
                event.setCanceled(true);
                return;
            }
        }

        CompoundNBT tag = event.getEntityLiving().getPersistentData();
        if (!tag.contains(EntityDefinition.DAMAGE_IMMUNITIES_TAG)) {
            return;
        }

        List<String> damageImmunities = tag.getList(EntityDefinition.DAMAGE_IMMUNITIES_TAG, Constants.NBT.TAG_STRING).stream()
            .map(INBT::getAsString).collect(Collectors.toList());

        if (damageImmunities.contains("fakePlayer") && event.getSource().getDirectEntity() instanceof FakePlayer) {
            event.setCanceled(true);
            return;
        }

        if (damageImmunities.contains(event.getSource().getMsgId())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void removeRaidDrops(LivingDropsEvent event) {
        if (event.getEntityLiving().getTeam() == BossRaid.RAID_TEAM) {
            event.setCanceled(true);
        }
    }
}
