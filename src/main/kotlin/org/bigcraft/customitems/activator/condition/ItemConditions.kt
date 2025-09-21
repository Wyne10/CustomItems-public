package org.bigcraft.customitems.activator.condition

import org.bigcraft.customitems.config.IntComparator
import org.bigcraft.customitems.item.CustomItem
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable

class DurabilityCondition(key: String, comparator: IntComparator) : ActivatorCondition<IntComparator>(key, comparator) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean {
        if (item.itemMeta !is Damageable) return false
        val durability = item.type.maxDurability - (item.itemMeta as Damageable).damage
        return value.compare(durability)
    }
}

class CooldownCondition(key: String, comparator: IntComparator) : ActivatorCondition<IntComparator>(key, comparator) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean =
        value.compare(player.getCooldown(item.type))
}

class ItemEquipmentCondition(key: String, slots: Set<EquipmentSlot>) : ActivatorCondition<Set<EquipmentSlot>>(key, slots) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean =
        value.any { customItem.isCustomItem(player.inventory.getItem(it)) }
}
