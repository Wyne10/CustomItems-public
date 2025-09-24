package org.bigcraft.customitems.activator

import org.bigcraft.customitems.item.CustomItem
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityEvent
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

interface ItemIdentifier {
    fun getItem(event: Event, player: Player, customItem: CustomItem): ItemStack?
}

object EmptyItemIdentifier : ItemIdentifier {
    override fun getItem(event: Event, player: Player, customItem: CustomItem) = ItemStack(Material.AIR)
}

object PlayerItemIdentifier : ItemIdentifier {
    override fun getItem(event: Event, player: Player, customItem: CustomItem): ItemStack? =
        customItem.getItem(player.inventory)
}

object EntityItemIdentifier : ItemIdentifier {
    override fun getItem(event: Event, player: Player, customItem: CustomItem): ItemStack? {
        if (event !is EntityEvent) return null
        val entity = event.entity as? InventoryHolder ?: return null
        return customItem.getItem(entity.inventory)
    }
}

enum class ItemIdentifiers(val identifier: ItemIdentifier) {
    EMPTY(EmptyItemIdentifier),
    PLAYER(PlayerItemIdentifier),
    ENTITY(EntityItemIdentifier)
}