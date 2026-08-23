package org.bigcraft.customitems.api.spi;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * A vocabulary of config keys, each backed by a {@link ConfigFactory} that builds one {@code T}
 * per occurrence in an item's YAML.
 * <p>
 * Registering a key that is already taken replaces it. Registrations must land before CustomItems
 * loads its items — see {@link CustomItemsRegistries}.
 *
 * @param <T> the type this vocabulary builds
 */
public interface FactoryRegistry<T> {

    void register(@NotNull String key, @NotNull ConfigFactory<? extends T> factory);

    boolean contains(@NotNull String key);

    @NotNull Set<@NotNull String> keys();
}
