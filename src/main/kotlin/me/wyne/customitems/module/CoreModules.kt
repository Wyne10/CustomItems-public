package me.wyne.customitems.module

import com.google.inject.AbstractModule
import me.wyne.customitems.CustomItemsApi
import me.wyne.customitems.aoe.AoeManager
import me.wyne.customitems.block.CustomBlockManager
import me.wyne.customitems.item.CustomItemManager
import me.wyne.customitems.composition.CompositionService
import me.wyne.customitems.composition.CompositorListener

private class CoreModule(private vararg val modules: Class<out Any>) : AbstractModule() {
    override fun configure() {
        modules.forEach { bind(it) }
    }
}

val CustomItemModule: AbstractModule = CoreModule(CustomItemManager::class.java, CustomBlockManager::class.java, AoeManager::class.java)

val CompositorModule: AbstractModule = CoreModule(CompositionService::class.java, CompositorListener::class.java)

val ApiModule: AbstractModule = CoreModule(CustomItemsApi::class.java)
