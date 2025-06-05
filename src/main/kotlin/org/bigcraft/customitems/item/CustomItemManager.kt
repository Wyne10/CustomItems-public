package org.bigcraft.customitems.item

import com.google.inject.Singleton
import me.wyne.wutils.common.loadable.LoadableMeta
import org.bigcraft.customitems.AbstractManager
import org.bigcraft.customitems.event.EventRegistry
import org.bigcraft.customitems.item.CustomItem.Companion.ITEM_TAG_KEY
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

@Singleton
@LoadableMeta(priority = 0)
class CustomItemManager : AbstractManager<CustomItem>() {

    override val sectionKey = "item"
    override val valueLoader = CustomItem.Factory

    val restrictionRegistry = EventRegistry()

    init {
        instance = this
    }

    fun getItem(key: String): CustomItem? =
        loadedMap[key]

    fun getItem(item: ItemStack?): CustomItem? =
        loadedMap[item?.itemMeta?.persistentDataContainer?.get(ITEM_TAG_KEY, PersistentDataType.STRING)]

    override fun load(config: ConfigurationSection) {
        restrictionRegistry.close()
        super.load(config)
    }

    companion object {
        lateinit var instance: CustomItemManager
            private set
    }

}