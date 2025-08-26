package org.bigcraft.customitems.activator

import me.wyne.wutils.config.configurables.attribute.AttributeBase
import me.wyne.wutils.config.configurables.attribute.AttributeFactory
import me.wyne.wutils.config.configurables.attribute.AttributeMap
import me.wyne.wutils.i18n.kotlin.placeholderString
import org.bigcraft.customitems.item.CustomItem
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.inventory.ItemStack

val ACTIVATOR_ATTRIBUTE_MAP = AttributeMap(
    mapOf(
        "cancel" to AttributeFactory { key, config -> CancelEventAttribute(key, config.getBoolean(key)) },
        "commands" to AttributeFactory { key, config -> CommandsAttribute(key, config.getStringList(key)) }
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