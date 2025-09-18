package org.bigcraft.customitems.block

import com.google.inject.Singleton
import me.wyne.wutils.json.JSON
import me.wyne.wutils.json.JsonRegistry
import org.bukkit.block.Block

@Singleton
class CustomBlockManager {

    @JSON(path = "data/blocks.json")
    private val customBlocks = mutableMapOf<BlockKey, CustomBlock>()

    init {
        instance = this
        JsonRegistry.global.registerObject(this)
    }

    fun isCustomBlock(block: Block) =
        customBlocks.containsKey(BlockKey(block))

    fun getCustomBlock(key: BlockKey): CustomBlock? =
        customBlocks[key]

    fun getCustomBlock(block: Block): CustomBlock? =
        customBlocks[BlockKey(block)]

    companion object {
        lateinit var instance: CustomBlockManager
            private set
    }

}