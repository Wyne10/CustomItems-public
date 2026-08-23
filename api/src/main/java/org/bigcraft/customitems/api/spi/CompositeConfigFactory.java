package org.bigcraft.customitems.api.spi;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link ConfigFactory} that dispatches on the shape of the YAML value at {@code key}: a nested
 * section goes to {@link #fromSection}, a scalar string goes to {@link #fromString}, and anything
 * else treats the <em>enclosing</em> section as the body.
 * <p>
 * That last branch is what lets an {@code attributeType} alias name its fields directly instead of
 * repeating the alias name.
 *
 * @param <T> the type this factory builds
 */
public interface CompositeConfigFactory<T> extends ConfigFactory<T> {

    @Override
    default @NotNull T create(@NotNull String key, @NotNull ConfigurationSection config) {
        if (config.isConfigurationSection(key))
            return fromSection(key, config.getConfigurationSection(key));
        if (config.isString(key))
            return fromString(key, config.getString(key), config);
        return fromSection(key, config);
    }

    @NotNull T fromSection(@NotNull String key, @NotNull ConfigurationSection section);

    @NotNull T fromString(@NotNull String key, @NotNull String string, @NotNull ConfigurationSection config);
}
