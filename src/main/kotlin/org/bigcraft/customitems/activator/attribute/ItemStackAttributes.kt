package org.bigcraft.customitems.activator.attribute

import me.wyne.wutils.common.scheduler.Schedulers
import me.wyne.wutils.common.Ticks
import me.wyne.wutils.common.kotlin.item.damageNaturally
import me.wyne.wutils.common.operation.IntOperation
import org.bigcraft.customitems.activator.ActivatorAttribute
import org.bigcraft.customitems.item.CustomItem
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta

class DropLockAttribute(
    key: String,
    durationMillis: Long
) : ActivatorAttribute<Long>(key, durationMillis) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack) =
        event is PlayerDeathEvent

    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        event as PlayerDeathEvent
        val killedPlayer = event.entity
        val drops = event.drops.toList()
        event.drops.clear()
        val items = drops
            .map { killedPlayer.world.dropItemNaturally(killedPlayer.location, it) }
        items.forEach { it.owner = player.uniqueId }
        Schedulers.sync().runLater({
            items.forEach { it.owner = null }
        }, Ticks.ofMillis(value))
    }
}

class ItemAmountAttribute(
    key: String,
    operation: IntOperation
) : ActivatorAttribute<IntOperation>(key, operation) {
    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        item.amount = value.evaluate(item.amount)
    }
}

class ItemDurabilityAttribute(
    key: String,
    operation: IntOperation
) : ActivatorAttribute<IntOperation>(key, operation) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean =
        item.itemMeta is Damageable

    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        val damageable = item.itemMeta as Damageable
        damageable.damage = item.type.maxDurability - value.evaluate(item.type.maxDurability - damageable.damage)
        item.itemMeta = damageable as ItemMeta
    }
}

class ItemOwnerAttribute(
    key: String,
    isActive: Boolean
) : ActivatorAttribute<Boolean>(key, isActive) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean =
        event is PlayerDropItemEvent

    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        val dropEvent = event as? PlayerDropItemEvent ?: return
        if (value)
            dropEvent.itemDrop.owner = player.uniqueId
    }
}

class DamageItemAttribute(
    key: String,
    damage: Int
) : ActivatorAttribute<Int>(key, damage) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean =
        item.itemMeta is Damageable

    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        item.damageNaturally(player, value)
    }
}
