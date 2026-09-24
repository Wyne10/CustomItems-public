package me.wyne.customitems.activator.attribute

import me.wyne.wutils.common.scheduler.Schedulers
import me.wyne.wutils.i18n.kotlin.placeholderString
import me.wyne.customitems.activator.ActivatorAttribute
import me.wyne.customitems.api.spi.CooldownContext
import me.wyne.customitems.item.CustomItem
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.inventory.ItemStack

class UpdateViewAttribute(
    key: String,
    value: Boolean
) : ActivatorAttribute<Boolean>(key, value) {
    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        if (!value) return
        Schedulers.sync().run { customItem.refreshView(item, player) }
    }
}

class CancelEventAttribute(
    key: String,
    value: Boolean
) : ActivatorAttribute<Boolean>(key, value) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack) =
        event is Cancellable

    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        event as Cancellable
        event.isCancelled = value
    }
}

class CooldownCancelEventAttribute(
    key: String,
    value: Boolean
) : ActivatorAttribute<Boolean>(key, value) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack) =
        event is Cancellable && customItem.cooldowns.isAnyActive(CooldownContext(player, customItem, item))

    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        event as Cancellable
        event.isCancelled = value
        customItem.cooldowns.notifyBlocked(CooldownContext(player, customItem, item))
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

class CooldownAttribute(
    key: String,
    cooldowns: Set<String>
) : ActivatorAttribute<Set<String>>(key, cooldowns) {
    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        customItem.cooldowns.start(value, CooldownContext(player, customItem, item))
    }
}
