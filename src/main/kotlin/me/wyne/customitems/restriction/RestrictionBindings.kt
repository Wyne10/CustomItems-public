package me.wyne.customitems.restriction

import me.wyne.customitems.CustomItems
import me.wyne.customitems.item.CustomItem
import me.wyne.customitems.spi.Registries
import org.bukkit.configuration.ConfigurationSection

/** Binds [item] to every restriction type its `restrictions` section names. */
internal fun bindRestrictions(item: CustomItem, section: ConfigurationSection) {
    for (key in section.getKeys(false)) {
        val type = Registries.restrictionTypeInstances.get(key)
        if (type == null) {
            CustomItems.logger.warn("Unknown restriction '{}' on item '{}'", key, item.key)
            continue
        }
        type.bind(item, section)
    }
}
