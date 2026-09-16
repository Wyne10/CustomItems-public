package org.bigcraft.customitems.activator.condition

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent
import org.bigcraft.customitems.item.CustomItem
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityEvent
import org.bukkit.inventory.ItemStack

class DamagerTypeCondition(key: String, types: Set<EntityType>) : ActivatorCondition<Set<EntityType>>(key, types) {
    override fun appliesTo(event: Event) = event is EntityDamageByEntityEvent

    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean {
        event as EntityDamageByEntityEvent
        return event.damager.type in value
    }
}

class DamagerMetaCondition(key: String, meta: Set<String>) : ActivatorCondition<Set<String>>(key, meta) {
    override fun appliesTo(event: Event) = event is EntityDamageByEntityEvent

    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean {
        event as EntityDamageByEntityEvent
        return value.any { event.damager.hasMetadata(it) }
    }
}

class EntityTypeCondition(key: String, types: Set<EntityType>) : ActivatorCondition<Set<EntityType>>(key, types) {
    override fun appliesTo(event: Event) = event is EntityEvent

    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean {
        event as EntityEvent
        return event.entity.type in value
    }
}

class EntityMetaCondition(key: String, meta: Set<String>) : ActivatorCondition<Set<String>>(key, meta) {
    override fun appliesTo(event: Event) = event is EntityEvent

    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean {
        event as EntityEvent
        return value.any { event.entity.hasMetadata(it) }
    }
}

class ProjectileTypeCondition(key: String, types: Set<EntityType>) : ActivatorCondition<Set<EntityType>>(key, types) {
    override fun appliesTo(event: Event) = event is PlayerLaunchProjectileEvent

    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean {
        event as PlayerLaunchProjectileEvent
        return event.projectile.type in value
    }
}

class DamageCauseCondition(key: String, causes: Set<EntityDamageEvent.DamageCause>) : ActivatorCondition<Set<EntityDamageEvent.DamageCause>>(key, causes) {
    override fun appliesTo(event: Event) = event is EntityDamageEvent

    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean {
        event as EntityDamageEvent
        return event.cause in value
    }
}