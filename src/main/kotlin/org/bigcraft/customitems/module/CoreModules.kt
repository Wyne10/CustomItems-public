package org.bigcraft.customitems.module

import com.google.inject.AbstractModule
import org.bigcraft.customitems.aoe.AoeManager
import org.bigcraft.customitems.block.CustomBlockManager
import org.bigcraft.customitems.item.CustomItemManager
import org.bigcraft.customitems.listener.CompositorListener

private class CoreModule(private vararg val modules: Class<out Any>) : AbstractModule() {
    override fun configure() {
        modules.forEach { bind(it) }
    }
}

val CustomItemModule: AbstractModule = CoreModule(CustomItemManager::class.java, CustomBlockManager::class.java, AoeManager::class.java)

val CompositorModule: AbstractModule = CoreModule(CompositorListener:: class.java)