package org.bigcraft.customitems.api;

import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface CustomBlockRegistry {
    boolean isCustomBlock(@NotNull Block block);
    @Nullable CustomItemApi getBlock(@NotNull Block block);
    void placeBlock(@NotNull Block block, @NotNull CustomItemApi item);
    void breakBlock(@NotNull Block block);
}