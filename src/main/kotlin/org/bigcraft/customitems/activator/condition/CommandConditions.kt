package org.bigcraft.customitems.activator.condition

import org.bigcraft.customitems.item.CustomItem
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.inventory.ItemStack

class CommandWhitelistCondition(key: String, commands: Set<String>) : ActivatorCondition<Set<String>>(key, commands) {
    override fun appliesTo(event: Event) = event is PlayerCommandPreprocessEvent

    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean {
        event as PlayerCommandPreprocessEvent
        return value.any { event.message.contains(it) }
    }
}