package dev.quarris.bossraids.raid;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import dev.quarris.bossraids.BossRaidManager;
import dev.quarris.bossraids.ModRef;
import dev.quarris.bossraids.content.KeystoneBlock;
import dev.quarris.bossraids.content.KeystoneTileEntity;
import dev.quarris.bossraids.raid.data.BossRaidDefinition;
import dev.quarris.bossraids.raid.data.WaveDefinition;
import dev.quarris.bossraids.util.ItemRequirement;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.LootTable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.CustomServerBossInfo;
import net.minecraft.server.CustomServerBossInfoManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;

import java.util.*;
import java.util.stream.Collectors;

public class BossRaid {

    private static final int MAX_CHARGE_TIMER = 4 * 20;

    private World level;
    private BlockPos pos;

    private ResourceLocation id;
    private BossRaidDefinition definition;

    private List<ItemRequirement.Instance> requirements = Collections.emptyList();

    private RaidState state = RaidState.INACTIVE;
    private int currentWave;
    private float totalHealth;

    private ResourceLocation bossbarId;
    private CustomServerBossInfo bossbar;

    private int chargeTimer = 0;

    private PlayerEntity initiator;
    private List<PlayerEntity> combatants = new ArrayList<>();
    private Map<String, LivingEntity> bosses = new HashMap<>();
    private Multimap<String, Entity> minions = HashMultimap.create();

    public BossRaid(KeystoneTileEntity tile, ResourceLocation id) {
        this.setLevelAndPos(tile.getLevel(), tile.getBlockPos());
        this.setId(id);
    }

    public BossRaid(KeystoneTileEntity tile, CompoundNBT nbt) {
        this.setLevelAndPos(tile.getLevel(), tile.getBlockPos());
        this.deserialize(nbt);
    }

    public boolean tryInsertRequirement(PlayerEntity player, ItemStack item) {
        Iterator<ItemRequirement.Instance> ite = this.requirements.iterator();
        while (ite.hasNext()) {
            ItemRequirement.Instance requirement = ite.next();
            if (requirement.matches(item)) {
                int removed = requirement.shrink(item.getCount());
                item.shrink(removed);
                if (requirement.isMet()) {
                    this.setInitiator(player);
                    ite.remove();
                }
                return true;
            }
        }

        return false;
    }

    public void update() {
        if (this.level.isClientSide()) {
            return;
        }

        ServerWorld level = (ServerWorld) this.level;

        if (this.definition == null) {
            return;
        }

        if (this.state.inactive()) {
            this.onInactive();
        }

        WaveDefinition wave = this.definition.getWave(this.currentWave);

        if (this.state.idle()) {
            this.onIdle(wave);
        }

        if (this.state.awaiting()) {
            this.onAwaiting(level, wave);
        }

        if (this.state.charging()) {
            this.onCharging(level, wave);
        }

        if (this.state.inProgress()) {
            this.onInProgress(level);
        }

        if (this.state.completed()) {
            this.onCompleted();
        }
    }

    // States
    private void onInactive() {
        this.currentWave = 0;
        this.state = RaidState.IDLE;
    }

    private void onIdle(WaveDefinition wave) {
        if (wave == null) {
            this.setState(RaidState.COMPLETED);
            return;
        }

        this.fillRequirements(wave);
        this.setState(RaidState.AWAITING);
    }

    private void onAwaiting(ServerWorld level, WaveDefinition wave) {
        if (this.requirements.isEmpty()) {
            this.initializeWave(level, wave);
            this.chargeTimer = 0;
            this.setState(RaidState.CHARGING);
        }
    }

    private void onCharging(ServerWorld level, WaveDefinition wave) {
        if (this.chargeTimer++ > MAX_CHARGE_TIMER) {
            this.startWave(level, wave);
            this.setState(RaidState.IN_PROGRESS);
        } else {
            this.bossbar.setValue(this.chargeTimer);
        }
    }

    private void onInProgress(ServerWorld level) {
        // Spawn Minions
        int health = 0;
        Iterator<Map.Entry<String, LivingEntity>> ite = this.bosses.entrySet().iterator();
        while (ite.hasNext()) {
            Map.Entry<String, LivingEntity> entry = ite.next();
            String leaderId = entry.getKey();
            LivingEntity boss = entry.getValue();
            if (!boss.isAlive()) {
                ite.remove();
                continue;
            }

            health += boss.getHealth();
        }

        this.totalHealth = health;
        this.updateBossBar(level);
        if (this.bosses.isEmpty()) {
            this.finalizeWave();
        }
    }

