package me.wyne.customitems.activator.condition

import me.wyne.wutils.common.comparator.IntComparator
import me.wyne.customitems.api.spi.CooldownContext
import me.wyne.customitems.composition.ComposedKeys
import me.wyne.customitems.item.CustomItem
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionEffectType

class ItemDurabilityCondition(key: String, comparator: IntComparator) : ActivatorCondition<IntComparator>(key, comparator) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean {
        if (item.itemMeta !is Damageable) return true
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

class ItemComposedCondition(key: String, isActive: Boolean) : ActivatorCondition<Boolean>(key, isActive) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean =
        (customItem.key in ComposedKeys.of(item)) == value
}

class ItemTypeCondition(key: String, types: Set<Material>) : ActivatorCondition<Set<Material>>(key, types) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean =
        item.type in value
}

class ConsumedItemTypeCondition(key: String, types: Set<Material>) : ActivatorCondition<Set<Material>>(key, types) {
    override fun appliesTo(event: Event) = event is PlayerItemConsumeEvent

    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean {
        event as PlayerItemConsumeEvent
        return event.item.type in value
    }
}

class PotionTypeCondition(key: String, types: Set<PotionEffectType>) : ActivatorCondition<Set<PotionEffectType>>(key, types) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean {
        val potionMeta = item.itemMeta as? PotionMeta ?: return true
        return value.any { potionMeta.hasCustomEffect(it) }
    }
}

class CustomItemCooldownCondition(key: String, isActive: Boolean) : ActivatorCondition<Boolean>(key, isActive) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean {
        val context = CooldownContext(player, customItem, item)
        val active = customItem.cooldowns.isAnyActive(context)
        if (active && !value) customItem.cooldowns.notifyBlocked(context)
        return active == value
    }
}

class ConsumedItemCustomCondition(key: String, isActive: Boolean) : ActivatorCondition<Boolean>(key, isActive) {
    override fun appliesTo(event: Event) = event is PlayerItemConsumeEvent

    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean {
        event as PlayerItemConsumeEvent
        return customItem.isCustomItem(event.item) == value
    }
}

class DroppedItemCustomCondition(key: String, isActive: Boolean) : ActivatorCondition<Boolean>(key, isActive) {
    override fun appliesTo(event: Event) = event is PlayerDropItemEvent

    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean {
        event as PlayerDropItemEvent
        return customItem.isCustomItem(event.itemDrop.itemStack) == value
    }
}
