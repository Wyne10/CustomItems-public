package org.bigcraft.customitems.activator

import me.wyne.wutils.config.configurables.attribute.AttributeBase
import me.wyne.wutils.config.configurables.attribute.AttributeFactory
import me.wyne.wutils.config.configurables.attribute.AttributeMap
import me.wyne.wutils.config.configurables.attribute.GenericAttributeContainer
import org.bigcraft.customitems.event.EventListener
import org.bigcraft.customitems.item.CustomItem
import org.bigcraft.customitems.item.CustomItemAttribute
import org.bigcraft.customitems.item.CustomItemManager
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.player.PlayerInteractEvent

typealias ActivatorAttributes = GenericAttributeContainer<ActivatorAttribute<*>>

val ACTIVATOR_MAP = AttributeMap(
    mutableMapOf(
        "right-click" to AttributeFactory { key, config -> RightClickActivator(key, ActivatorAttributes(ACTIVATOR_ATTRIBUTE_MAP, config.getConfigurationSection(key))) }
    )
)

abstract class Activator<E : Event>(
    key: String,
    attributes: ActivatorAttributes,
    override val event: Class<out E>
) : AttributeBase<ActivatorAttributes>(key, attributes), CustomItemAttribute, EventListener<E> {

    protected lateinit var item: CustomItem
        private set

    override fun apply(item: CustomItem) {
        this.item = item
        ActivatorManager.instance.activatorRegistry.register(this)
    }

    fun activate(event: E, player: Player, item: CustomItem) {
        value.attributes.values.forEach { it.apply(event, player, item) }
    }

}

class RightClickActivator(
    key: String,
    attributes: ActivatorAttributes,
) : Activator<PlayerInteractEvent>(key, attributes, PlayerInteractEvent::class.java) {
    override fun onEvent(event: PlayerInteractEvent) {
        if (!item.isCustomItem(event.item)) return
        CustomItemManager.instance.getItem(event.item)?.let {
            activate(event, event.player, it)
        }
    }
}