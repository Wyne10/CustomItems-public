package org.bigcraft.customitems.activator

import me.wyne.wutils.common.Args
import me.wyne.wutils.config.configurables.attribute.Attribute
import me.wyne.wutils.config.configurables.attribute.AttributeBase
import me.wyne.wutils.config.configurables.attribute.AttributeFactory
import me.wyne.wutils.config.configurables.attribute.AttributeMap
import me.wyne.wutils.config.configurables.attribute.CompositeAttributeFactory
import me.wyne.wutils.i18n.kotlin.placeholderString
import org.bigcraft.customitems.item.CustomItem
import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.inventory.ItemStack
import kotlin.random.Random

val ACTIVATOR_ATTRIBUTE_MAP = AttributeMap(
    mapOf(
        "cancel" to AttributeFactory { key, config -> CancelEventAttribute(key, config.getBoolean(key)) },
        "commands" to AttributeFactory { key, config -> CommandsAttribute(key, config.getStringList(key)) },
        "level-steal" to LevelSteal.Factory
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

class LevelSteal(
    key: String,
    chance: Double,
    steal: Double
) : ActivatorAttribute<Pair<Double, Double>>(key, chance to steal) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack) =
        event is PlayerDeathEvent

    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        if (!value.first.test()) return
        event as PlayerDeathEvent
        val killedPlayer = event.entity
        val toSteal = killedPlayer.level * value.second
        player.level += toSteal.toInt()
    }

    object Factory : CompositeAttributeFactory {
        override fun fromSection(key: String, section: ConfigurationSection): Attribute<*> =
            LevelSteal(
                key,
                section.getDouble("chance"),
                section.getDouble("steal")
            )

        override fun fromString(key: String, string: String): Attribute<*> {
            val args = Args(string, ":")
            return LevelSteal(
                key,
                args.get(0, "0.5").toDouble(),
                args.get(1, "0.5").toDouble()
            )
        }
    }
}

fun Double.test(): Boolean =
    Random.nextDouble() < this