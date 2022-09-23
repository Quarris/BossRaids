package dev.quarris.bossraids.raid.offsets;

import net.minecraft.util.math.vector.Vector3d;

import java.util.Random;

public class MinMaxSquareOffset implements IOffset {

    private final float minRadius;
    private final float maxRadius;
    private final float minHeight;
    private final float maxHeight;
    private final Random random;

    public MinMaxSquareOffset(float minRadius, float maxRadius, float minHeight, float maxHeight) {
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
        float unit = getSquareDistance(angle);
        float minDist = this.minRadius * unit;
        float maxDist = this.maxRadius * unit;
        float distance = this.randomBetween(this.random, minDist, maxDist);
        Vector3d vOff = new Vector3d(distance, y, 0).yRot(angle);
        return pos.add(vOff);
    }

    private static float getSquareDistance(float angle) {
        angle = (float) ((angle + Math.PI / 4) % (Math.PI / 2) - Math.PI / 4);
        return 1 / (float)Math.cos(angle);
    }
}
