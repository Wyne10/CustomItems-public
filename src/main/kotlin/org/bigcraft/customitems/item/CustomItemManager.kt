package org.bigcraft.customitems.item

import com.google.inject.Singleton
import me.wyne.wutils.common.loadable.LoadableMeta
import org.bigcraft.customitems.AbstractManager
import org.bigcraft.customitems.ConfigurableFactory

const val ITEM_SECTION_KEY = "item"

@Singleton
@LoadableMeta(priority = 0)
class CustomItemManager : AbstractManager<CustomItem>() {

    override val sectionKey: String
        get() = ITEM_SECTION_KEY
    override val valueLoader: ConfigurableFactory<CustomItem>
        get() = CustomItem.Factory

    init {
        instance = this
    }

    fun getItem(key: String): CustomItem? =
        loadedMap[key]

    companion object {
        lateinit var instance: CustomItemManager
            private set
    }

}