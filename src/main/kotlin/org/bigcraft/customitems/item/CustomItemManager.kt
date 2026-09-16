package org.bigcraft.customitems.item

import com.google.inject.Inject
import com.google.inject.Singleton
import me.wyne.wutils.common.loadable.LoadableMeta
import org.bigcraft.customitems.CustomItems
import org.bigcraft.customitems.LoadableProvider
import org.bigcraft.customitems.CustomKeyedLoot
import org.bigcraft.customitems.api.CustomItemApi
import org.bigcraft.customitems.api.CustomItemProvider
import org.bigcraft.customitems.block.CustomBlockManager
import org.bigcraft.customitems.item.CustomItem.Companion.ITEM_TAG_KEY
import org.bukkit.Bukkit
import org.bukkit.configuration.serialization.ConfigurationSerialization
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

@Singleton
@LoadableMeta(priority = 0)
class CustomItemManager @Inject constructor(plugin: CustomItems, private val blockManager: CustomBlockManager) :
    LoadableProvider<CustomItem>(SECTION_KEY, CustomItem.Factory), Listener, CustomItemProvider {

    val itemPaths
        get() = loadedMap.values.map { it.path + it.key }.toSet()

    init {
        instance = this
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    override fun getKeys(): Set<String> = loadedMap.keys

    override fun getPaths(): Set<String> = itemPaths

    override fun contains(key: String): Boolean =
        loadedMap.containsKey(key)

    override fun getItem(key: String): CustomItem? =
        loadedMap[key]

    override fun getItem(item: ItemStack?): CustomItem? {
        if (item == null || !item.hasItemMeta()) return null
        return loadedMap[item.itemMeta.persistentDataContainer.get(ITEM_TAG_KEY, PersistentDataType.STRING)]
    }

    override fun onLoaded(key: String, value: CustomItem, path: String) {
        value.path = path
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private fun onPlace(event: BlockPlaceEvent) {
        val customItem = getItem(event.itemInHand) ?: return
        if (!customItem.isBlock) return
        blockManager.placeBlock(event.block, customItem)
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private fun onBreak(event: BlockBreakEvent) {
        val customBlock = blockManager.getCustomBlock(event.block) ?: return
        blockManager.breakBlock(customBlock)
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private fun onChange(event: EntityChangeBlockEvent) {
        val customBlock = blockManager.getCustomBlock(event.block) ?: return
        blockManager.breakBlock(customBlock)
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private fun onExplode(event: EntityExplodeEvent) {
        event.blockList().forEach { block ->
            val customBlock = blockManager.getCustomBlock(block) ?: return@forEach
            blockManager.breakBlock(customBlock)
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private fun onBlockExplode(event: BlockExplodeEvent) {
        event.blockList().forEach { block ->
            val customBlock = blockManager.getCustomBlock(block) ?: return@forEach
            blockManager.breakBlock(customBlock)
        }
    }

    companion object {
        const val SECTION_KEY = "item"

        lateinit var instance: CustomItemManager
            private set
    }

}