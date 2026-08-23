package org.bigcraft.customitems.api.spi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * A vocabulary of shared, stateless extensions keyed by {@link KeyedExtension#getKey()}. Unlike a
 * {@link FactoryRegistry} nothing is built per item — the same instance serves every item that
 * names it.
 *
 * @param <T> the extension type
 */
public interface InstanceRegistry<T extends KeyedExtension> {

    void register(@NotNull T extension);

    @Nullable T get(@NotNull String key);

    boolean contains(@NotNull String key);

    @NotNull Set<@NotNull String> keys();
}
