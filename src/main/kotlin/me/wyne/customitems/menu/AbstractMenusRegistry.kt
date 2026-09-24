package me.wyne.customitems.menu

import com.google.inject.Singleton
import ru.abstractmenus.api.Types

@Singleton
class AbstractMenusRegistry {

    init {
        Types.registerItemProperty("customItem", CustomItemProperty::class.java, CustomItemProperty.Factory)
    }

}