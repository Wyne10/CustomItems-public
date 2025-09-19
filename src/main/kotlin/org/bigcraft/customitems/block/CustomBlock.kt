package org.bigcraft.customitems.block

import org.bigcraft.customitems.item.CustomItem
import org.bigcraft.customitems.item.CustomItemManager
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.block.Block
import java.util.UUID

data class CustomBlock(
    val key: BlockKey,
    val itemKey: String
) {
    val item: CustomItem
        get() = CustomItemManager.instance.getItem(itemKey)!!
}

data class BlockKey(val key: Long, val worldUuid: UUID) {
    val world: World
        get() = Bukkit.getWorld(worldUuid)!!

    constructor(block: Block) : this(block.blockKey, block.world.uid)
}