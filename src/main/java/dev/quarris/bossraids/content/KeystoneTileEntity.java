package dev.quarris.bossraids.content;

import dev.quarris.bossraids.init.ModContent;
import dev.quarris.bossraids.network.ClientboundItemRequirementInfo;
import dev.quarris.bossraids.network.PacketHandler;
import dev.quarris.bossraids.raid.BossRaidDataManager;
import dev.quarris.bossraids.raid.BossRaidManager;
import dev.quarris.bossraids.raid.data.BossRaid;
import dev.quarris.bossraids.raid.data.RaidState;
import dev.quarris.bossraids.raid.definitions.BossRaidDefinition;
import dev.quarris.bossraids.util.ItemRequirement;
import net.minecraft.block.BlockState;
import net.minecraft.command.arguments.ItemInput;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class KeystoneTileEntity extends TileEntity implements ITickableTileEntity {

    private ResourceLocation defId;
    private boolean isActive;
    private long raidId;

    @OnlyIn(Dist.CLIENT)
    public List<ItemRequirement.Instance> displayItems;

    public KeystoneTileEntity() {
        super(ModContent.KEYSTONE_TILE.get());
    }

    public KeystoneBlock.KeystoneAction activateWithItem(PlayerEntity player, ItemStack item) {
        // Set raid definition id
        if (player.isCreative() && item.getItem() == Items.NAME_TAG) {
            if (item.hasCustomHoverName()) {
                try {
                    ResourceLocation id = new ResourceLocation(item.getHoverName().getString());
                    if (BossRaidDataManager.INST.getRaidDefinition(id) != null) {
                        this.defId = id;
                        this.sendRequirementsToClient();
                        return KeystoneBlock.KeystoneAction.RENAME;
                    }
                } catch (ResourceLocationException e) {
                    // Name is not a valid resource name, do not try to change the boss raid id
                }
            }
        }

        // Print debug
        if (item.isEmpty()) {
            if (!player.level.isClientSide()) {
                /*if (this.defId != null) {
                    player.displayClientMessage(new StringTextComponent("DefId: " + this.defId), false);
                }
                if (!this.isActive) {
                    player.displayClientMessage(new StringTextComponent("Not active"), false);
                    if (this.defId != null) {
                        player.displayClientMessage(new StringTextComponent("Requirements: " + BossRaidDataManager.INST.getRaidDefinition(this.defId).getWave(0).getRequirements()), false);
                    }
                } else {
                    player.displayClientMessage(new StringTextComponent("RaidId: " + this.raidId), false);
                    player.displayClientMessage(new StringTextComponent("State: " + this.getBlockState().getValue(KeystoneBlock.RAID_STATE)), false);
                    List<ItemRequirement.Instance> reqs = BossRaidManager.getBossRaids((ServerWorld) this.level).get(this.raidId).getRequirements();
                    player.displayClientMessage(new StringTextComponent("Requirements: " + reqs), false);
                }*/
                List<ItemRequirement.Instance> requirements = null;
                if (this.isActive && this.getBlockState().getValue(KeystoneBlock.RAID_STATE) == RaidState.AWAITING) {
                    requirements = BossRaidManager.getBossRaids((ServerWorld) player.level).get(this.raidId).getRequirements();
                } else if (!this.isActive && this.defId != null) {
                    requirements = BossRaidDataManager.INST.getRaidDefinition(this.defId).getWave(0).getRequirements().stream().map(ItemRequirement::inst).collect(Collectors.toList());
                }

                if (requirements != null) {
                    player.displayClientMessage(new StringTextComponent("Requires: "), false);
                    for (ItemRequirement.Instance req : requirements) {
                        player.displayClientMessage(new StringTextComponent(" - " + req.getRequirementDisplay()), false);
                    }
                }
            }
            return KeystoneBlock.KeystoneAction.DISPLAY_REQUIREMENTS;
        }

        if (this.defId == null) {
            return KeystoneBlock.KeystoneAction.INVALID;
        }

        // Server logic
        ServerWorld serverLevel = (ServerWorld) this.level;
        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;

        BossRaidManager manager = BossRaidManager.getBossRaids(serverLevel);
        BossRaidDefinition raidDefinition = BossRaidDataManager.INST.getRaidDefinition(this.defId);

        // Try activating the keystone with a new raid, if it's not yet active.
        if (!this.isActive) {
            Optional<Long> id = manager.tryActivateRaid(serverPlayer, this.worldPosition, raidDefinition, item);
            if (id.isPresent()) {
                this.raidId = id.get();
                this.setActive(true);
                this.sendRequirementsToClient();
                return KeystoneBlock.KeystoneAction.ACTIVATE;
            }

            return KeystoneBlock.KeystoneAction.INVALID;
        }

        BossRaid raid = manager.get(this.raidId);
        if (raid.tryInsertRequirement(serverPlayer, item)) {
            this.sendRequirementsToClient();
            return KeystoneBlock.KeystoneAction.INSERT;
        }

        return KeystoneBlock.KeystoneAction.INVALID;
    }

    @Override
    public void tick() {
        if (this.level.isClientSide()) {
            return;
        }

        if (this.level.getGameTime() % 40 == 0) {
            this.sendRequirementsToClient();
        }

        ServerWorld serverLevel = (ServerWorld) this.level;
        if (this.isActive) {
            BossRaid raid = BossRaidManager.getBossRaids(serverLevel).get(this.raidId);
            if (raid == null) {
                this.setActive(false);
            }
        }
    }

    public void sendRequirementsToClient() {
        if (this.level.isClientSide()) {
            return;
        }

        List<ItemRequirement.Instance> requirements = Collections.emptyList();
        if (this.isActive && this.getBlockState().getValue(KeystoneBlock.RAID_STATE) == RaidState.AWAITING) {
            requirements = BossRaidManager.getBossRaids((ServerWorld) this.level).get(this.raidId).getRequirements();
        } else if (!this.isActive && this.defId != null) {
            requirements = BossRaidDataManager.INST.getRaidDefinition(this.defId).getWave(0).getRequirements().stream().map(ItemRequirement::inst).collect(Collectors.toList());
        }

        ClientboundItemRequirementInfo packet = new ClientboundItemRequirementInfo(this.worldPosition, requirements);
        PacketHandler.sendAllAround(packet, this.worldPosition, this.level.dimension(), 32);
    }

    public boolean isActive() {
        return this.isActive;
    }

    public long getRaidId() {
        return this.raidId;
    }

    @OnlyIn(Dist.CLIENT)
    public void setDisplayItems(List<ItemRequirement.Instance> items) {
        this.displayItems = items;
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        nbt = super.save(nbt);
        if (this.defId != null) {
            nbt.putString("RaidDefinition", this.defId.toString());
        }
        if (this.isActive) {
            nbt.putLong("RaidId", this.raidId);
        }
        return nbt;
    }

    @Override
    public void load(BlockState state, CompoundNBT nbt) {
        super.load(state, nbt);
        if (nbt.contains("RaidDefinition")) {
            this.defId = new ResourceLocation(nbt.getString("RaidDefinition"));
        }
        if (nbt.contains("RaidId")) {
            this.raidId = nbt.getLong("RaidId");
            this.isActive = true;
        }
    }

    public void setRaid(ResourceLocation id) {
        this.defId = id;
    }

    public void setActive(boolean active) {
        if (this.isActive != active) {
            this.isActive = active;
            this.level.setBlockAndUpdate(this.worldPosition, this.getBlockState().setValue(KeystoneBlock.ACTIVE, active));
        }
    }

    public void clearRaid() {
        this.setActive(false);
    }
}
