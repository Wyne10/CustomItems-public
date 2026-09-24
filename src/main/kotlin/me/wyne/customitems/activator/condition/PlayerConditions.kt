package me.wyne.customitems.activator.condition

import me.wyne.wutils.common.comparator.DoubleComparator
import me.wyne.wutils.common.comparator.IntComparator
import me.wyne.wutils.common.kotlin.player.currentExp
import me.wyne.customitems.activator.attribute.clampToInt
import me.wyne.customitems.aoe.AoeManager
import me.wyne.customitems.item.CustomItem
import me.wyne.customitems.worldguard.RegionPatterns
import me.wyne.customitems.worldguard.regionsAt
import me.wyne.customitems.AntiRelogWrapper
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Biome
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

class LevelCondition(key: String, comparator: IntComparator) : ActivatorCondition<IntComparator>(key, comparator) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean =
        value.compare(player.level)
}

class ExpCondition(key: String, comparator: IntComparator) : ActivatorCondition<IntComparator>(key, comparator) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean =
        value.compare(player.currentExp.clampToInt())
}

class FoodLevelCondition(key: String, comparator: IntComparator) : ActivatorCondition<IntComparator>(key, comparator) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean =
        value.compare(player.foodLevel)
}

class HealthCondition(key: String, comparator: DoubleComparator) : ActivatorCondition<DoubleComparator>(key, comparator) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean =
        value.compare(player.health)
}

class HasPermissionsCondition(key: String, permissions: Set<String>) : ActivatorCondition<Set<String>>(key, permissions) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean =
        value.all { player.hasPermission(it) }
}

/**
 * Deliberately not the inverse of [HasPermissionsCondition]: that one requires *all* the listed
 * permissions, so its strict negation would be "missing at least one". This requires *none* of
 * them, which is what a blacklist wants and what the key has always meant.
 */
class NotHasPermissionsCondition(key: String, permissions: Set<String>) : ActivatorCondition<Set<String>>(key, permissions) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean =
        value.all { !player.hasPermission(it) }
}

class TargetBlockCondition(key: String, materials: Set<Material>) : ActivatorCondition<Set<Material>>(key, materials) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean =
        player.getTargetBlock(5)?.type in value
}

class OnBlockCondition(key: String, materials: Set<Material>) : ActivatorCondition<Set<Material>>(key, materials) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean =
        player.location.block.getRelative(BlockFace.DOWN).type in value
}

class InBiomeCondition(key: String, biomes: Set<Biome>) : ActivatorCondition<Set<Biome>>(key, biomes) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean =
        player.location.world.getBiome(player.location.blockX, player.location.blockY, player.location.blockZ) in value
}

class InWorldCondition(key: String, worlds: Set<World>) : ActivatorCondition<Set<World>>(key, worlds) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean =
        player.location.world in value
}

class WithinAoeCondition(key: String, aoeKeys: Set<String>) : ActivatorCondition<Set<String>>(key, aoeKeys) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean =
        value.flatMap { AoeManager.instance.getAoe(it) }
            .any { it.bounds.contains(player) }
}

class AoeAffectedCondition(key: String, aoeKeys: Set<String>) : ActivatorCondition<Set<String>>(key, aoeKeys) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean =
        value.flatMap { AoeManager.instance.getAoe(it) }
            .filter { it.owner != player }
            .any { it.bounds.contains(player) }
}

class WithinRegionCondition(key: String, patterns: RegionPatterns) : ActivatorCondition<RegionPatterns>(key, patterns) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean =
        value.matchesAny(regionsAt(player.location))
}

class WithinTopRegionCondition(key: String, patterns: RegionPatterns) : ActivatorCondition<RegionPatterns>(key, patterns) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean =
        value.matchesTop(regionsAt(player.location))
}

class PvpCondition(key: String, isActive: Boolean) : ActivatorCondition<Boolean>(key, isActive) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean =
        AntiRelogWrapper.isInPvp(player) == value
}