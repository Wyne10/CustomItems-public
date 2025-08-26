package org.bigcraft.customitems.item

import com.google.inject.Inject
import com.google.inject.Singleton
import me.wyne.wutils.common.event.EventRegistry
import me.wyne.wutils.common.loadable.LoadableMeta
import org.bigcraft.customitems.AbstractManager
import org.bigcraft.customitems.CustomItems
import org.bigcraft.customitems.item.CustomItem.Companion.ITEM_TAG_KEY
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

@Singleton
@LoadableMeta(priority = 0)
class CustomItemManager @Inject constructor(plugin: CustomItems) : AbstractManager<CustomItem>() {

    override val sectionKey = "item"
    override val valueLoader = CustomItem.Factory

    val restrictionRegistry = EventRegistry(plugin)
    val activatorRegistry = EventRegistry(plugin)

    init {
        instance = this
    }

    fun getItem(key: String): CustomItem? =
        loadedMap[key]

    fun getItem(item: ItemStack?): CustomItem? =
        loadedMap[item?.itemMeta?.persistentDataContainer?.get(ITEM_TAG_KEY, PersistentDataType.STRING)]

    override fun load(config: ConfigurationSection) {
        restrictionRegistry.close()
        activatorRegistry.close()
        super.load(config)
    }

    companion object {
        lateinit var instance: CustomItemManager
            private set
    }

}