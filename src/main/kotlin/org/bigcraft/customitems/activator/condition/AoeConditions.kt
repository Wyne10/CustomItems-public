package org.bigcraft.customitems.activator.condition

import org.bigcraft.customitems.aoe.event.AoeFinishEvent
import org.bigcraft.customitems.item.CustomItem
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.inventory.ItemStack

class AoeFinishConditions(key: String, aoeKeys: Set<String>) : ActivatorCondition<Set<String>>(key, aoeKeys) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean {
        if (event !is AoeFinishEvent) return true
        return event.areaOfEffect.key in value
    }
}