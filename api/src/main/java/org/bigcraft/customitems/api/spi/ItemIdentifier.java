package org.bigcraft.customitems.api.spi;

import org.bigcraft.customitems.api.CustomItemApi;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Decides which stack of a custom item an activator acts on, selected per activator with the
 * {@code identifier} key. Returning {@code null} aborts the activation.
 */
public interface ItemIdentifier extends KeyedExtension {

    @Nullable ItemStack getItem(@NotNull Player player, @NotNull CustomItemApi item);
}
