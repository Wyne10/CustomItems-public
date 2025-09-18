package org.bigcraft.customitems.block

import org.bigcraft.customitems.item.CustomItem
import org.bukkit.World
import org.bukkit.block.Block

class CustomBlock(
    val key: BlockKey,
    val item: CustomItem
)

data class BlockKey(val key: Long, val world: World) {
    constructor(block: Block) : this(block.blockKey, block.world)
}