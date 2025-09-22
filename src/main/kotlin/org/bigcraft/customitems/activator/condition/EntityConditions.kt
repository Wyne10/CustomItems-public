package org.bigcraft.customitems.activator.condition

import org.bigcraft.customitems.item.CustomItem
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack

class DamagerTypeCondition(key: String, types: Set<EntityType>) : ActivatorCondition<Set<EntityType>>(key, types) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean {
        if (event !is EntityDamageByEntityEvent) return true
        return event.damager.type in value
    }
}

class EntityTypeCondition(key: String, types: Set<EntityType>) : ActivatorCondition<Set<EntityType>>(key, types) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean {
        if (event !is EntityDamageByEntityEvent) return true
        return event.entity.type in value
    }
}