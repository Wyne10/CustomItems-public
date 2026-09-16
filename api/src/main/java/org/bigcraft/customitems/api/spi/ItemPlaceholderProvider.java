package org.bigcraft.customitems.api.spi;

import org.bigcraft.customitems.api.CustomItemApi;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Supplies placeholders for an item's name and lore from the state of an actual stack — its
 * durability, what it has been composed with, how long its cooldowns have left.
 * <p>
 * Called every time a stack is built or refreshed, so keep it cheap and side-effect free. Register
 * with {@link CustomItemsRegistries#registerItemPlaceholders}.
 *
 * <pre>{@code
 * registries.registerItemPlaceholders((sink, item, stack, player) ->
 *         sink.text("charges", String.valueOf(readCharges(stack))));
 * }</pre>
 */
@FunctionalInterface
public interface ItemPlaceholderProvider {

    /**
     * @param player who the stack is being shown to, or {@code null} when it is built outside any
     *               player context
     */
    void contribute(
            @NotNull ItemPlaceholderSink sink,
            @NotNull CustomItemApi item,
            @NotNull ItemStack stack,
            @Nullable Player player
    );
}
