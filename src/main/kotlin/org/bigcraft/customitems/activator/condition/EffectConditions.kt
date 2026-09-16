package org.bigcraft.customitems.activator.condition

import org.bigcraft.customitems.item.CustomItem
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityPotionEffectEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType

class EffectActionCondition(key: String, actions: Set<EntityPotionEffectEvent.Action>) : ActivatorCondition<Set<EntityPotionEffectEvent.Action>>(key, actions) {
    override fun appliesTo(event: Event) = event is EntityPotionEffectEvent

    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean {
        event as EntityPotionEffectEvent
        return event.action in value
    }
}

class EffectTypeCondition(key: String, types: Set<PotionEffectType>) : ActivatorCondition<Set<PotionEffectType>>(key, types) {
    override fun appliesTo(event: Event) = event is EntityPotionEffectEvent

    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean {
        event as EntityPotionEffectEvent
        return event.newEffect?.type in value
    }
}

class EffectCauseCondition(key: String, causes: Set<EntityPotionEffectEvent.Cause>) : ActivatorCondition<Set<EntityPotionEffectEvent.Cause>>(key, causes) {
    override fun appliesTo(event: Event) = event is EntityPotionEffectEvent

    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean {
        event as EntityPotionEffectEvent
        return event.cause in value
    }
}
