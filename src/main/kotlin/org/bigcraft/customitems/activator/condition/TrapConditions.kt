package org.bigcraft.customitems.activator.condition

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion
import org.bigcraft.customitems.item.CustomItem
import org.bigcraft.customitems.trap.CreateTrapEvent
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.inventory.ItemStack

class TrapBlockCondition(key: String, blocks: Set<Material>) : ActivatorCondition<Set<Material>>(key, blocks) {

    private val adaptedBlocks = value.map { BukkitAdapter.asBlockType(it) }

    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean {
        if (event !is CreateTrapEvent) return true
        val blocks = event.snapshot.region.map { event.snapshot.getBlock(it) }
        return blocks.none { it.blockType in adaptedBlocks }
    }

}

class TrapRegionCondition(key: String, regions: Set<String>) : ActivatorCondition<Set<String>>(key, regions) {

    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean {
        if (event !is CreateTrapEvent) return true
        val regions = WorldGuard.getInstance().platform.regionContainer.get(BukkitAdapter.adapt(event.location.world))
            ?.getApplicableRegions(ProtectedCuboidRegion(event.scheme.key, event.snapshot.region.minimumPoint, event.snapshot.region.maximumPoint))
        return regions?.all { it.id in value } ?: false
    }

}

class TrapTopRegionCondition(key: String, regions: Set<String>) : ActivatorCondition<Set<String>>(key, regions) {

    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean {
        if (event !is CreateTrapEvent) return true
        val region = WorldGuard.getInstance().platform.regionContainer.get(BukkitAdapter.adapt(event.location.world))
            ?.getApplicableRegions(ProtectedCuboidRegion(event.scheme.key, event.snapshot.region.minimumPoint, event.snapshot.region.maximumPoint))
            ?.maxBy { it.priority }
        return region?.let { value.contains(it.id) } ?: false
    }

}
