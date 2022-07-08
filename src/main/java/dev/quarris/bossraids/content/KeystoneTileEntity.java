package dev.quarris.bossraids.content;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import dev.quarris.bossraids.BossRaidManager;
import dev.quarris.bossraids.ModRef;
import dev.quarris.bossraids.util.ItemRequirement;
import dev.quarris.bossraids.waves.BossRaidDefinition;
import dev.quarris.bossraids.waves.WaveDefinition;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.LootTable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.CustomServerBossInfo;
import net.minecraft.server.CustomServerBossInfoManager;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;

import java.util.*;
import java.util.stream.Collectors;

public class KeystoneTileEntity extends TileEntity implements ITickableTileEntity {
    private static final int MAX_CHARGE_TIMER = 4 * 20;

    private ResourceLocation bossRaidId;
    private BossRaidDefinition bossRaidDefinition;

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

    public KeystoneTileEntity() {
        super(ModContent.KEYSTONE_TILE.get());
    }

    public KeystoneAction activateWithItem(PlayerEntity player, ItemStack item) {
        if (item.isEmpty()) {
            if (!player.level.isClientSide()) {
                player.displayClientMessage(new StringTextComponent(String.valueOf(this.bossRaidId)), false);
                player.displayClientMessage(new StringTextComponent(String.valueOf(this.requirements)), false);
                player.displayClientMessage(new StringTextComponent(this.state.toString()), false);
            }
            return KeystoneAction.DISPLAY_REQUIREMENTS;
        }

        if (this.state.inProgress()) {
            return KeystoneAction.IN_PROGRESS;
        }

        if (player.isCreative() && item.getItem() == Items.NAME_TAG) {
            if (item.hasCustomHoverName()) {
                try {
                    ResourceLocation id = new ResourceLocation(item.getHoverName().getString());
                    this.setBossRaidId(id);
                    return KeystoneAction.RENAME;
                } catch (ResourceLocationException e) {
                    // Name is not a valid resource name, do not try to change the boss wave id
                }
            }
        }

        if (this.bossRaidDefinition == null) {
            return KeystoneAction.INVALID;
        }

        if (this.state.awaiting()) {
            // Add required item
            if (this.tryInsertRequirement(player, item)) {
                return KeystoneAction.INSERT;
            }
        }

        return KeystoneAction.INVALID;
    }

    @Override
    public void tick() {
        if (this.level.isClientSide()) {
            return;
        }

        if (this.bossRaidDefinition == null) {
            return;
        }

        if (this.state.inactive()) {
            this.onInactive();
        }

        ServerWorld level = (ServerWorld) this.level;
        WaveDefinition wave = this.bossRaidDefinition.getWave(this.currentWave);

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
            this.spawnLootTable(this.bossRaidDefinition.getLootTableId());
        }

