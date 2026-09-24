package me.wyne.customitems.api.spi;

import me.wyne.customitems.api.CustomItemApi;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Who a cooldown is being asked about: the player, the custom item, and the stack of it in play.
 * <p>
 * A cooldown reads whichever of the three it is scoped to — per player, per item stack, or neither
 * for a server-wide one.
 */
public record CooldownContext(@NotNull Player player, @NotNull CustomItemApi item, @NotNull ItemStack stack) {

    public @NotNull Player getPlayer() {
        return player;
    }

    public @NotNull CustomItemApi getItem() {
        return item;
    }

    public @NotNull ItemStack getStack() {
        return stack;
    }
}
