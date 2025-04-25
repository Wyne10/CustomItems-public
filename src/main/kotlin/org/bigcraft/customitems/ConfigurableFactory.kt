package org.bigcraft.customitems

import org.bukkit.configuration.ConfigurationSection

interface ConfigurableFactory<out T> {
    fun fromConfig(config: ConfigurationSection): T
}