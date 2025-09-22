package org.bigcraft.customitems.activator

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent
import me.wyne.wutils.common.Ticks
import me.wyne.wutils.common.event.RegisterableListener
import me.wyne.wutils.config.configurables.attribute.Attribute
import me.wyne.wutils.config.configurables.attribute.AttributeBase
import me.wyne.wutils.config.configurables.attribute.AttributeFactory
import me.wyne.wutils.config.configurables.attribute.AttributeMap
import me.wyne.wutils.config.configurables.item.attribute.PotionEffectsAttribute
import me.wyne.wutils.i18n.kotlin.placeholderString
import org.bigcraft.customitems.CustomItems
import org.bigcraft.customitems.config.getMillis
import org.bigcraft.customitems.item.CustomItem
import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import kotlin.random.Random

val ACTIVATOR_ATTRIBUTE_MAP = AttributeMap(
    mapOf(
        "cancel" to AttributeFactory { key, config -> CancelEventAttribute(key, config.getBoolean(key)) },
        "commands" to AttributeFactory { key, config -> CommandsAttribute(key, config.getStringList(key)) },
        "level-steal" to AttributeFactory { key, config -> LevelStealAttribute(key, config.getDouble(key)) },
        "entity-potion-effects" to EntityPotionEffectsAttribute.Factory,
        "player-potion-effects" to PlayerPotionEffectsAttribute.Factory,
        "consume" to AttributeFactory { key, config -> ConsumeProjectileAttribute(key, config.getBoolean(key)) },
        "damager-health" to AttributeFactory { key, config -> DamagerHealthAttribute(key, config.getDouble(key)) },
        "drop-lock" to AttributeFactory { key, config -> DropLockAttribute(key, config.getMillis(key)) },
    )
)

abstract class ActivatorAttribute<V>(key: String, value: V) : AttributeBase<V>(key, value) {
    open fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack) = true
    abstract fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack)
}

class CancelEventAttribute(
    key: String,
    value: Boolean
) : ActivatorAttribute<Boolean>(key, value) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack) =
        event is Cancellable

    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        if (!value) return
        if (event is Cancellable)
            event.isCancelled = true
    }
}

class CommandsAttribute(
    key: String,
    commands: List<String>
) : ActivatorAttribute<List<String>>(key, commands) {
    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        value.forEach { Bukkit.dispatchCommand(Bukkit.getConsoleSender(), player.placeholderString(it).get()) }
    }
}

class LevelStealAttribute(
    key: String,
    steal: Double
) : ActivatorAttribute<Double>(key, steal) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack) =
        event is PlayerDeathEvent

    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        event as PlayerDeathEvent
        val killedPlayer = event.entity
        val toSteal = killedPlayer.level * value
        player.level += toSteal.toInt()
    }
}

class EntityPotionEffectsAttribute(
    key: String,
    effects: Set<PotionEffect>
) : ActivatorAttribute<Set<PotionEffect>>(key, effects) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack) =
        event is EntityEvent

    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        event as EntityEvent
        val target = event.entity as? LivingEntity ?: return
        value.forEach { potionEffect ->
            potionEffect.apply(target)
        }
    }

    object Factory : AttributeFactory {
        override fun create(key: String, config: ConfigurationSection): Attribute<*> {
            return EntityPotionEffectsAttribute(
                key,
                PotionEffectsAttribute(key, config).value.map { it.value }.toSet()
            )
        }
    }
}

class PlayerPotionEffectsAttribute(
    key: String,
    effects: Set<PotionEffect>
) : ActivatorAttribute<Set<PotionEffect>>(key, effects) {
    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        value.forEach { potionEffect ->
            potionEffect.apply(player)
        }
    }

    object Factory : AttributeFactory {
        override fun create(key: String, config: ConfigurationSection): Attribute<*> {
            return PlayerPotionEffectsAttribute(
                key,
                PotionEffectsAttribute(key, config).value.map { it.value }.toSet()
            )
        }
    }
}

class ConsumeProjectileAttribute(
    key: String,
    consume: Boolean
) : ActivatorAttribute<Boolean>(key, consume) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack) =
        event is PlayerLaunchProjectileEvent

    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        event as PlayerLaunchProjectileEvent
        event.setShouldConsume(value)
    }
}

class DamagerHealthAttribute(
    key: String,
    health: Double
) : ActivatorAttribute<Double>(key, health) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack) =
        event is EntityDamageByEntityEvent && event.damager is LivingEntity

    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        event as EntityDamageByEntityEvent
        (event.damager as LivingEntity).health = value
    }
}

class DropLockAttribute(
    key: String,
    durationMillis: Long
) : ActivatorAttribute<Long>(key, durationMillis), RegisterableListener {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack) =
        event is PlayerDeathEvent

    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        event as PlayerDeathEvent
        val killedPlayer = event.entity
        val drops = event.drops.toList()
        event.drops.clear()
        val items = drops
            .map { killedPlayer.world.dropItem(killedPlayer.location, it) }
        items.forEach { it.owner = player.uniqueId }
        Bukkit.getScheduler().runTaskLater(CustomItems.instance, Runnable {
            items.forEach { it.owner = null }
        }, Ticks.ofMillis(value))
    }
}

fun Double.test(): Boolean =
    Random.nextDouble() < this