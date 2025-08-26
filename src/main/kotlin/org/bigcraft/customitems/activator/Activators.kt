package org.bigcraft.customitems.activator

import me.wyne.wutils.common.event.RegisterableListener
import me.wyne.wutils.config.configurables.AttributeConfigurable
import me.wyne.wutils.config.configurables.attribute.AttributeBase
import me.wyne.wutils.config.configurables.attribute.AttributeFactory
import me.wyne.wutils.config.configurables.attribute.AttributeMap
import me.wyne.wutils.config.configurables.attribute.ImmutableAttributeContainer
import org.bigcraft.customitems.activator.condition.ACTIVATOR_CONDITION_MAP
import org.bigcraft.customitems.activator.condition.ActivatorCondition
import org.bigcraft.customitems.item.CustomItem
import org.bigcraft.customitems.item.CustomItemAttribute
import org.bigcraft.customitems.item.CustomItemManager
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

val ACTIVATOR_MAP = AttributeMap(
    mutableMapOf(
        "right-click" to AttributeFactory { key, config -> RightClickActivator(key, createActivatorAttributes(key, config), createActivatorConditions(key, config)) },
    )
)

private fun createActivatorAttributes(key: String, config: ConfigurationSection) =
    AttributeConfigurable(ImmutableAttributeContainer(ACTIVATOR_ATTRIBUTE_MAP, config.getConfigurationSection(key)))
        .getSet(ActivatorAttribute::class.java)

private fun createActivatorConditions(key: String, config: ConfigurationSection) =
    AttributeConfigurable(ImmutableAttributeContainer(ACTIVATOR_CONDITION_MAP, config.getConfigurationSection(key)))
        .getSet(ActivatorCondition::class.java)

abstract class Activator(
    key: String,
    private val attributes: Set<ActivatorAttribute<*>>,
    private val conditions: Set<ActivatorCondition<*>>,
) : AttributeBase<Boolean>(key, true), CustomItemAttribute, RegisterableListener {

    protected lateinit var item: CustomItem
        private set

    override fun apply(item: CustomItem) {
        this.item = item
        CustomItemManager.instance.activatorRegistry.register(this)
    }

    fun activate(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        if (conditions
                .any { !it.isApplicable(event, player, customItem, item) })
            return
        attributes
            .filter { it.isApplicable(event, player, customItem, item) }
            .forEach { it.apply(event, player, customItem, item) }
    }

}

class RightClickActivator(
    key: String,
    attributes: Set<ActivatorAttribute<*>>,
    conditions: Set<ActivatorCondition<*>>,
) : Activator(key, attributes, conditions) {
    @EventHandler
    private fun onEvent(event: PlayerInteractEvent) {
        if (!item.isCustomItem(event.item)) return
        if (event.action != Action.RIGHT_CLICK_AIR && event.action != Action.RIGHT_CLICK_BLOCK) return
        CustomItemManager.instance.getItem(event.item)?.let {
            activate(event, event.player, it, event.item!!)
        }
    }
}