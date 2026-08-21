package org.bigcraft.customitems.api;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Registry of all custom items loaded by the CustomItems plugin. Obtain the active instance
 * via {@link CustomItemsApi#getProvider()} or Bukkit's services manager.
 */
public interface CustomItemProvider {

    /**
     * @return the keys of every registered custom item
     */
    @NotNull Set<@NotNull String> getKeys();

    /**
     * @return every registered custom item's key, qualified with its config subdirectory path
     */
    @NotNull Set<@NotNull String> getPaths();

    /**
     * @return whether a custom item is registered under {@code key}
     */
    boolean contains(@NotNull String key);

    /**
     * @return the custom item registered under {@code key}, or {@code null} if none is registered
     */
    @Nullable CustomItemApi getItem(@NotNull String key);

    /**
     * Resolves the custom item that produced {@code item}, based on the persistent data tag
     * written by {@link CustomItemApi#createItemStack} / {@link CustomItemApi#applyItemStack}.
     *
     * @return the matching custom item, or {@code null} if {@code item} is {@code null} or is not a custom item
     */
    @Nullable CustomItemApi getItem(@Nullable ItemStack item);
}
