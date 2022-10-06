package dev.quarris.bossraids.util.offsets;

import com.google.gson.JsonObject;
import net.minecraft.util.math.vector.Vector3d;

import java.util.Random;

public class RandomCircleOffset implements IOffset {

    private final float radius;
    private final float minHeight;
    private final float maxHeight;
    private final Random random;

    public RandomCircleOffset(float radius, float minHeight, float maxHeight) {
        this.radius = radius;
        this.minHeight = minHeight;
        this.maxHeight = maxHeight <= minHeight ? minHeight + 1 : maxHeight;
        this.random = new Random();
    }

    @Override
    public Vector3d getOffset(Vector3d pos) {
        float y = this.randomBetween(this.random, this.minHeight, this.maxHeight);
        float angle = this.randomBetween(this.random, 0, 2 * (float) Math.PI);
        float distance = this.randomBetween(this.random, 0, this.radius);
        Vector3d vOff = new Vector3d(1, y, 0).yRot(angle).multiply(distance, 1, distance);
        return pos.add(vOff);
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", "randomCircle");
        json.addProperty("radius", this.radius);
        json.addProperty("minHeight", this.minHeight);
        json.addProperty("maxHeight", this.maxHeight);
        return json;
    }
}
