package dev.quarris.bossraids.util;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;

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

    public Instance inst() {
        return new Instance();
    }

    @Override
    public String toString() {
        return "ItemRequirement{" +
                "ingredient=" + ingredient +
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

        @Override
        public String toString() {
            return "ItemRequirement$Instance{" +
                    "ingredient=" + ItemRequirement.this.ingredient.toJson() +
                    "count=" + count +
                    '}';
        }
    }
}
