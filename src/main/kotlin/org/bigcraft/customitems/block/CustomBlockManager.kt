package org.bigcraft.customitems.block

import com.google.inject.Inject
import com.google.inject.Singleton
import me.wyne.wutils.common.event.EventRegistry
import me.wyne.wutils.json.JSON
import me.wyne.wutils.json.JsonRegistry
import org.bigcraft.customitems.CustomItems
import org.bigcraft.customitems.item.CustomItem
import org.bukkit.block.Block

@Singleton
class CustomBlockManager @Inject constructor(plugin: CustomItems) {

    @JSON(path = "data/blocks.json")
    private val customBlocks = mutableMapOf<BlockKey, CustomBlock>()

    val blockAttributesRegistry = EventRegistry(plugin)

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

    fun placeBlock(block: Block, customItem: CustomItem) {
        val key = BlockKey(block)
        customBlocks[key] = CustomBlock(key, customItem.key)
    }

    fun breakBlock(customBlock: CustomBlock) {
        customBlocks.remove(customBlock.key)
    }

    fun breakBlock(block: Block) {
        customBlocks.remove(BlockKey(block))
    }

    companion object {
        lateinit var instance: CustomBlockManager
            private set
    }

}