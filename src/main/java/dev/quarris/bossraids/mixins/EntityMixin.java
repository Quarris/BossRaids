package dev.quarris.bossraids.mixins;

import dev.quarris.bossraids.raid.data.BossRaid;
import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.Team;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow @Nullable public abstract Team getTeam();

    /*@Inject(method = "changeDimension(Lnet/minecraft/world/server/ServerWorld;)Lnet/minecraft/entity/Entity;",
    at = @At("HEAD"), cancellable = true)
    private void cancelDimensionChange(ServerWorld p_241206_1_, CallbackInfoReturnable<Entity> cir) {
        if (this.getTeam() == BossRaid.RAID_TEAM) {
            cir.setReturnValue(null);
        }
    }*/

    @Inject(method = "canChangeDimensions", at = @At("HEAD"), cancellable = true)
    private void cancelDimensionChange(CallbackInfoReturnable<Boolean> cir) {
        if (this.getTeam() == BossRaid.RAID_TEAM) {
            cir.setReturnValue(false);
        }
    }
}
