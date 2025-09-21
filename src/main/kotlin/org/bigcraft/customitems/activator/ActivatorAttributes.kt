package org.bigcraft.customitems.activator

import me.wyne.wutils.config.configurables.attribute.Attribute
import me.wyne.wutils.config.configurables.attribute.AttributeBase
import me.wyne.wutils.config.configurables.attribute.AttributeFactory
import me.wyne.wutils.config.configurables.attribute.AttributeMap
import me.wyne.wutils.config.configurables.item.attribute.PotionEffectsAttribute
import me.wyne.wutils.i18n.kotlin.placeholderString
import org.bigcraft.customitems.item.CustomItem
import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
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
        "potion-effects" to org.bigcraft.customitems.activator.PotionEffectsAttribute.Factory,
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

class PotionEffectsAttribute(
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
            return PotionEffectsAttribute(
                key,
                PotionEffectsAttribute(key, config).value.map { it.value }.toSet()
            )
        }
    }
}

fun Double.test(): Boolean =
    Random.nextDouble() < this