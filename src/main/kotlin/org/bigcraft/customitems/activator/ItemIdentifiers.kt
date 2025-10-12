package org.bigcraft.customitems.activator

import org.bigcraft.customitems.item.CustomItem
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

interface ItemIdentifier {
    fun getItem(player: Player, customItem: CustomItem): ItemStack?
}

object EmptyItemIdentifier : ItemIdentifier {
    override fun getItem(player: Player, customItem: CustomItem) = ItemStack(Material.AIR)
}

object PlayerItemIdentifier : ItemIdentifier {
    override fun getItem(player: Player, customItem: CustomItem): ItemStack? =
        customItem.getItem(player.inventory)
}

object HoldItemIdentifier : ItemIdentifier {
    override fun getItem(player: Player, customItem: CustomItem): ItemStack? {
        return if (customItem.isCustomItem(player.inventory.getItem(EquipmentSlot.HAND)))
            player.inventory.getItem(EquipmentSlot.HAND)
        else if (customItem.isCustomItem(player.inventory.getItem(EquipmentSlot.OFF_HAND)))
            player.inventory.getItem(EquipmentSlot.OFF_HAND)
        else null
    }
}

enum class ItemIdentifiers(val identifier: ItemIdentifier) {
    EMPTY(EmptyItemIdentifier),
    PLAYER(PlayerItemIdentifier),
    HOLD(HoldItemIdentifier),
}