    private void onCompleted() {
        if (!this.level.isClientSide()) {
            this.spawnLootTable(this.definition.getLootTableId());
        }

        this.setState(RaidState.INACTIVE);
    }

    public void setState(RaidState state) {
        this.level.setBlock(this.pos, this.level.getBlockState(this.pos).setValue(KeystoneBlock.RAID_STATE_PROP, state), Constants.BlockFlags.DEFAULT);
        this.state = state;
    }

    public RaidState getState() {
        return this.state;
    }

    // Logic Methods
    private void initializeWave(ServerWorld level, WaveDefinition wave) {
        CustomServerBossInfoManager bossbarManager = level.getServer().getCustomBossEvents();
        this.bossbar = bossbarManager.create(this.bossbarId, new StringTextComponent(wave.bossbar));
        level.getPlayers(this::isWithinRange).forEach(this.bossbar::addPlayer);
        this.bossbar.setMax(MAX_CHARGE_TIMER);
        this.setState(RaidState.IN_PROGRESS);
    }

    public void startWave(ServerWorld level, WaveDefinition wave) {
        wave.bosses.forEach(bossDef -> {
            LivingEntity boss = bossDef.create(level, this.pos);
            if (boss == null) {
                ModRef.LOGGER.error("Unknown entity for raid '{}'", bossDef.getId());
                return;
            }
            this.level.addFreshEntity(boss);
            this.bosses.put(bossDef.getId(), boss);
            this.totalHealth += boss.getHealth();
        });

        this.bossbar.setMax((int) this.totalHealth);
    }

    private void finalizeWave() {
        WaveDefinition wave = this.definition.getWave(this.currentWave);

        this.removeBossBar();
        // Kill minions
        // Spawn wave loot
        if (!this.level.isClientSide()) {
            this.spawnLootTable(wave.loot);
        }

        this.currentWave++;
        this.state = RaidState.IDLE;
    }

    public void onRemoved() {
        this.removeBossBar();
    }

    private void updateBossBar(ServerWorld level) {
        level.getPlayers(this::isWithinRange).forEach(this.bossbar::addPlayer);
        this.bossbar.setValue((int) this.totalHealth);
    }

    private void spawnLoot(List<ItemStack> loot) {
        for (ItemStack stack : loot) {
            ItemEntity itemEntity = new ItemEntity(this.level, this.pos.getX() + 0.5, this.pos.getY() + 1, this.pos.getZ() + 0.5, stack);
            this.level.addFreshEntity(itemEntity);
        }
    }

    private void spawnLootTable(ResourceLocation id) {
        if (!this.level.isClientSide()) {
            ServerWorld level = (ServerWorld) this.level;
            LootTable lootTable = level.getServer().getLootTables().get(id);
            if (lootTable != LootTable.EMPTY) {
                LootContext ctx = new LootContext.Builder(level)
                        .withParameter(LootParameters.ORIGIN, new Vector3d(this.pos.getX(), this.pos.getY(), this.pos.getZ()))
                        .withParameter(LootParameters.THIS_ENTITY, this.initiator).create(LootParameterSets.CHEST);

                List<ItemStack> loot = lootTable.getRandomItems(ctx);
                this.spawnLoot(loot);
            }
        }
    }

    private void fillRequirements(WaveDefinition wave) {
        this.requirements = wave.getRequirements().stream().map(ItemRequirement::inst).collect(Collectors.toList());
    }

    public boolean isWithinRange(ServerPlayerEntity player) {
        return this.pos.distSqr(player.blockPosition()) <= 96 * 96;
    }

    private void removeBossBar() {
        if (!this.level.isClientSide() && this.bossbar != null) {
            this.bossbar.removeAllPlayers();
            this.level.getServer().getCustomBossEvents().remove(this.bossbar);
        }
    }

    public CompoundNBT serialize() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putString("Id", this.id.toString());
        return nbt;
    }

    public void deserialize(CompoundNBT nbt) {
        this.setId(new ResourceLocation(nbt.getString("Id")));
    }

    public ResourceLocation getId() {
        return this.id;
    }

    public void setId(ResourceLocation id) {
        this.id = id;
        this.definition = BossRaidManager.INST.getBossWave(this.id);
        ModRef.LOGGER.debug("Setting boss raid {}", this.definition);
    }

    public void setLevelAndPos(World level, BlockPos pos) {
        this.level = level;
        this.pos = pos;
        this.bossbarId = ModRef.res("bossbar_" + pos.asLong());
    }

    public void setInitiator(PlayerEntity initiator) {
        this.initiator = initiator;
    }

    public List<ItemRequirement.Instance> getRequirements() {
        return this.requirements;
    }
}
