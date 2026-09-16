package org.bigcraft.customitems.api.spi;

import org.jetbrains.annotations.NotNull;

/**
 * What the player sees when a cooldown starts or turns an activation away. Declared as extra keys
 * inside a cooldown's own section, so it applies to any {@link Cooldown} including registered ones.
 * <p>
 * Keeping this separate from {@link Cooldown} is what lets {@link Cooldown#isActive} be asked freely
 * — by a condition, by a placeholder, by another plugin — without a message being sent each time.
 */
public interface CooldownFeedback {

    /** Called once, when {@code cooldown} is started. */
    default void onStarted(@NotNull CooldownContext context, @NotNull Cooldown cooldown) {
    }

    /** Called once, when an activation was turned away because {@code cooldown} is active. */
    default void onBlocked(@NotNull CooldownContext context, @NotNull Cooldown cooldown) {
    }
}
