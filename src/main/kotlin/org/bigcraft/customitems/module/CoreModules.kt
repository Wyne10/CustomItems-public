package org.bigcraft.customitems.module

import com.google.inject.AbstractModule
import org.bigcraft.customitems.item.CustomItemManager

private class CoreModule(private vararg val modules: Class<out Any>) : AbstractModule() {
    override fun configure() {
        modules.forEach { bind(it) }
    }
}

val CustomItemModule: AbstractModule = CoreModule(CustomItemManager::class.java)