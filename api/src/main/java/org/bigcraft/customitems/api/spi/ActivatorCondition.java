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
     * Whether this condition has anything to say about {@code event} — typically an event-type
     * check, for a condition that only makes sense for some of the events an item is bound to.
     * A condition that does not apply is skipped rather than failed.
     * <p>
     * Keeping this separate from {@link #test} is what makes {@link #inverted} safe: inverting a
     * condition must flip its verdict without flipping whether it had one.
     */
    default boolean appliesTo(@NotNull Event event) {
        return true;
    }

    /**
     * @param event the event that triggered the activator
     * @param player the player the activator resolved to
     * @param item the custom item being activated
     * @param stack the stack of {@code item} the activator resolved to
     */
    boolean test(@NotNull Event event, @NotNull Player player, @NotNull CustomItemApi item, @NotNull ItemStack stack);

    /**
     * This condition with its verdict flipped, for registering a {@code not-} variant of a key
     * without writing the opposite condition by hand.
     */
    default @NotNull ActivatorCondition inverted() {
        ActivatorCondition delegate = this;
        return new ActivatorCondition() {
            @Override
            public boolean appliesTo(@NotNull Event event) {
                return delegate.appliesTo(event);
            }

            @Override
            public boolean test(@NotNull Event event, @NotNull Player player, @NotNull CustomItemApi item, @NotNull ItemStack stack) {
                return !delegate.test(event, player, item, stack);
            }
        };
    }
}
