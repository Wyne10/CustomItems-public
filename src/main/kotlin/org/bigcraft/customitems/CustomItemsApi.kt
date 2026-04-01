package org.bigcraft.customitems

import com.google.inject.Inject
import com.google.inject.Singleton
import org.bigcraft.customitems.api.CustomItemProvider
import org.bigcraft.customitems.api.CustomItemsApi
import org.bigcraft.customitems.item.CustomItemManager
import org.bukkit.plugin.ServicePriority

@Singleton
class CustomItemsApi @Inject constructor(plugin: CustomItems, customItemManager: CustomItemManager) {
    init {
        plugin.server.servicesManager.register(CustomItemProvider::class.java, customItemManager, plugin, ServicePriority.Normal)
        CustomItemsApi.setProvider(customItemManager)
    }
}