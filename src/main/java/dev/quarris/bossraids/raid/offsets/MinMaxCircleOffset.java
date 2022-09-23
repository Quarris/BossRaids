package dev.quarris.bossraids.raid.offsets;

import net.minecraft.util.math.vector.Vector3d;

import java.util.Random;

public class MinMaxCircleOffset implements IOffset {

    private final float minRadius;
    private final float maxRadius;
    private final float minHeight;
    private final float maxHeight;
    private final Random random;
    public MinMaxCircleOffset(float minRadius, float maxRadius, float minHeight, float maxHeight) {
        this.minRadius = minRadius;
        this.maxRadius = maxRadius;
        this.minHeight = minHeight;
        this.maxHeight = maxHeight <= minHeight ? minHeight + 1 : maxHeight;
        this.random = new Random();
    }

    @Override
    public Vector3d getOffset(Vector3d pos) {
        float y = this.randomBetween(this.random, this.minHeight, this.maxHeight);
        float angle = this.randomBetween(this.random, 0, 2 * (float) Math.PI);
        float distance = this.randomBetween(this.random, this.minRadius, this.maxRadius);
        Vector3d vOff = new Vector3d(1, y, 0).yRot(angle).multiply(distance, 1, distance);
        return pos.add(vOff);
    }
}
