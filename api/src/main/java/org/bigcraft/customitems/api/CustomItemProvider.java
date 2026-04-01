package org.bigcraft.customitems.api;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface CustomItemProvider {
    @NotNull Set<@NotNull String> getKeys();
    @NotNull Set<@NotNull String> getPaths();
    boolean contains(@NotNull String key);
    @Nullable CustomItemApi getItem(@NotNull String key);
    @Nullable CustomItemApi getItem(@Nullable ItemStack item);
}