        this.setState(RaidState.INACTIVE);
    }

    public void setState(RaidState state) {
        this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(KeystoneBlock.RAID_STATE_PROP, state), Constants.BlockFlags.DEFAULT);
        this.state = state;
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
            LivingEntity boss = bossDef.create(level, this.getBlockPos());
            this.level.addFreshEntity(boss);
            this.bosses.put(bossDef.getId(), boss);
            this.totalHealth += boss.getHealth();
        });

        this.bossbar.setMax((int) this.totalHealth);
    }

    private void finalizeWave() {
        WaveDefinition wave = this.bossRaidDefinition.getWave(this.currentWave);

        this.removeBossBar();
        // Kill minions
        // Spawn wave loot
        if (!this.level.isClientSide()) {
            this.spawnLootTable(wave.loot);
        }

        this.currentWave++;
        this.state = RaidState.IDLE;
    }

    private void updateBossBar(ServerWorld level) {
        level.getPlayers(this::isWithinRange).forEach(this.bossbar::addPlayer);
        this.bossbar.setValue((int) this.totalHealth);
    }

    private void spawnLoot(List<ItemStack> loot) {
        for (ItemStack stack : loot) {
            ItemEntity itemEntity = new ItemEntity(this.level, this.getBlockPos().getX() + 0.5, this.getBlockPos().getY() + 1, this.getBlockPos().getZ() + 0.5, stack);
            this.level.addFreshEntity(itemEntity);
        }
    }

    private void spawnLootTable(ResourceLocation id) {
        if (!this.level.isClientSide()) {
            ServerWorld level = (ServerWorld) this.level;
            LootTable lootTable = level.getServer().getLootTables().get(id);
            if (lootTable != LootTable.EMPTY) {
                LootContext ctx = new LootContext.Builder(level)
                        .withParameter(LootParameters.ORIGIN, new Vector3d(this.getBlockPos().getX(), this.getBlockPos().getY(), this.getBlockPos().getZ()))
                        .withParameter(LootParameters.THIS_ENTITY, this.initiator).create(LootParameterSets.CHEST);

                List<ItemStack> loot = lootTable.getRandomItems(ctx);
                this.spawnLoot(loot);
            }
        }
    }

    private boolean tryInsertRequirement(PlayerEntity player, ItemStack item) {
        Iterator<ItemRequirement.Instance> ite = this.requirements.iterator();
        while (ite.hasNext()) {
            ItemRequirement.Instance requirement = ite.next();
            if (requirement.matches(item)) {
                int removed = requirement.shrink(item.getCount());
                item.shrink(removed);
                if (requirement.isMet()) {
                    this.initiator = player;
                    ite.remove();
                }
                return true;
            }
        }

        return false;
    }

    public boolean isWithinRange(ServerPlayerEntity player) {
        return this.getBlockPos().distSqr(player.blockPosition()) <= 96 * 96;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        this.removeBossBar();
    }

    private void fillRequirements(WaveDefinition wave) {
        this.requirements = wave.getRequirements().stream().map(ItemRequirement::inst).collect(Collectors.toList());
    }

    private void removeBossBar() {
        if (!this.level.isClientSide() && this.bossbar != null) {
            this.bossbar.removeAllPlayers();
            ((ServerWorld) this.level).getServer().getCustomBossEvents().remove(this.bossbar);
        }
    }

    public void setBossRaidId(ResourceLocation id) {
        this.bossRaidId = id;
        this.bossRaidDefinition = BossRaidManager.INST.getBossWave(this.bossRaidId);
        ModRef.LOGGER.debug("Setting boss raid {}", this.bossRaidDefinition);
    }

    @Override
    public void setLevelAndPosition(World level, BlockPos pos) {
        super.setLevelAndPosition(level, pos);
        this.bossbarId = ModRef.res("bossbar_" + pos.asLong());
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        nbt = super.save(nbt);
        if (this.bossRaidId != null) {
            nbt.putString("BossRaidId", this.bossRaidId.toString());
        }
        return nbt;
    }

    @Override
    public void load(BlockState state, CompoundNBT nbt) {
        super.load(state, nbt);
        if (nbt.contains("BossRaidId")) {
            this.setBossRaidId(new ResourceLocation(nbt.getString("BossRaidId")));
        }
    }

    public enum KeystoneAction {
        INSERT, RENAME, DISPLAY_REQUIREMENTS, IN_PROGRESS, INVALID
    }

    public enum RaidState implements IStringSerializable {
        INACTIVE, IDLE, AWAITING, CHARGING, IN_PROGRESS, COMPLETED;

        public boolean inactive() {
            return this == INACTIVE;
        }

        public boolean inProgress() {
            return this == IN_PROGRESS;
        }

        public boolean idle() {
            return this == IDLE;
        }

        public boolean awaiting() {
            return this == AWAITING;
        }

        public boolean charging() {
            return this == CHARGING;
        }

        public boolean completed() {
            return this == COMPLETED;
        }

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }
}
