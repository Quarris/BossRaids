package dev.quarris.bossraids.raid;

import dev.quarris.bossraids.ModRef;
import dev.quarris.bossraids.raid.data.BossRaid;
import dev.quarris.bossraids.raid.definitions.EntityDefinition;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = ModRef.ID)
public class ClientRaidEvents {

    @SubscribeEvent()
    public static void removeBossBars(RenderGameOverlayEvent.BossInfo event) {
        System.out.println(event.getBossInfo().getId());
    }
}
