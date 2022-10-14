package dev.quarris.bossraids.raid.data;

import dev.quarris.bossraids.raid.BossRaidDataManager;
import dev.quarris.bossraids.ModRef;
import dev.quarris.bossraids.content.KeystoneBlock;
import dev.quarris.bossraids.raid.definitions.BossRaidDefinition;
import dev.quarris.bossraids.raid.definitions.WaveDefinition;
import dev.quarris.bossraids.util.InventoryUtils;
import dev.quarris.bossraids.util.ItemRequirement;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.LootTable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.CustomServerBossInfo;
import net.minecraft.server.CustomServerBossInfoManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerBossInfo;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class BossRaid {

    private static final int MAX_CHARGE_TIMER = 4 * 20;
    public static ScorePlayerTeam RAID_TEAM;

    //private ResourceLocation id;
    private final World level;
    private final BlockPos center;
    private final BossRaidDefinition definition;
    private final long raidId;

    private RaidState state = RaidState.INACTIVE;
    private List<ItemRequirement.Instance> requirements = new ArrayList<>();
    private int currentWave;

    private int chargeTimer = 0;
    private float totalHealth;

    private ResourceLocation bossbarId;
    private CustomServerBossInfo bossbar;

    private ServerPlayerEntity initiator;
    private UUID initiatorUUID;
    private List<ServerPlayerEntity> combatants = new ArrayList<>();
    private Map<String, RaidBoss> bosses = new HashMap<>();

    public BossRaid(ServerWorld level, BlockPos center, long raidId, ResourceLocation defId) {
        this.level = level;
        this.center = center;
        this.raidId = raidId;
        this.definition = BossRaidDataManager.INST.getRaidDefinition(defId);
        this.bossbarId = ModRef.res("bossraid_" + this.raidId);
        this.bossbar = level.getServer().getCustomBossEvents().create(this.bossbarId, StringTextComponent.EMPTY);
    }

    public BossRaid(ServerWorld level, CompoundNBT nbt) {
        this.level = level;
        this.definition = BossRaidDataManager.INST.getRaidDefinition(new ResourceLocation(nbt.getString("Definition")));
        this.raidId = nbt.getLong("Id");
        this.center = BlockPos.of(nbt.getLong("Center"));
        this.bossbarId = ModRef.res("bossraid_" + this.raidId);
        this.bossbar = level.getServer().getCustomBossEvents().get(this.bossbarId);
        this.state = RaidState.valueOf(nbt.getString("State"));
        this.currentWave = nbt.getInt("Wave");
        this.chargeTimer = nbt.getInt("Charge");
        this.initiatorUUID = nbt.getUUID("Initiator");
        PlayerEntity player = level.getPlayerByUUID(this.initiatorUUID);
        if (player != null) {
            this.initiator =  (ServerPlayerEntity) player;
        }
        for (INBT reqNbt : nbt.getList("Requirements", Constants.NBT.TAG_COMPOUND)) {
            this.requirements.add(ItemRequirement.deserialize((CompoundNBT) reqNbt));
        }
        for (INBT bossNbt : nbt.getList("Bosses", Constants.NBT.TAG_COMPOUND)) {
            CompoundNBT bossData = (CompoundNBT) bossNbt;
            this.bosses.put(bossData.getString("BossId"), new RaidBoss(bossData));
        }
    }

    public void setup(ServerPlayerEntity player) {
        this.initiator = player;
        this.initiatorUUID = player.getUUID();
        this.currentWave = 0;
        WaveDefinition wave = this.definition.getWave(this.currentWave);
        this.fillRequirements(wave);
        this.setupRequirementsBar();
        this.setState(RaidState.AWAITING);
    }

    private void setupRequirementsBar() {
        if (!this.requirements.isEmpty()) {
            this.setupBossBar(new StringTextComponent("Awaiting Items"), bossbar -> {
                bossbar.setMax(this.getRequiredCount());
                bossbar.setValue(0);
            });
        }
    }

    public void update() {
        if (this.level.isClientSide()) {
            return;
        }

        ServerWorld level = (ServerWorld) this.level;
        WaveDefinition wave = this.definition.getWave(this.currentWave);
        this.calculateCombatants(level);

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
    }

    // States

    private void onIdle(WaveDefinition wave) {
        this.fillRequirements(wave);
        this.setupRequirementsBar();
        this.setState(RaidState.AWAITING);
    }

    private void onAwaiting(ServerWorld level, WaveDefinition wave) {
        if (!this.requirements.isEmpty()) {
            this.bossbar.setValue(this.bossbar.getMax() - this.getRequiredCount());
            return;
        }

        this.initializeWave(level, wave);
        this.setState(RaidState.CHARGING);
    }

    private void onCharging(ServerWorld level, WaveDefinition wave) {
        if (this.chargeTimer < MAX_CHARGE_TIMER) {
            this.bossbar.setValue(this.chargeTimer);
            this.chargeTimer++;
            return;
        }

        this.startWave(level, wave);
        this.setState(RaidState.IN_PROGRESS);
    }

    private void onInProgress(ServerWorld level) {
        this.bossbar.setPlayers(this.combatants);
        if (this.combatants.isEmpty()) {
            //this.stop();
            //return;
        }

        float health = 0;
        Iterator<Map.Entry<String, RaidBoss>> ite = this.bosses.entrySet().iterator();
        while (ite.hasNext()) {
            Map.Entry<String, RaidBoss> entry = ite.next();
            String leaderId = entry.getKey();
            RaidBoss raidBoss = entry.getValue();
            if (raidBoss.shouldRemove(level)) {
                raidBoss.remove(level);
                ite.remove();
                continue;
            }

            raidBoss.update(level);
            health += raidBoss.getCurrentHealth(level);
        }

        this.totalHealth = health;
        this.bossbar.setValue((int) this.totalHealth);
        if (this.bosses.isEmpty()) {
            this.finalizeWave();
        }
    }

    public boolean tryInsertRequirement(ServerPlayerEntity player, ItemStack item) {
        Iterator<ItemRequirement.Instance> ite = this.requirements.iterator();
        while (ite.hasNext()) {
            ItemRequirement.Instance requirement = ite.next();
            if (requirement.matches(item)) {
                int removed = requirement.shrink(item.getCount());
                if (!player.isCreative()) {
                    item.shrink(removed);
                }
                if (requirement.isMet()) {
                    this.setInitiator(player);
                    ite.remove();
                }
                return true;
            }
        }

        return false;
    }

    // Logic Methods

    private void initializeWave(ServerWorld level, WaveDefinition wave) {
        this.chargeTimer = 0;
        this.setupBossBar(new StringTextComponent("Charging"), bossbar -> {
            bossbar.setMax(MAX_CHARGE_TIMER);
            bossbar.setValue(0);
        });

    }

    public void startWave(ServerWorld level, WaveDefinition wave) {
        wave.bosses.forEach(bossDef -> {
            Vector3d pos = Vector3d.atCenterOf(this.center);
            LivingEntity boss = bossDef.create(level, pos);
            if (boss == null) {
                ModRef.LOGGER.error("Unknown entity for raid '{}'", bossDef.getId());
                return;
            }
            this.level.addFreshEntity(boss);
            this.totalHealth += boss.getMaxHealth();

            LivingEntity mount = null;
            if (bossDef.mount != null) {
                mount = bossDef.mount.create(level, pos);
                this.level.addFreshEntity(mount);
                boss.startRiding(mount, true);
                this.totalHealth += mount.getMaxHealth();
            }
            LivingEntity rider = null;
            if (bossDef.rider != null) {
                rider = bossDef.rider.create(level, pos);
                this.level.addFreshEntity(rider);
                rider.startRiding(boss, true);
                this.totalHealth += rider.getMaxHealth();
            }
            RaidBoss raidBoss = new RaidBoss(boss, mount, rider, bossDef);
            this.bosses.put(bossDef.getId(), raidBoss);
        });

        this.setupBossBar(new StringTextComponent(wave.bossbar), bossbar -> {
            bossbar.setMax((int) this.totalHealth);
            bossbar.setValue((int) this.totalHealth);
        });
    }

    private void finalizeWave() {
        WaveDefinition wave = this.definition.getWave(this.currentWave);
        this.removeBossBar();

        if (!this.level.isClientSide()) {
            this.spawnLootTable(wave.loot);
        }

        this.currentWave++;
        this.setState(this.currentWave >= this.definition.getWaveCount() ? RaidState.COMPLETED : RaidState.IDLE);
    }

    private void calculateCombatants(ServerWorld level) {
        this.combatants = level.getPlayers(this::isWithinRange);
    }

    private void spawnLoot(List<ItemStack> loot) {
        List<IItemHandler> invs = new ArrayList<>();
        for (Direction dir : Direction.values()) {
            TileEntity blockEntity = this.level.getBlockEntity(this.center.relative(dir));
            if (blockEntity != null) {
                blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir.getOpposite()).ifPresent(invs::add);
            }
        }
        for (ItemStack stack : loot) {
            for (IItemHandler inv : invs) {
                stack = InventoryUtils.tryInsertItem(inv, stack);
            }

            if (!stack.isEmpty()) {
                ItemEntity itemEntity = new ItemEntity(this.level, this.center.getX() + 0.5, this.center.getY() + 1, this.center.getZ() + 0.5, stack);
                this.level.addFreshEntity(itemEntity);
            }
        }
    }

    private void setupBossBar(ITextComponent name, Consumer<CustomServerBossInfo> onCreate) {
        CustomServerBossInfoManager manager = this.level.getServer().getCustomBossEvents();
        this.removeBossBar();
        this.bossbar = manager.create(this.bossbarId, name);
        this.combatants.forEach(this.bossbar::addPlayer);
        onCreate.accept(this.bossbar);
    }

    private void fillRequirements(WaveDefinition wave) {
        this.requirements = wave.getRequirements().stream().map(ItemRequirement::inst).collect(Collectors.toList());
    }

    public void spawnFinalLoot() {
        this.spawnLootTable(this.definition.getLootTableId());
    }

    private void spawnLootTable(ResourceLocation id) {
        if (!this.level.isClientSide()) {
            ServerWorld level = (ServerWorld) this.level;
            LootTable lootTable = level.getServer().getLootTables().get(id);
            if (lootTable != LootTable.EMPTY) {
                LootContext.Builder builder = new LootContext.Builder(level).withParameter(LootParameters.ORIGIN, new Vector3d(this.center.getX(), this.center.getY(), this.center.getZ()));
                if (this.initiator != null) {
                    builder.withParameter(LootParameters.THIS_ENTITY, this.initiator);
                }

                List<ItemStack> loot = lootTable.getRandomItems(builder.create(LootParameterSets.CHEST));
                this.spawnLoot(loot);
            }
        }
    }

    public void setState(RaidState state) {
        this.level.setBlockAndUpdate(this.center, this.level.getBlockState(this.center).setValue(KeystoneBlock.RAID_STATE, state));
        this.state = state;
    }

    public RaidState getState() {
        return this.state;
    }

    private int getRequiredCount() {
        int total = 0;
        for (ItemRequirement.Instance req : this.requirements) {
            total += req.getCount();
        }
        return total;
    }

    public boolean isWithinRange(ServerPlayerEntity player) {
        return this.center.distSqr(player.blockPosition()) <= this.definition.getRadius() * this.definition.getRadius();
    }

    public void stop() {
        this.removeBossBar();
        this.cleanUpBosses();
    }

    private void removeBossBar() {
        if (!this.level.isClientSide() && this.bossbar != null) {
            this.bossbar.removeAllPlayers();
            this.level.getServer().getCustomBossEvents().remove(this.bossbar);
        }
    }

    private void cleanUpBosses() {
        if (this.level instanceof ServerWorld) {
            for (RaidBoss boss : this.bosses.values()) {
                boss.remove((ServerWorld) this.level);
            }
        }
    }

    public CompoundNBT serialize() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putString("Definition", BossRaidDataManager.INST.getId(this.definition).toString());
        nbt.putLong("Id", this.raidId);
        nbt.putLong("Center", this.center.asLong());
        nbt.putString("State", this.state.name());
        nbt.putInt("Wave", this.currentWave);
        nbt.putInt("Charge", this.chargeTimer);
        nbt.putUUID("Initiator", this.initiatorUUID);
        ListNBT requirementsList = new ListNBT();
        for (ItemRequirement.Instance req : this.requirements) {
            requirementsList.add(req.serialize());
        }
        nbt.put("Requirements", requirementsList);
        ListNBT bossesNbt = new ListNBT();
        for (Map.Entry<String, RaidBoss> entry : this.bosses.entrySet()) {
            CompoundNBT tag = entry.getValue().serialize();
            tag.putString("BossId", entry.getKey());
            bossesNbt.add(tag);
        }
        nbt.put("Bosses", bossesNbt);

        return nbt;
    }

    public long getId() {
        return this.raidId;
    }

    public void setInitiator(ServerPlayerEntity initiator) {
        this.initiator = initiator;
        this.initiatorUUID = initiator.getUUID();
    }

    public List<ItemRequirement.Instance> getRequirements() {
        return this.requirements;
    }

    public BlockPos getCenter() {
        return this.center;
    }
}
