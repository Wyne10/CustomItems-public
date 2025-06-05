package org.bigcraft.customitems.activator

import me.wyne.wutils.config.configurables.attribute.Attribute
import org.bigcraft.customitems.item.CustomItem
import org.bukkit.entity.Player
import org.bukkit.event.Event

interface ActivatorRestriction<V> : Attribute<V> {
    fun isRestricted(event: Event, player: Player, item: CustomItem): Boolean
}