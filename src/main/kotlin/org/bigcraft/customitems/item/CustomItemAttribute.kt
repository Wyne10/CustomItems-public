package org.bigcraft.customitems.item

import me.wyne.wutils.common.event.EventRegistry
import me.wyne.wutils.common.event.RegisterableListener
import me.wyne.wutils.config.configurables.attribute.AttributeBase

interface CustomItemAttribute {
    fun apply(item: CustomItem)
}

abstract class RegisterableCustomItemAttribute<T>(
    key: String,
    value: T,
    private val eventRegistry: EventRegistry,
) : AttributeBase<T>(key, value), CustomItemAttribute, RegisterableListener {

    protected lateinit var item: CustomItem
        private set

    final override fun apply(item: CustomItem) {
        if (value is Boolean && !(value as Boolean))
            return
        if (::item.isInitialized)
            throw UnsupportedOperationException("Registerable attribute should only be applied once!")
        this.item = item
        eventRegistry.register(this)
    }

}