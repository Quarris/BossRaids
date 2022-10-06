package dev.quarris.bossraids.util.offsets;

import com.google.gson.JsonObject;
import net.minecraft.util.math.vector.Vector3d;

import java.util.Random;

public class RandomSquareOffset implements IOffset {

    private final float radius;
    private final float minHeight;
    private final float maxHeight;
    private final Random random;
    public RandomSquareOffset(float radius, float minHeight, float maxHeight) {
        this.radius = radius;
        this.minHeight = minHeight;
        this.maxHeight = maxHeight <= minHeight ? minHeight + 1 : maxHeight;
        this.random = new Random();
    }

    @Override
    public Vector3d getOffset(Vector3d pos) {
        float x = this.randomBetween(this.random, -this.radius, this.radius + 1);
        float z = this.randomBetween(this.random, -this.radius, this.radius + 1);
        float y = this.randomBetween(this.random, this.minHeight, this.maxHeight);
        return pos.add(x, y, z);
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", "randomSquare");
        json.addProperty("radius", this.radius);
        json.addProperty("minHeight", this.minHeight);
        json.addProperty("maxHeight", this.maxHeight);
        return json;
    }
}
