package org.bigcraft.customitems.attribute

import me.wyne.wutils.config.configurables.attribute.AttributeFactory
import me.wyne.wutils.config.configurables.attribute.AttributeMap
import org.bigcraft.customitems.block.CustomBlockManager
import org.bigcraft.customitems.item.RegisterableCustomItemAttribute
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockBreakEvent

val BLOCK_ATTRIBUTE_MAP = AttributeMap(
    mapOf(
        "break-drop" to AttributeFactory { key, config -> BreakDropAttribute(key, config.getBoolean(key)) }
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

    @EventHandler
    private fun onBreak(event: BlockBreakEvent) {
        if (!item.isCustomItem(event.block)) return
        event.isDropItems = false
        event.block.world.dropItem(event.block.location, item.createItemStack(event.player))
    }

}