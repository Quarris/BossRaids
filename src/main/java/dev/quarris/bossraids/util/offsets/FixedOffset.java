package dev.quarris.bossraids.util.offsets;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundNBT;
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

    @Override
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", "fixed");
        JsonArray pos = new JsonArray();
        pos.add(this.x);
        pos.add(this.y);
        pos.add(this.z);
        json.add("pos", pos);
        return json;
    }
}
