package dev.quarris.bossraids.util;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.RangedInteger;

public class NbtUtils {

    public static CompoundNBT writeRangedInt(RangedInteger rangedInteger) {
        CompoundNBT tag = new CompoundNBT();
        tag.putInt("Min", rangedInteger.getMinInclusive());
        tag.putInt("Max", rangedInteger.getMaxInclusive());
        return tag;
    }

    public static RangedInteger readRangedInt(CompoundNBT tag) {
        return RangedInteger.of(tag.getInt("Min"), tag.getInt("Max"));
    }
}
