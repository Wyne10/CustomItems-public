package org.bigcraft.customitems.module

import com.google.inject.AbstractModule
import org.bigcraft.customitems.CustomItemsApi
import org.bigcraft.customitems.aoe.AoeManager
import org.bigcraft.customitems.block.CustomBlockManager
import org.bigcraft.customitems.item.CustomItemManager
import org.bigcraft.customitems.composition.CompositionService
import org.bigcraft.customitems.composition.CompositorListener

private class CoreModule(private vararg val modules: Class<out Any>) : AbstractModule() {
    override fun configure() {
        modules.forEach { bind(it) }
    }
}

val CustomItemModule: AbstractModule = CoreModule(CustomItemManager::class.java, CustomBlockManager::class.java, AoeManager::class.java)

val CompositorModule: AbstractModule = CoreModule(CompositionService::class.java, CompositorListener::class.java)

val ApiModule: AbstractModule = CoreModule(CustomItemsApi::class.java)
