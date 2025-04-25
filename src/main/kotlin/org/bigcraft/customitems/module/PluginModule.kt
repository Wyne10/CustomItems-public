package org.bigcraft.customitems.module

import com.google.inject.AbstractModule
import org.bigcraft.customitems.CustomItems
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.java.JavaPlugin

class PluginModule(private val plugin: CustomItems) : AbstractModule() {
    override fun configure() {
        bind(CustomItems::class.java)
            .toInstance(plugin)
        bind(JavaPlugin::class.java)
            .toInstance(plugin)
        bind(FileConfiguration::class.java)
            .toInstance(plugin.getConfig())
    }
}
