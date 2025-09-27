package org.bigcraft.customitems.attribute

import com.destroystokyo.paper.event.block.AnvilDamagedEvent
import me.wyne.wutils.common.inventory.IUtils
import me.wyne.wutils.config.configurables.attribute.AttributeFactory
import me.wyne.wutils.config.configurables.attribute.AttributeMap
import org.bigcraft.customitems.block.CustomBlockManager
import org.bigcraft.customitems.item.RegisterableCustomItemAttribute
import org.bukkit.Material
import org.bukkit.block.Container
import org.bukkit.block.Furnace
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockBurnEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityExplodeEvent

val BLOCK_ATTRIBUTE_MAP = AttributeMap(
    mapOf(
        "break-drop" to AttributeFactory { key, config -> BreakDropAttribute(key, config.getBoolean(key)) },
        "break-magnet" to AttributeFactory { key, config -> BreakMagnetAttribute(key, config.getBoolean(key)) },
        "burn-drop" to AttributeFactory { key, config -> BurnDropAttribute(key, config.getBoolean(key)) },
        "explode-drop" to AttributeFactory { key, config -> ExplodeDropAttribute(key, config.getBoolean(key)) },
        "furnace-speed" to AttributeFactory { key, config -> FurnaceSpeedAttribute(key, config.getDouble(key)) },
        "unbreakable-anvil" to AttributeFactory { key, config -> UnbreakableAnvilAttribute(key, config.getBoolean(key)) },
    )
)

abstract class BlockAttribute<V>(
    key: String,
    value: V
) : RegisterableCustomItemAttribute<V>(key, value, CustomBlockManager.instance.blockAttributesRegistry)

class BreakDropAttribute(
    key: String,
    drop: Boolean,
) : BlockAttribute<Boolean>(key, drop) {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private fun onBreak(event: BlockBreakEvent) {
        if (!item.isCustomItem(event.block)) return
        event.isDropItems = false
        val state = event.block.state
        if (state is Container)
            state.inventory
                .filterNotNull()
                .forEach { event.block.world.dropItem(event.block.location, it) }
        event.block.world.dropItem(event.block.location, item.createItemStack(event.player))
    }

}

class BreakMagnetAttribute(
    key: String,
    magnet: Boolean,
) : BlockAttribute<Boolean>(key, magnet) {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private fun onBreak(event: BlockBreakEvent) {
        if (!item.isCustomItem(event.block)) return
        event.isDropItems = false
        val state = event.block.state
        if (state is Container)
            state.inventory
                .filterNotNull()
                .forEach { IUtils.addOrDrop(event.player, it) }
        IUtils.addOrDrop(event.player, item.createItemStack(event.player))
    }

}

class BurnDropAttribute(
    key: String,
    drop: Boolean
) : BlockAttribute<Boolean>(key, drop) {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private fun onBurn(event: BlockBurnEvent) {
        if (!item.isCustomItem(event.block)) return
        event.block.world.dropItem(event.block.location, item.createItemStack())
    }

}

class ExplodeDropAttribute(
    key: String,
    drop: Boolean
) : BlockAttribute<Boolean>(key, drop) {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private fun onExplode(event: EntityExplodeEvent) {
        event.blockList().forEach { block ->
            if (!item.isCustomItem(block)) return@forEach
            block.world.dropItem(block.location, item.createItemStack())
            block.type = Material.AIR
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private fun onBlockExplode(event: BlockExplodeEvent) {
        event.blockList().forEach { block ->
            if (!item.isCustomItem(block)) return@forEach
            block.world.dropItem(block.location, item.createItemStack())
            block.type = Material.AIR
        }
    }

}

class FurnaceSpeedAttribute(
    key: String,
    multiplier: Double
) : BlockAttribute<Double>(key, multiplier) {

    @EventHandler(ignoreCancelled = true)
    private fun onPlace(event: BlockPlaceEvent) {
        if (!item.isCustomItem(event.itemInHand)) return
        if (event.block.state !is Furnace) return
        (event.block.state as Furnace).let { furnace ->
            furnace.cookSpeedMultiplier = value
            furnace.update()
        }
    }

}

class UnbreakableAnvilAttribute(
    key: String,
    unbreakable: Boolean,
) : BlockAttribute<Boolean>(key, unbreakable) {

    @EventHandler(ignoreCancelled = true)
    private fun onDamage(event: AnvilDamagedEvent) {
        if (!item.isCustomItem((event.inventory.location!!.block))) return
        event.isCancelled = true
    }

}