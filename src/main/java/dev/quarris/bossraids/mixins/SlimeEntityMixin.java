package dev.quarris.bossraids.mixins;

import dev.quarris.bossraids.raid.BossRaidManager;
import dev.quarris.bossraids.raid.data.BossRaid;
import dev.quarris.bossraids.raid.data.RaidBoss;
import dev.quarris.bossraids.raid.definitions.EntityDefinition;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(SlimeEntity.class)
public abstract class SlimeEntityMixin extends MobEntity {

    protected SlimeEntityMixin(EntityType<? extends MobEntity> p_i48576_1_, World p_i48576_2_) {
        super(p_i48576_1_, p_i48576_2_);
    }

    @Inject(method = "remove", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addFreshEntity(Lnet/minecraft/entity/Entity;)Z"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void addSplittersToRaid(boolean keepData, CallbackInfo ci, int i, ITextComponent itextcomponent, boolean flag, float f, int newSize, int splits, int l, float f1, float f2, SlimeEntity splitter) {
        if (!(this.level instanceof ServerWorld)) {
            return;
        }

        if (!this.getPersistentData().contains("BossRaidData")) {
            return;
        }

        ServerWorld level = ((ServerWorld) this.level);

        CompoundNBT bossData = this.getPersistentData().getCompound("BossRaidData");
        long raidId = bossData.getLong("RaidId");
        String bossId = bossData.getString("BossId");

        BossRaid raid = BossRaidManager.getBossRaids(level).get(raidId);
        if (raid == null || raid.getState().inactive()) {
            return;
        }

        RaidBoss boss = raid.getRaidBoss(bossId);
        if (boss == null) {
            return;
        }

        boss.addSubBoss(splitter.getUUID());
        Vector3d pos = splitter.position();

        EntityDefinition definition = boss.definition;
        String type = "";
        if (bossData.contains("BossType")) {
            type = bossData.getString("BossType");
            if ("Mount".equals(type)) {
                definition = boss.definition.mount;
            } else if ("Rider".equals(type)) {
                definition = boss.definition.rider;
            }
        }

        double base = this.getAttributeBaseValue(Attributes.MAX_HEALTH);
        definition.configureEntity(raidId, level, Vector3d.atCenterOf(boss.spawnPos), splitter);
        boss.definition.applyBossData(raidId, level, splitter, type);
        splitter.getAttribute(Attributes.MAX_HEALTH).setBaseValue(base / (splits + 1));

        splitter.setPosAndOldPos(pos.x, pos.y, pos.z);
        splitter.getEntityData().set(SlimeEntity.ID_SIZE, newSize);
        splitter.reapplyPosition();
        splitter.refreshDimensions();
        splitter.setHealth(splitter.getMaxHealth());

        raid.getBossbar().setMax((int) (raid.getBossbar().getMax() + splitter.getMaxHealth()));
    }
}
