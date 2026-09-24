package me.wyne.customitems.api;

import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Tracks which world blocks were placed as CustomItems custom blocks and which custom item
 * each one corresponds to. Obtain the active instance via {@link CustomItemsApi#getBlockRegistry()}.
 */
public interface CustomBlockRegistry {

    /**
     * @return whether {@code block} is tracked as a placed custom block
     */
    boolean isCustomBlock(@NotNull Block block);

    /**
     * @return the custom item {@code block} was placed as, or {@code null} if {@code block} is not tracked
     */
    @Nullable CustomItemApi getBlock(@NotNull Block block);

    /**
     * Registers {@code block}'s location as a placed instance of {@code item}, overwriting any
     * existing registration at that location. Does not modify the block in the world.
     */
    void placeBlock(@NotNull Block block, @NotNull CustomItemApi item);

    /**
     * Removes {@code block}'s location from the registry, if present. Does not modify the block in the world.
     */
    void breakBlock(@NotNull Block block);
}