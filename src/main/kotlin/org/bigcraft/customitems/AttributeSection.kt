package org.bigcraft.customitems

import org.bukkit.configuration.ConfigurationSection

/**
 * Resolves an attribute's own configuration section regardless of how it was declared.
 *
 * A plain [me.wyne.wutils.config.configurables.attribute.AttributeFactory] receives `(key, config)`.
 * Normally [this] is the parent section and the attribute lives under [key]. When the attribute is
 * declared via `attributeType`, WUtils' `AttributeMap` hands the factory the attribute's own section
 * directly (its name equals [key]). This helper returns the right section in both cases so a factory
 * can read its fields relative to it.
 */
fun ConfigurationSection.attributeSection(key: String): ConfigurationSection? =
    if (name == key) this else getConfigurationSection(key)
