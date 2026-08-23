package org.bigcraft.customitems.api.spi;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

/**
 * Builds a {@code T} from a config key and the section it lives in.
 * <p>
 * Note what is received: the key, and the <em>enclosing</em> section — not the value at the key.
 * An implementation may read {@code config.getString(key)}, {@code config.getConfigurationSection(key)},
 * or ignore the key and read sibling keys instead. Use {@link CompositeConfigFactory} to accept
 * both a bare scalar at {@code key} and a nested section body.
 *
 * @param <T> the type this factory builds
 */
@FunctionalInterface
public interface ConfigFactory<T> {
    @NotNull T create(@NotNull String key, @NotNull ConfigurationSection config);
}
