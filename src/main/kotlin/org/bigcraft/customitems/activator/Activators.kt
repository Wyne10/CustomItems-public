package org.bigcraft.customitems.activator

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent
import me.wyne.wutils.config.configurables.AttributeConfigurable
import me.wyne.wutils.config.configurables.attribute.AttributeFactory
import me.wyne.wutils.config.configurables.attribute.AttributeMap
import me.wyne.wutils.config.configurables.attribute.ImmutableAttributeContainer
import org.bigcraft.customitems.activator.condition.ACTIVATOR_CONDITION_MAP
import org.bigcraft.customitems.activator.condition.ActivatorCondition
import org.bigcraft.customitems.item.CustomItemManager
import org.bigcraft.customitems.item.RegisterableCustomItemAttribute
import org.bigcraft.customitems.trap.CreateTrapEvent
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityPotionEffectEvent
import org.bukkit.event.entity.EntityResurrectEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

val ACTIVATOR_MAP = AttributeMap(
    mapOf(
        "right-click" to AttributeFactory { key, config -> RightClickActivator(key, createItemIdentifier(key, config), createActivatorAttributes(key, config), createActivatorConditions(key, config)) },
        "player-kill-player" to AttributeFactory { key, config -> PlayerKillActivator(key, createItemIdentifier(key, config), createActivatorAttributes(key, config), createActivatorConditions(key, config)) },
        "player-hit-player" to AttributeFactory { key, config -> PlayerHitActivator(key, createItemIdentifier(key, config), createActivatorAttributes(key, config), createActivatorConditions(key, config)) },
        "entity-hit-player" to AttributeFactory { key, config -> EntityHitPlayerActivator(key, createItemIdentifier(key, config), createActivatorAttributes(key, config), createActivatorConditions(key, config)) },
        "projectile-hit-player" to AttributeFactory { key, config -> ProjectileHitPlayerActivator(key, createItemIdentifier(key, config), createActivatorAttributes(key, config), createActivatorConditions(key, config)) },
        "player-hit-entity" to AttributeFactory { key, config -> PlayerHitEntityActivator(key, createItemIdentifier(key, config), createActivatorAttributes(key, config), createActivatorConditions(key, config)) },
        "player-launch-projectile" to AttributeFactory { key, config -> LaunchProjectileActivator(key, createItemIdentifier(key, config), createActivatorAttributes(key, config), createActivatorConditions(key, config)) },
        "player-take-damage" to AttributeFactory { key, config -> PlayerDamageActivator(key, createItemIdentifier(key, config), createActivatorAttributes(key, config), createActivatorConditions(key, config)) },
        "player-potion-effect" to AttributeFactory { key, config -> PlayerPotionEffectActivator(key, createItemIdentifier(key, config), createActivatorAttributes(key, config), createActivatorConditions(key, config)) },
        "player-consume-item" to AttributeFactory { key, config -> PlayerConsumeItemActivator(key, createItemIdentifier(key, config), createActivatorAttributes(key, config), createActivatorConditions(key, config)) },
        "player-block-break" to AttributeFactory { key, config -> BlockBreakActivator(key, createItemIdentifier(key, config), createActivatorAttributes(key, config), createActivatorConditions(key, config)) },
        "player-block-place" to AttributeFactory { key, config -> BlockPlaceActivator(key, createItemIdentifier(key, config), createActivatorAttributes(key, config), createActivatorConditions(key, config)) },
        "player-resurrect" to AttributeFactory { key, config -> PlayerResurrectActivator(key, createItemIdentifier(key, config), createActivatorAttributes(key, config), createActivatorConditions(key, config)) },
        "player-move" to AttributeFactory { key, config -> PlayerMoveActivator(key, createItemIdentifier(key, config), createActivatorAttributes(key, config), createActivatorConditions(key, config)) },
        "player-create-trap" to AttributeFactory { key, config -> CreateTrapActivator(key, createItemIdentifier(key, config), createActivatorAttributes(key, config), createActivatorConditions(key, config)) },
    )
).cheese(1)

private fun AttributeMap.cheese(amount: Int): AttributeMap {
    for (i in 1..amount) {
        keyMap.toMap().forEach { (key, factory) -> put("$key-$i", factory) }
    }
    return this
}

private fun createItemIdentifier(key: String, config: ConfigurationSection) =
    runCatching { ItemIdentifiers.valueOf(config.getString("$key.identifier", "PLAYER")!!).identifier }
        .getOrDefault(ItemIdentifiers.PLAYER.identifier)

