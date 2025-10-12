package org.bigcraft.customitems.activator.condition

import com.sk89q.worldedit.bukkit.BukkitAdapter
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
