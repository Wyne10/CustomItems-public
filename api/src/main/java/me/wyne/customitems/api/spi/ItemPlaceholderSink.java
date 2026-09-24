package me.wyne.customitems.api.spi;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Collects the placeholders one item's name and lore can be written against.
 * <p>
 * The three kinds differ in when and how they are substituted:
 * <ul>
 *   <li>{@link #text} is applied to the raw string <em>before</em> it is interpreted, so its value
 *       can carry markup. Only use it for values you control.</li>
 *   <li>{@link #component} is applied to the already-built component, so a value containing
 *       {@code <} or {@code &} is shown literally. Use it for anything player-derived.</li>
 *   <li>{@link #lines} replaces the whole lore line the placeholder sits on with a run of lines —
 *       the only one that can change how many lines the lore has.</li>
 * </ul>
 * A key is written in YAML as {@code <key>}.
 */
public interface ItemPlaceholderSink {

    void text(@NotNull String key, @NotNull String value);

    /** @param miniMessage the replacement, parsed as MiniMessage */
    void component(@NotNull String key, @NotNull String miniMessage);

    /** @param miniMessage one entry per lore line, each parsed as MiniMessage */
    void lines(@NotNull String key, @NotNull List<@NotNull String> miniMessage);
}
