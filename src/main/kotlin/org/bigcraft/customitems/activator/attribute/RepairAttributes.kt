package org.bigcraft.customitems.activator.attribute

import org.bigcraft.customitems.activator.ActivatorAttribute
import org.bigcraft.customitems.item.CustomItem
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable

class RepairAttribute(
    key: String,
    slots: Set<EquipmentSlot>
) : ActivatorAttribute<Set<EquipmentSlot>>(key, slots) {
    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        value.mapNotNull { player.inventory.getItem(it) }
            .filter { it.enchantments.any { enchantment -> enchantment.key.key.value().contains("mending", ignoreCase = true) } }
            .forEach { it.editMeta { meta -> (meta as? Damageable)?.damage = 0 } }
    }
}