private fun createActivatorAttributes(key: String, config: ConfigurationSection) =
    AttributeConfigurable(ImmutableAttributeContainer(ACTIVATOR_ATTRIBUTE_MAP, config.getConfigurationSection(key)))
        .getSet(ActivatorAttribute::class.java)

private fun createActivatorConditions(key: String, config: ConfigurationSection) =
    AttributeConfigurable(ImmutableAttributeContainer(ACTIVATOR_CONDITION_MAP, config.getConfigurationSection(key)))
        .getSet(ActivatorCondition::class.java)

abstract class Activator(
    key: String,
    private val itemIdentifier: ItemIdentifier,
    private val attributes: Set<ActivatorAttribute<*>>,
    private val conditions: Set<ActivatorCondition<*>>
) : RegisterableCustomItemAttribute<Boolean>(key, true, CustomItemManager.instance.activatorRegistry) {

    protected fun getItemStack(event: Event, player: Player): ItemStack? =
        itemIdentifier.getItem(event, player, item)

    fun activate(event: Event, player: Player, itemStack: ItemStack) {
        if (conditions.any { !it.isApplicable(event, player, item, itemStack) }) {
            attributes.firstOrNull { it.key == "condition-cancel-event" }?.apply(event, player, item, itemStack)
            return
        }
        attributes
            .filter { it.key != "condition-cancel-event" }
            .filter { it.isApplicable(event, player, item, itemStack) }
            .forEach { it.apply(event, player, item, itemStack) }
        item.cooldowns.forEach { it.apply(player, itemStack) }
    }

}

class RightClickActivator(
    key: String,
    itemIdentifier: ItemIdentifier,
    attributes: Set<ActivatorAttribute<*>>,
    conditions: Set<ActivatorCondition<*>>
) : Activator(key, itemIdentifier, attributes, conditions) {
    @EventHandler(priority = EventPriority.HIGH)
    private fun onRightClick(event: PlayerInteractEvent) {
        val itemStack = getItemStack(event, event.player) ?: return
        if (event.action != Action.RIGHT_CLICK_AIR && event.action != Action.RIGHT_CLICK_BLOCK) return
        if (event.hand != EquipmentSlot.HAND) return
        activate(event, event.player, itemStack)
    }
}

class PlayerKillActivator(
    key: String,
    itemIdentifier: ItemIdentifier,
    attributes: Set<ActivatorAttribute<*>>,
    conditions: Set<ActivatorCondition<*>>
) : Activator(key, itemIdentifier, attributes, conditions) {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private fun onDeath(event: PlayerDeathEvent) {
        val killer = event.entity.killer ?: return
        val itemStack = getItemStack(event, killer) ?: return
        activate(event, killer, itemStack)
    }
}

class PlayerHitActivator(
    key: String,
    itemIdentifier: ItemIdentifier,
    attributes: Set<ActivatorAttribute<*>>,
    conditions: Set<ActivatorCondition<*>>
) : Activator(key, itemIdentifier, attributes, conditions) {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private fun onDamage(event: EntityDamageByEntityEvent) {
        val damager = event.damager as? Player ?: return
        if (event.entity !is Player) return
        val itemStack = getItemStack(event, damager) ?: return
        activate(event, damager, itemStack)
    }
}

class EntityHitPlayerActivator(
    key: String,
    itemIdentifier: ItemIdentifier,
    attributes: Set<ActivatorAttribute<*>>,
    conditions: Set<ActivatorCondition<*>>
) : Activator(key, itemIdentifier, attributes, conditions) {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private fun onDamage(event: EntityDamageByEntityEvent) {
        val damaged = event.entity as? Player ?: return
        if (event.damager is Player) return
        val itemStack = getItemStack(event, damaged) ?: return
        activate(event, damaged, itemStack)
    }
}

class ProjectileHitPlayerActivator(
    key: String,
    itemIdentifier: ItemIdentifier,
    attributes: Set<ActivatorAttribute<*>>,
    conditions: Set<ActivatorCondition<*>>
) : Activator(key, itemIdentifier, attributes, conditions) {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private fun onDamage(event: EntityDamageByEntityEvent) {
        val damaged = event.entity as? Player ?: return
        if (event.damager !is Projectile) return
        val itemStack = getItemStack(event, damaged) ?: return
        activate(event, damaged, itemStack)
    }
}

