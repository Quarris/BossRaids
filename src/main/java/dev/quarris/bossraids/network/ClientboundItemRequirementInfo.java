package dev.quarris.bossraids.network;

import dev.quarris.bossraids.content.KeystoneTileEntity;
import dev.quarris.bossraids.util.ItemRequirement;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ClientboundItemRequirementInfo {

    private BlockPos pos;
    private List<ItemRequirement.Instance> items;

    public ClientboundItemRequirementInfo(BlockPos tilePos, List<ItemRequirement.Instance> items) {
        this.pos = tilePos;
        this.items = items;
    }

    public static void encode(ClientboundItemRequirementInfo packet, PacketBuffer buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeVarInt(packet.items.size());
        for (ItemRequirement.Instance inst : packet.items) {
            inst.writeToBuffer(buf);
        }
    }

    public static ClientboundItemRequirementInfo decode(PacketBuffer buf) {
        BlockPos pos = buf.readBlockPos();
        int size = buf.readVarInt();
        List<ItemRequirement.Instance> items = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            ItemRequirement.Instance inst = ItemRequirement.readFromBuffer(buf);
            items.add(inst);
        }

        return new ClientboundItemRequirementInfo(pos, items);
    }

    public static void handle(ClientboundItemRequirementInfo packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            IWorld level = mc.level;
            if (!level.isAreaLoaded(packet.pos, 0)) {
                return;
            }

            TileEntity blockEntity = level.getBlockEntity(packet.pos);
            if (blockEntity instanceof KeystoneTileEntity) {
                KeystoneTileEntity keystone = (KeystoneTileEntity) blockEntity;
                keystone.setDisplayItems(packet.items);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
