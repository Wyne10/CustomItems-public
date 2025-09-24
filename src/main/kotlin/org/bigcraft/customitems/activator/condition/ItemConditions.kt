package org.bigcraft.customitems.activator.condition

import org.bigcraft.customitems.config.IntComparator
import org.bigcraft.customitems.item.CustomItem
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionEffectType

class ItemDurabilityCondition(key: String, comparator: IntComparator) : ActivatorCondition<IntComparator>(key, comparator) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean {
        if (item.itemMeta !is Damageable) return false
        val durability = item.type.maxDurability - (item.itemMeta as Damageable).damage
        return value.compare(durability)
    }
}

class ItemCooldownCondition(key: String, comparator: IntComparator) : ActivatorCondition<IntComparator>(key, comparator) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean =
        value.compare(player.getCooldown(item.type))
}

class ItemEquipmentCondition(key: String, slots: Set<EquipmentSlot>) : ActivatorCondition<Set<EquipmentSlot>>(key, slots) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean =
        value.any { customItem.isCustomItem(player.inventory.getItem(it)) }
}

class ItemTypeCondition(key: String, types: Set<Material>) : ActivatorCondition<Set<Material>>(key, types) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean =
        item.type in value
}

class PotionTypeCondition(key: String, types: Set<PotionEffectType>) : ActivatorCondition<Set<PotionEffectType>>(key, types) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean {
        val potionMeta = item.itemMeta as? PotionMeta ?: return true
        return value.any { potionMeta.hasCustomEffect(it) }
    }
}