package dev.quarris.bossraids.util;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

import java.util.Arrays;

public class ItemRequirement {

    private final Ingredient ingredient;
    private final int count;

    public ItemRequirement(Ingredient ingredient, int count) {
        this.ingredient = ingredient;
        this.count = count;
    }

    public Ingredient getIngredient() {
        return this.ingredient;
    }

    public static Instance deserialize(CompoundNBT nbt) {
        Ingredient ingredient = Ingredient.fromJson(JsonUtils.stringToJson(nbt.getString("Ingredient")));
        Instance inst = new ItemRequirement(ingredient, -1).inst();
        inst.count = nbt.getInt("Count");
        return inst;
    }

    public static Instance readFromBuffer(PacketBuffer buf) {
        Ingredient ingredient = Ingredient.fromNetwork(buf);
        Instance inst = new ItemRequirement(ingredient, -1).inst();
        inst.count = buf.readVarInt();
        return inst;
    }

    public Instance inst() {
        return new Instance();
    }

    @Override
    public String toString() {
        return "ItemRequirement{" +
            "ingredient=" + Arrays.toString(ingredient.getItems()) +
            ", count=" + count +
            '}';
    }

    public class Instance {

        private int count;

        public Instance() {
            this.count = ItemRequirement.this.count;
        }

        public boolean matches(ItemStack item) {
            return ItemRequirement.this.ingredient.test(item);
        }

        public boolean isMet() {
            return this.count == 0;
        }

        public int shrink(int amount) {
            int toShrink = Math.min(amount, this.count);
            this.count -= toShrink;
            return toShrink;
        }

        public String getRequirementDisplay() {
            StringBuilder builder = new StringBuilder();
            ItemStack[] items = ItemRequirement.this.ingredient.getItems();
            builder.append(this.count).append("x ").append(items[0].getDisplayName().getString());
            for (int i = 1; i < items.length; i++) {
                builder.append(" OR ").append(this.count).append("x ").append(items[i].getDisplayName().getString());
            }
            return builder.toString();
        }

        public int getCount() {
            return this.count;
        }

        public CompoundNBT serialize() {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putString("Ingredient", JsonUtils.jsonToString(ItemRequirement.this.ingredient.toJson()));
            nbt.putInt("Count", this.count);
            return nbt;
        }

        public void writeToBuffer(PacketBuffer buf) {
            ItemRequirement.this.ingredient.toNetwork(buf);
            buf.writeVarInt(this.count);
        }

        @Override
        public String toString() {
            return "ItemRequirement$Instance{" +
                "ingredient=" + ItemRequirement.this.ingredient.toJson() +
                "count=" + count +
                '}';
        }

        public ItemStack getLoopingItem(long gameTime) {
            ItemStack[] items = ItemRequirement.this.ingredient.getItems();
            int timedIndex = (int) (gameTime / 20);
            return items[timedIndex % items.length];
        }
    }
}
