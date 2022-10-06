package dev.quarris.bossraids.raid.definitions;

import dev.quarris.bossraids.ModRef;
import dev.quarris.bossraids.util.JsonUtils;
import dev.quarris.bossraids.util.NbtUtils;
import dev.quarris.bossraids.util.offsets.IOffset;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.RangedInteger;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;

public class MinionEntityDefinition extends AbstractEntityDefinition<Entity> {

    private RangedInteger count = new RangedInteger(1, 1);
    private RangedInteger cooldown = new RangedInteger(20, 100);
    private IOffset offset;

    public MinionEntityDefinition() {

    }

    public MinionEntityDefinition(CompoundNBT tag) {
        super(tag);
        this.count = NbtUtils.readRangedInt(tag.getCompound("Count"));
        this.cooldown = NbtUtils.readRangedInt(tag.getCompound("Cooldown"));
        if (tag.contains("Offset", Constants.NBT.TAG_STRING)) {
            this.offset = IOffset.fromJson(JsonUtils.stringToJson(tag.getString("Offset")).getAsJsonObject());
        }
    }

    @Override
    protected void editEntity(ServerWorld level, Vector3d pos, Entity entity) {
        if (this.offset != null) {
            Vector3d offPos = this.offset.getOffset(pos);
            if (World.isOutsideBuildHeight(MathHelper.floor(offPos.y))) {
                int height = level.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, MathHelper.floor(offPos.x), MathHelper.floor(offPos.z));
                offPos = new Vector3d(offPos.x, height, offPos.z);
            }
            entity.setPos(offPos.x, offPos.y, offPos.z);
        }
    }

    public int cooldown() {
        return this.cooldown.randomValue(ModRef.RANDOM);
    }

    public int amountToSpawn(int current) {
        return this.count.getMinInclusive() + ModRef.RANDOM.nextInt(this.count.getMaxInclusive() - this.count.getMinInclusive() + 1) - current;
    }

    @Override
    public CompoundNBT serialize() {
        CompoundNBT tag = super.serialize();
        tag.put("Count", NbtUtils.writeRangedInt(this.count));
        tag.put("Cooldown", NbtUtils.writeRangedInt(this.cooldown));
        if (this.offset != null) {
            tag.putString("Offset", this.offset.toJson().toString());
        }
        return tag;
    }

    @Override
    public String toString() {
        return "MinionEntityDefinition{" +
            "entity='" + entity + '\'' +
            ", count=" + count +
            '}';
    }
}
