package org.bigcraft.customitems.activator.condition

import org.bigcraft.customitems.item.CustomItem
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityPotionEffectEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType

class PotionActionCondition(key: String, actions: Set<EntityPotionEffectEvent.Action>) : ActivatorCondition<Set<EntityPotionEffectEvent.Action>>(key, actions) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean {
        if (event !is EntityPotionEffectEvent) return true
        return event.action in value
    }
}

class PotionTypeCondition(key: String, types: Set<PotionEffectType>) : ActivatorCondition<Set<PotionEffectType>>(key, types) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean {
        if (event !is EntityPotionEffectEvent) return true
        return event.newEffect?.type in value
    }
}