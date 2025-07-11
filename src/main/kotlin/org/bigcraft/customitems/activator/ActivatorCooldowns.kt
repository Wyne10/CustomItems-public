package org.bigcraft.customitems.activator

import me.wyne.wutils.config.configurables.attribute.AttributeBase
import me.wyne.wutils.config.configurables.attribute.GenericAttributeContainer
import org.bigcraft.customitems.item.CustomItem
import org.bukkit.entity.Player
import org.bukkit.event.Event

typealias ActivatorAttributes = GenericAttributeContainer<ActivatorAttribute<*>>

abstract class ActivatorCooldown<V>(key: String, value: V) : AttributeBase<V>(key, value) {
    abstract fun apply(event: Event, player: Player, item: CustomItem)
}