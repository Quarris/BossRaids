package dev.quarris.bossraids.raid.definitions;

import dev.quarris.bossraids.ModRef;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class AttributeDefinition {

    public final Attribute attribute;
    public final double level;
    public final Operation operation;

    public AttributeDefinition(Attribute attribute, float level, Operation operation) {
        this.attribute = attribute;
        this.level = level;
        this.operation = operation;
    }

    public AttributeDefinition(CompoundNBT tag) {
        this.attribute = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(tag.getString("Attribute")));
        this.level = tag.getDouble("Level");
        this.operation = Operation.fromId(tag.getString("Operation"));
    }

    public CompoundNBT serialize() {
        CompoundNBT tag = new CompoundNBT();
        tag.putString("Attribute", this.attribute.getRegistryName().toString());
        tag.putDouble("Level", this.level);
        tag.putString("Operation", this.operation.id);
        return tag;
    }

    public enum Operation {
        BASE("base"),
        ADD("add", () -> AttributeModifier.Operation.ADDITION),
        MULTIPLY_TOTAL("multiply_total", () -> AttributeModifier.Operation.MULTIPLY_TOTAL),
        MULTIPLY_BASE("multiply_base", () -> AttributeModifier.Operation.MULTIPLY_BASE);

        private String id;
        private Supplier<AttributeModifier.Operation> attributeOp;

        Operation(String id) {
            this.id = id;
            this.attributeOp = () -> null;
        }

        Operation(String id, Supplier<AttributeModifier.Operation> attributeOp) {
            this.id = id;
            this.attributeOp = attributeOp;
        }

        public AttributeModifier.Operation getOp() {
            return this.attributeOp.get();
        }

        public static Operation fromId(String id) {
            for (Operation op : Operation.values()) {
                if (op.id.equals(id)) {
                    return op;
                }
            }

            ModRef.LOGGER.error("Invalid Operation: {}", id);
            return null;
        }
    }
}
