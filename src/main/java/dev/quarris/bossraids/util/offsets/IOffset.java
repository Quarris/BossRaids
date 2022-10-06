package dev.quarris.bossraids.util.offsets;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.util.math.vector.Vector3d;

import java.util.Random;

public interface IOffset {
    Vector3d getOffset(Vector3d pos);

    JsonObject toJson();

    default float randomBetween(Random random, float min, float max) {
        return min + random.nextFloat() * (max - min);
    }

    static IOffset fromJson(JsonObject json) {
        String type = json.get("type").getAsString();
        switch (type) {
            case "randomCircle": {
                float radius = json.get("radius").getAsFloat();
                int minHeight = json.get("minHeight").getAsInt();
                int maxHeight = minHeight;
                if (json.has("maxHeight")) {
                    maxHeight = json.get("maxHeight").getAsInt();
                }
                return new RandomCircleOffset(radius, minHeight, maxHeight);
            }
            case "randomSquare": {
                float radius = json.get("radius").getAsFloat();
                int minHeight = json.get("minHeight").getAsInt();
                int maxHeight = minHeight;
                if (json.has("maxHeight")) {
                    maxHeight = json.get("maxHeight").getAsInt();
                }
                return new RandomSquareOffset(radius, minHeight, maxHeight);
            }
            case "minmaxSquare": {
                float minRadius = json.get("minRadius").getAsFloat();
                float maxRadius = json.get("maxRadius").getAsFloat();
                int minHeight = json.get("minHeight").getAsInt();
                int maxHeight = minHeight;
                if (json.has("maxHeight")) {
                    maxHeight = json.get("maxHeight").getAsInt();
                }
                return new MinMaxSquareOffset(minRadius, maxRadius, minHeight, maxHeight);
            }
            case "minmaxCircle": {
                float minRadius = json.get("minRadius").getAsFloat();
                float maxRadius = json.get("maxRadius").getAsFloat();
                int minHeight = json.get("minHeight").getAsInt();
                int maxHeight = minHeight;
                if (json.has("maxHeight")) {
                    maxHeight = json.get("maxHeight").getAsInt();
                }
                return new MinMaxCircleOffset(minRadius, maxRadius, minHeight, maxHeight);
            }
            case "fixed": {
                JsonArray posArray = json.getAsJsonArray("pos");
                return new FixedOffset(posArray.get(0).getAsFloat(), posArray.get(1).getAsFloat(), posArray.get(2).getAsFloat());
            }
        }

        throw new IllegalArgumentException("Invalid Offset Type");
    }
}
