package org.bigcraft.customitems.api.spi;

import org.jetbrains.annotations.NotNull;

/**
 * A period during which a custom item will not activate, declared under an item's {@code cooldowns}
 * section and started by the {@code cooldown} activator attribute.
 * <p>
 * Implementations decide what the period is scoped to — the player, the stack, the whole server —
 * and are built per item by a {@link ConfigFactory} registered with
 * {@link CustomItemsRegistries#cooldowns()}.
 * <p>
 * {@link #isActive} must be a pure query. Anything the player sees belongs in a
 * {@link CooldownFeedback}, which CustomItems drives at the points where a cooldown actually starts
 * or actually blocks an activation.
 */
public interface Cooldown extends KeyedExtension {

    boolean isActive(@NotNull CooldownContext context);

    void start(@NotNull CooldownContext context);

    void reset(@NotNull CooldownContext context);

    /** Milliseconds left, or {@code 0} when not active. */
    long getRemainingMillis(@NotNull CooldownContext context);
}
