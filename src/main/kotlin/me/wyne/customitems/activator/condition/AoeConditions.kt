package me.wyne.customitems.activator.condition

import me.wyne.customitems.aoe.event.AoeFinishEvent
import me.wyne.customitems.item.CustomItem
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.inventory.ItemStack

class AoeFinishConditions(key: String, aoeKeys: Set<String>) : ActivatorCondition<Set<String>>(key, aoeKeys) {
    override fun appliesTo(event: Event) = event is AoeFinishEvent

    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean {
        event as AoeFinishEvent
        return event.areaOfEffect.key in value
    }
}