class PlayerHitEntityActivator(
    key: String,
    itemIdentifier: ItemIdentifier,
    attributes: Set<ActivatorAttribute<*>>,
    conditions: Set<ActivatorCondition<*>>
) : Activator(key, itemIdentifier, attributes, conditions) {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private fun onDamage(event: EntityDamageByEntityEvent) {
        val damager = event.damager as? Player ?: return
        if (event.entity is Player) return
        val itemStack = getItemStack(event, damager) ?: return
        activate(event, damager, itemStack)
    }
}

class LaunchProjectileActivator(
    key: String,
    itemIdentifier: ItemIdentifier,
    attributes: Set<ActivatorAttribute<*>>,
    conditions: Set<ActivatorCondition<*>>
) : Activator(key, itemIdentifier, attributes, conditions) {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private fun onLaunch(event: PlayerLaunchProjectileEvent) {
        val itemStack = getItemStack(event, event.player) ?: return
        activate(event, event.player, itemStack)
    }
}

class PlayerDamageActivator(
    key: String,
    itemIdentifier: ItemIdentifier,
    attributes: Set<ActivatorAttribute<*>>,
    conditions: Set<ActivatorCondition<*>>
) : Activator(key, itemIdentifier, attributes, conditions) {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private fun onDamage(event: EntityDamageEvent) {
        val damaged = event.entity as? Player ?: return
        val itemStack = getItemStack(event, damaged) ?: return
        activate(event, damaged, itemStack)
    }
}

class PlayerPotionEffectActivator(
    key: String,
    itemIdentifier: ItemIdentifier,
    attributes: Set<ActivatorAttribute<*>>,
    conditions: Set<ActivatorCondition<*>>
) : Activator(key, itemIdentifier, attributes, conditions) {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private fun onPotion(event: EntityPotionEffectEvent) {
        val player = event.entity as? Player ?: return
        val itemStack = getItemStack(event, player) ?: return
        activate(event, player, itemStack)
    }
}

class PlayerConsumeItemActivator(
    key: String,
    itemIdentifier: ItemIdentifier,
    attributes: Set<ActivatorAttribute<*>>,
    conditions: Set<ActivatorCondition<*>>
) : Activator(key, itemIdentifier, attributes, conditions) {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private fun onConsume(event: PlayerItemConsumeEvent) {
        val itemStack = getItemStack(event, event.player) ?: return
        activate(event, event.player, itemStack)
    }
}

class BlockBreakActivator(
    key: String,
    itemIdentifier: ItemIdentifier,
    attributes: Set<ActivatorAttribute<*>>,
    conditions: Set<ActivatorCondition<*>>
) : Activator(key, itemIdentifier, attributes, conditions) {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private fun onBreak(event: BlockBreakEvent) {
        val itemStack = getItemStack(event, event.player) ?: return
        activate(event, event.player, itemStack)
    }
}

class BlockPlaceActivator(
    key: String,
    itemIdentifier: ItemIdentifier,
    attributes: Set<ActivatorAttribute<*>>,
    conditions: Set<ActivatorCondition<*>>
) : Activator(key, itemIdentifier, attributes, conditions) {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private fun onPlace(event: BlockPlaceEvent) {
        val itemStack = getItemStack(event, event.player) ?: return
        activate(event, event.player, itemStack)
    }
}

class PlayerResurrectActivator(
    key: String,
    itemIdentifier: ItemIdentifier,
    attributes: Set<ActivatorAttribute<*>>,
    conditions: Set<ActivatorCondition<*>>
) : Activator(key, itemIdentifier, attributes, conditions) {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private fun onResurrect(event: EntityResurrectEvent) {
        val player = event.entity as? Player ?: return
        val itemStack = getItemStack(event, player) ?: return
        activate(event, player, itemStack)
    }
}

class PlayerMoveActivator(
    key: String,
    itemIdentifier: ItemIdentifier,
    attributes: Set<ActivatorAttribute<*>>,
    conditions: Set<ActivatorCondition<*>>
) : Activator(key, itemIdentifier, attributes, conditions) {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private fun onMove(event: PlayerMoveEvent) {
        if (!event.hasChangedPosition()) return
        val itemStack = getItemStack(event, event.player) ?: return
        activate(event, event.player, itemStack)
    }
}

class CreateTrapActivator(
    key: String,
    itemIdentifier: ItemIdentifier,
    attributes: Set<ActivatorAttribute<*>>,
    conditions: Set<ActivatorCondition<*>>
) : Activator(key, itemIdentifier, attributes, conditions) {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private fun onTrapCreation(event: CreateTrapEvent) {
        val itemStack = getItemStack(event, event.player) ?: return
        activate(event, event.player, itemStack)
    }
}