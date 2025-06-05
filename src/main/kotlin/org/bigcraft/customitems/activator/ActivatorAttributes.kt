package org.bigcraft.customitems.activator

import me.wyne.wutils.config.configurables.attribute.AttributeBase
import me.wyne.wutils.config.configurables.attribute.AttributeFactory
import me.wyne.wutils.config.configurables.attribute.AttributeMap
import org.bigcraft.customitems.item.CustomItem
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event

val ACTIVATOR_ATTRIBUTE_MAP = AttributeMap(
    mutableMapOf(
        "cancel" to AttributeFactory { key, config -> CancelEventAttribute(key, config.getBoolean(key)) },
        "commands" to AttributeFactory { key, config -> CommandAttribute(key, config.getStringList(key)) }
    )
)

abstract class ActivatorAttribute<V>(key: String, value: V) : AttributeBase<V>(key, value) {
    abstract fun apply(event: Event, player: Player, item: CustomItem)
}

class CancelEventAttribute(
    key: String,
    value: Boolean
) : ActivatorAttribute<Boolean>(key, value) {
    override fun apply(event: Event, player: Player, item: CustomItem) {
        if (!value) return
        if (event is Cancellable)
            event.isCancelled = true
    }
}

class CommandAttribute(
    key: String,
    commands: List<String>
) : ActivatorAttribute<List<String>>(key, commands) {
    override fun apply(event: Event, player: Player, item: CustomItem) {
        value.forEach { Bukkit.dispatchCommand(Bukkit.getConsoleSender(), it) }
    }
}