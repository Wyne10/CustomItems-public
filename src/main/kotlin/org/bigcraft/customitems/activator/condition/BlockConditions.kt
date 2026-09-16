package org.bigcraft.customitems.activator.condition

import org.bigcraft.customitems.aoe.AoeManager
import org.bigcraft.customitems.item.CustomItem
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.block.BlockEvent
import org.bukkit.inventory.ItemStack

class BlockWithinAoeCondition(key: String, aoeKeys: Set<String>) : ActivatorCondition<Set<String>>(key, aoeKeys) {
    override fun appliesTo(event: Event) = event is BlockEvent

    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean {
        event as BlockEvent
        return value.flatMap { AoeManager.instance.getAoe(it) }
            .any { it.bounds.contains(event.block.location) }
    }
}

class BlockTypeCondition(key: String, types: Set<Material>) : ActivatorCondition<Set<Material>>(key, types) {
    override fun appliesTo(event: Event) = event is BlockEvent

    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean {
        event as BlockEvent
        return event.block.type in value
    }
}

class BlockMetaCondition(key: String, meta: Set<String>) : ActivatorCondition<Set<String>>(key, meta) {
    override fun appliesTo(event: Event) = event is BlockEvent

    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean {
        event as BlockEvent
        return value.any { event.block.hasMetadata(it) }
    }
}
