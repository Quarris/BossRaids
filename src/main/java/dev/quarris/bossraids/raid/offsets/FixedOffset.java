package dev.quarris.bossraids.raid.offsets;

import net.minecraft.util.math.vector.Vector3d;

public class FixedOffset implements IOffset {

    private final float x, y, z;
    public FixedOffset(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public Vector3d getOffset(Vector3d pos) {
        return pos.add(this.x, this.y, this.z);
    }
}
