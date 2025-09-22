package org.bigcraft.customitems.activator

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent
import me.wyne.wutils.config.configurables.AttributeConfigurable
import me.wyne.wutils.config.configurables.attribute.AttributeFactory
import me.wyne.wutils.config.configurables.attribute.AttributeMap
import me.wyne.wutils.config.configurables.attribute.ImmutableAttributeContainer
import org.bigcraft.customitems.activator.condition.ACTIVATOR_CONDITION_MAP
import org.bigcraft.customitems.activator.condition.ActivatorCondition
import org.bigcraft.customitems.item.CustomItem
import org.bigcraft.customitems.item.CustomItemManager
import org.bigcraft.customitems.item.RegisterableCustomItemAttribute
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

val ACTIVATOR_MAP = AttributeMap(
    mapOf(
        "right-click" to AttributeFactory { key, config -> RightClickActivator(key, createActivatorAttributes(key, config), createActivatorConditions(key, config)) },
        "player-kill-player" to AttributeFactory { key, config -> PlayerKillActivator(key, createActivatorAttributes(key, config), createActivatorConditions(key, config)) },
        "player-hit-player" to AttributeFactory { key, config -> PlayerHitActivator(key, createActivatorAttributes(key, config), createActivatorConditions(key, config)) },
        "entity-hit-player" to AttributeFactory { key, config -> EntityHitPlayerActivator(key, createActivatorAttributes(key, config), createActivatorConditions(key, config)) },
        "player-hit-entity" to AttributeFactory { key, config -> PlayerHitEntityActivator(key, createActivatorAttributes(key, config), createActivatorConditions(key, config)) },
        "launch-projectile" to AttributeFactory { key, config -> LaunchProjectileActivator(key, createActivatorAttributes(key, config), createActivatorConditions(key, config)) },
        "player-damage" to AttributeFactory { key, config -> PlayerDamageActivator(key, createActivatorAttributes(key, config), createActivatorConditions(key, config)) },
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
    private val conditions: Set<ActivatorCondition<*>>
) : RegisterableCustomItemAttribute<Boolean>(key, true, CustomItemManager.instance.activatorRegistry) {

    fun activate(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        if (conditions.any { !it.isApplicable(event, player, customItem, item) })
            return
        if (customItem.cooldowns.any { it.isCooldowned(player, item) })
            return
        attributes
            .filter { it.isApplicable(event, player, customItem, item) }
            .forEach { it.apply(event, player, customItem, item) }
        customItem.cooldowns.forEach { it.apply(player, item) }
    }

}

class RightClickActivator(
    key: String,
    attributes: Set<ActivatorAttribute<*>>,
    conditions: Set<ActivatorCondition<*>>
) : Activator(key, attributes, conditions) {
    @EventHandler
    private fun onRightClick(event: PlayerInteractEvent) {
        val itemStack = item.getItem(event.player.inventory) ?: return
        if (event.action != Action.RIGHT_CLICK_AIR && event.action != Action.RIGHT_CLICK_BLOCK) return
        activate(event, event.player, item, itemStack)
    }
}

class PlayerKillActivator(
    key: String,
    attributes: Set<ActivatorAttribute<*>>,
    conditions: Set<ActivatorCondition<*>>
) : Activator(key, attributes, conditions) {
    @EventHandler(ignoreCancelled = true)
    private fun onDeath(event: PlayerDeathEvent) {
        val killer = event.entity.killer ?: return
        val itemStack = item.getItem(killer.inventory) ?: return
        activate(event, killer, item, itemStack)
    }
}

class PlayerHitActivator(
    key: String,
    attributes: Set<ActivatorAttribute<*>>,
    conditions: Set<ActivatorCondition<*>>
) : Activator(key, attributes, conditions) {
    @EventHandler(ignoreCancelled = true)
    private fun onDamage(event: EntityDamageByEntityEvent) {
        val damager = event.damager as? Player ?: return
        if (event.entity !is Player) return
        val itemStack = item.getItem(damager.inventory) ?: return
        activate(event, damager, item, itemStack)
    }
}

class EntityHitPlayerActivator(
    key: String,
    attributes: Set<ActivatorAttribute<*>>,
    conditions: Set<ActivatorCondition<*>>
) : Activator(key, attributes, conditions) {
    @EventHandler(ignoreCancelled = true)
    private fun onDamage(event: EntityDamageByEntityEvent) {
        val damaged = event.entity as? Player ?: return
        if (event.damager is Player) return
        val itemStack = item.getItem(damaged.inventory) ?: return
        activate(event, damaged, item, itemStack)
    }
}

class PlayerHitEntityActivator(
    key: String,
    attributes: Set<ActivatorAttribute<*>>,
    conditions: Set<ActivatorCondition<*>>
) : Activator(key, attributes, conditions) {
    @EventHandler(ignoreCancelled = true)
    private fun onDamage(event: EntityDamageByEntityEvent) {
        val damager = event.damager as? Player ?: return
        if (event.entity is Player) return
        val itemStack = item.getItem(damager.inventory) ?: return
        activate(event, damager, item, itemStack)
    }
}

class LaunchProjectileActivator(
    key: String,
    attributes: Set<ActivatorAttribute<*>>,
    conditions: Set<ActivatorCondition<*>>
) : Activator(key, attributes, conditions) {
    @EventHandler(ignoreCancelled = true)
    private fun onLaunch(event: PlayerLaunchProjectileEvent) {
        val itemStack = item.getItem(event.player.inventory) ?: return
        activate(event, event.player, item, itemStack)
    }
}

class PlayerDamageActivator(
    key: String,
    attributes: Set<ActivatorAttribute<*>>,
    conditions: Set<ActivatorCondition<*>>
) : Activator(key, attributes, conditions) {
    @EventHandler(ignoreCancelled = true)
    private fun onDamage(event: EntityDamageEvent) {
        val damaged = event.entity as? Player ?: return
        val itemStack = item.getItem(damaged.inventory) ?: return
        activate(event, damaged, item, itemStack)
    }
}