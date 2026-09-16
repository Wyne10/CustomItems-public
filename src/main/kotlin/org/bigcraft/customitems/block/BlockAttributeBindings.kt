package org.bigcraft.customitems.block

import org.bigcraft.customitems.CustomItems
import org.bigcraft.customitems.item.CustomItem
import org.bigcraft.customitems.spi.Registries
import org.bukkit.configuration.ConfigurationSection

/** Binds [item] to every block attribute type its `block` section names. */
internal fun bindBlockAttributes(item: CustomItem, section: ConfigurationSection) {
    for (key in section.getKeys(false)) {
        val type = Registries.blockAttributeTypeInstances.get(key)
        if (type == null) {
            CustomItems.logger.warn("Unknown block attribute '{}' on item '{}'", key, item.key)
            continue
        }
        type.bind(item, section)
    }
}
