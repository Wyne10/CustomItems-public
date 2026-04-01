package org.bigcraft.customitems.api;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface CustomItemApi {
    @NotNull String getKey();
    @NotNull ItemStack createItemStack(@Nullable Player player);
    void applyItemStack(@NotNull ItemStack item, @Nullable Player player);
    boolean isCustomItem(@Nullable ItemStack item);
    boolean isCustomItem(@NotNull Block block);
    @Nullable ItemStack getItem(@NotNull Inventory inventory);
    @Nullable ItemStack getItem(@NotNull PlayerInventory inventory);
    @NotNull List<@NotNull ItemStack> getItems(@NotNull Inventory inventory);
}
