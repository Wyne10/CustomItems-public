package org.bigcraft.customitems.api.spi;

import org.jetbrains.annotations.NotNull;

/**
 * An extension that carries the config key it answers to, so it can be registered as a single
 * shared instance rather than through a {@link ConfigFactory}.
 */
public interface KeyedExtension {
    @NotNull String getKey();
}
