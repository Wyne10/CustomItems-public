package me.wyne.customitems

import com.google.inject.Inject
import com.google.inject.Singleton
import me.wyne.customitems.api.CustomItemProvider
import me.wyne.customitems.api.CustomItemsApi
import me.wyne.customitems.block.CustomBlockManager
import me.wyne.customitems.item.CustomItemManager
import org.bukkit.plugin.ServicePriority

@Singleton
class CustomItemsApi @Inject constructor(plugin: CustomItems, customItemManager: CustomItemManager, customBlockManager: CustomBlockManager) {
    init {
        plugin.server.servicesManager.register(CustomItemProvider::class.java, customItemManager, plugin, ServicePriority.Normal)
        CustomItemsApi.setProvider(customItemManager)
        CustomItemsApi.setBlockRegistry(customBlockManager)
    }
}