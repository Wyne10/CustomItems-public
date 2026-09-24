package me.wyne.customitems

import com.google.gson.GsonBuilder
import com.google.inject.*
import me.wyne.wutils.common.plugin.CompositeJavaPlugin
import org.bukkit.configuration.MemoryConfiguration
import org.slf4j.Logger

class CustomItems : CompositeJavaPlugin<CustomItems>() {

    lateinit var injector: Injector
        internal set

    override fun init() {
        instance = this
        addSteps(
            RegisterLootPool,
            LoadDefaultConfig,
            InitializeJson(GsonBuilder()
                    .enableComplexMapKeySerialization()
                    .setPrettyPrinting()
                    .create(), dataFolder),
            InitializeLogger,
            InitializeI18n,
            RegisterVocabulary,
            InitializeInjector,
            InitializeConfig,
            InitializeLoader,
            Load,
            Disable,
            Reload
        )
    }

    companion object {
        lateinit var instance: CustomItems
            private set
        lateinit var logger: Logger
            internal set
        val EMPTY_CONFIGURATION = MemoryConfiguration()
    }

}