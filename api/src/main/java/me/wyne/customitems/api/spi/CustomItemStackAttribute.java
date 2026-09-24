package me.wyne.customitems.api.spi;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A step in building a custom item's stack, applied on top of the attributes CustomItems and WUtils
 * already provide (material, name, lore, enchantments and so on).
 */
public interface CustomItemStackAttribute {

    /**
     * @param player context for player-dependent values; {@code null} when the stack is built
     *               outside any player context
     */
    void apply(@NotNull ItemStack item, @Nullable Player player);
}
