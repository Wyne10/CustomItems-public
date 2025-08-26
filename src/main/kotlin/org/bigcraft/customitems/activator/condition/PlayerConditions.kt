package org.bigcraft.customitems.activator.condition

import org.bigcraft.customitems.config.IntComparator
import org.bigcraft.customitems.item.CustomItem
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.inventory.ItemStack

class SneakingCondition(key: String, isActive: Boolean) : ActivatorCondition<Boolean>(key, isActive) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack) =
        player.isSneaking == value
}

class SprintingCondition(key: String, isActive: Boolean) : ActivatorCondition<Boolean>(key, isActive) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean =
        player.isSprinting == value
}

class FlyingCondition(key: String, isActive: Boolean) : ActivatorCondition<Boolean>(key, isActive) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean =
        player.isFlying == value
}

class BlockingCondition(key: String, isActive: Boolean) : ActivatorCondition<Boolean>(key, isActive) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean =
        player.isBlocking == value
}

class GlidingCondition(key: String, isActive: Boolean) : ActivatorCondition<Boolean>(key, isActive) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean =
        player.isGliding == value
}

class SwimmingCondition(key: String, isActive: Boolean) : ActivatorCondition<Boolean>(key, isActive) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean =
        player.isSwimming == value
}

class OnFireCondition(key: String, isActive: Boolean) : ActivatorCondition<Boolean>(key, isActive) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean =
        (player.fireTicks != 0) == value
}

class InAirCondition(key: String, isActive: Boolean) : ActivatorCondition<Boolean>(key, isActive) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean =
        (player.location.block.getRelative(BlockFace.DOWN).type == Material.AIR) == value
}

class LightLevelCondition(key: String, comparator: IntComparator) : ActivatorCondition<IntComparator>(key, comparator) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean =
        value.compare(player.location.block.lightLevel.toInt())
}