package me.wyne.customitems.block

import me.wyne.customitems.CustomItems
import me.wyne.customitems.item.CustomItem
import me.wyne.customitems.spi.Registries
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
