package me.wyne.customitems.block

import com.google.inject.Inject
import com.google.inject.Singleton
import me.wyne.wutils.json.JSON
import me.wyne.wutils.json.JsonRegistry
import me.wyne.customitems.api.CustomBlockRegistry
import me.wyne.customitems.api.CustomItemApi
import org.bukkit.block.Block

@Singleton
class CustomBlockManager @Inject constructor() : CustomBlockRegistry {

    @JSON(path = "data/blocks.json")
    private val customBlocks = mutableMapOf<BlockKey, CustomBlock>()

    init {
        instance = this
        JsonRegistry.global.registerObject(this)
    }

    override fun isCustomBlock(block: Block) =
        customBlocks.containsKey(BlockKey(block))

    fun getCustomBlock(key: BlockKey): CustomBlock? =
        customBlocks[key]

    fun getCustomBlock(block: Block): CustomBlock? =
        customBlocks[BlockKey(block)]

    override fun getBlock(block: Block): CustomItemApi? =
        getCustomBlock(block)?.item

    override fun placeBlock(block: Block, customItem: CustomItemApi) {
        val key = BlockKey(block)
        customBlocks[key] = CustomBlock(key, customItem.key)
    }

    fun breakBlock(customBlock: CustomBlock) {
        customBlocks.remove(customBlock.key)
    }

    override fun breakBlock(block: Block) {
        customBlocks.remove(BlockKey(block))
    }

    companion object {
        lateinit var instance: CustomBlockManager
            private set
    }

}