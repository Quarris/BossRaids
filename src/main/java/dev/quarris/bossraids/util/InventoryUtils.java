package dev.quarris.bossraids.util;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class InventoryUtils {

    public static ItemStack tryInsertItem(IItemHandler inv, ItemStack stack) {
        int size = inv.getSlots();
        for (int slot = 0; slot < size; slot++) {
            stack = inv.insertItem(slot, stack, false);
        }

        return stack;
    }

}
