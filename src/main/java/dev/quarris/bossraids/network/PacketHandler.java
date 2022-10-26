package dev.quarris.bossraids.network;

import dev.quarris.bossraids.ModRef;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.Optional;

public class PacketHandler {

    private static final String VERSION = "1";
    private static final SimpleChannel CHANNEL =
        NetworkRegistry.newSimpleChannel(
            ModRef.res("network"),
            () -> VERSION,
            VERSION::equals,
            VERSION::equals
        );

    public static void init() {
        CHANNEL.registerMessage(0, ClientboundItemRequirementInfo.class, ClientboundItemRequirementInfo::encode, ClientboundItemRequirementInfo::decode, ClientboundItemRequirementInfo::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    public static <MSG> void sendAllAround(MSG packet, BlockPos pos, RegistryKey<World> dimension, double radius) {
        CHANNEL.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(pos.getX(), pos.getY(), pos.getZ(), radius, dimension)), packet);
    }

}
