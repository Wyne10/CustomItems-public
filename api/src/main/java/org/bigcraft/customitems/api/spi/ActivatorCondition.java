package org.bigcraft.customitems.api.spi;

import org.bigcraft.customitems.api.CustomItemApi;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * A gate on an activator: every condition declared alongside an activator must pass before any of
 * its attributes run.
 * <p>
 * Conditions are declared as flat keys inside an activator's section, sharing that section with the
 * activator's attributes — the two vocabularies must not overlap.
 */
public interface ActivatorCondition {

    /**
     * @param event the event that triggered the activator
     * @param player the player the activator resolved to
     * @param item the custom item being activated
     * @param stack the stack of {@code item} the activator resolved to
     */
    boolean test(@NotNull Event event, @NotNull Player player, @NotNull CustomItemApi item, @NotNull ItemStack stack);
}
