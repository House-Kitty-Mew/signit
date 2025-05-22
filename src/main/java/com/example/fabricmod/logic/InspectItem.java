package com.example.fabricmod.logic;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;

/**
 * Utility for inspecting an ItemStack’s basic data.
 */
public class InspectItem {
    /**
     * Holds the results of inspect(...)
     */
    public record InspectResult(
            String itemName,
            int count
    ) {
        /**
         * Convert the stored itemName back into an Item instance.
         */
        public net.minecraft.item.Item getItem() {
            Identifier id = Identifier.tryParse(itemName);
            return (id != null)
                    ? Registries.ITEM.get(id)
                    : Registries.ITEM.get(Identifier.tryParse("minecraft:air"));
        }
    }

    /**
     * Inspect the given stack for its registry name and count.
     * Any NBT/components remain on the original ItemStack and will
     * be retained if you do `ItemStack toGive = original.copy();`
     *
     * @param stack the stack to inspect
     * @return an InspectResult record
     */
    public static InspectResult inspect(ItemStack stack) {
        String name = Registries.ITEM.getId(stack.getItem()).toString();
        return new InspectResult(name, stack.getCount());
    }
}
