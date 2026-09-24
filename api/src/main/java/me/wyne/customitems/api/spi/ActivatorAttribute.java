package me.wyne.customitems.api.spi;

import me.wyne.customitems.api.CustomItemApi;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * An effect run when an activator fires and all of its {@link ActivatorCondition}s pass.
 * <p>
 * Attributes run in the order their keys were registered, not the order they appear in YAML.
 */
public interface ActivatorAttribute {

    /**
     * Whether this attribute applies to {@code event} at all — typically an event-type check.
     * An attribute that does not apply is skipped without failing the activation.
     */
    default boolean isApplicable(@NotNull Event event, @NotNull Player player, @NotNull CustomItemApi item, @NotNull ItemStack stack) {
        return true;
    }

    void apply(@NotNull Event event, @NotNull Player player, @NotNull CustomItemApi item, @NotNull ItemStack stack);
}
