package org.bigcraft.customitems

import com.google.gson.Gson
import com.google.inject.CreationException
import com.google.inject.Guice
import com.google.inject.Stage
import me.wyne.wutils.common.loadable.Loader
import me.wyne.wutils.common.plugin.CompositeStep
import me.wyne.wutils.common.plugin.PluginStep
import me.wyne.wutils.common.plugin.Step
import me.wyne.wutils.common.plugin.StepScope
import me.wyne.wutils.config.Config
import me.wyne.wutils.i18n.I18n
import me.wyne.wutils.i18n.language.component.BukkitComponentAudience
import me.wyne.wutils.i18n.language.interpretation.ComponentInterpreters
import me.wyne.wutils.i18n.language.validation.EmptyValidator
import me.wyne.wutils.json.JsonRegistry
import me.wyne.wutils.log.*
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bigcraft.customitems.CustomItems.Companion.EMPTY_CONFIGURATION
import org.bigcraft.customitems.CustomItems.Companion.log
import org.bigcraft.customitems.module.CommandModule
import org.bigcraft.customitems.module.CustomItemModule
import org.bigcraft.customitems.module.PlaceholderModule
import org.bigcraft.customitems.module.PluginModule
import java.io.File
import java.util.concurrent.Executors

@Step(priority = 0, scope = StepScope.ENABLE)
object LoadDefaultConfig : PluginStep<CustomItems> {
    override fun run(plugin: CustomItems) {
        plugin.saveDefaultConfig()
        plugin.config.setDefaults(EMPTY_CONFIGURATION)
    }
}

@Step(priority = 1, scope = StepScope.ENABLE)
class InitializeLogger(private val logDirectory: File) : PluginStep<CustomItems> {
    @Suppress("DEPRECATION")
    override fun run(plugin: CustomItems) {
        Log.global = Log.builder()
            .setLogger(plugin.logger)
            .setConfig(
                ConfigurableLogConfig(
                    "Global",
                    Config.global,
                    BasicLogConfig(true, true, false, true, true, false)
                )
            )
            .setLogDirectory(logDirectory)
            .setFileWriteExecutor(Executors.newSingleThreadExecutor())
            .build()
        Log.global.deleteOlderLogs()

        log = Log4jFactory.createLogger(
            plugin,
            Log4jFactory.DEFAULT_FILE_MESSAGE_PATTERN,
            Level.valueOf(plugin.config.getString("log-level", "INFO")!!),
            logDirectory.path,
            Log.global
        )
    }
}

@Step(priority = 2, scope = StepScope.ENABLE)
class InitializeJson(private val gson: Gson, private val directory: File) : PluginStep<CustomItems> {
    override fun run(plugin: CustomItems) {
        JsonRegistry.global.apply {
            log = CustomItems.log
            gson = this@InitializeJson.gson
            directory = this@InitializeJson.directory
        }
    }
}

@Step(priority = 3, scope = StepScope.ENABLE)
object InitializeI18n : PluginStep<CustomItems> {
    override fun run(plugin: CustomItems) {
        I18n.global.apply {
            log = CustomItems.log
            audiences = BukkitComponentAudience(BukkitAudiences.create(plugin))
            clearLanguageMap()
            loadLanguage("lang/ru.yml", plugin)
            loadLanguage("lang/en.yml", plugin)
            loadDefaultResourceLanguage(plugin)
            loadLanguages(plugin)
            setDefaultLanguage(getDefaultLanguageCode(plugin))
            setComponentInterpreter(
                ComponentInterpreters.valueOf(
                    plugin.config.getString("serializer", "MINI_MESSAGE")!!
                ).get(EmptyValidator())
            )
            usePlayerLanguage = plugin.config.getBoolean("usePlayerLanguage", true)
        }
    }
}

@Step(priority = 4, scope = StepScope.ENABLE)
object InitializeInjector : PluginStep<CustomItems> {
    override fun run(plugin: CustomItems) {
        try {
            CustomItems.instance.injector = Guice.createInjector(
                Stage.PRODUCTION,
                PluginModule(plugin),
                CustomItemModule,
                PlaceholderModule,
                CommandModule
            )
        } catch (e: CreationException) {
            log.error("Guice injector creation exception", e)
        }
    }
}

@Step(priority = 5, scope = StepScope.ENABLE)
object InitializeConfig : CompositeStep<CustomItems>(ReloadConfig) {
    override fun before(plugin: CustomItems) {
        Config.global.apply {
            log = CustomItems.log
            setConfigGenerator(plugin, "config.yml")
            generateConfig()
        }
    }
}

@Step(priority = 6, scope = StepScope.ENABLE)
object InitializeLoader : PluginStep<CustomItems> {
    override fun run(plugin: CustomItems) {
        Loader.global.registerConfig(Loader.DEFAULT_PATH, plugin.config)
    }
}

@Step(priority = 7, scope = StepScope.ENABLE)
object Load : PluginStep<CustomItems> {
    override fun run(plugin: CustomItems) {
        JsonRegistry.global.load()
        Loader.global.load()
    }
}

@Step(scope = StepScope.DISABLE)
object Disable : CompositeStep<CustomItems>(Write)

@Step(scope = StepScope.RELOAD)
object Reload : CompositeStep<CustomItems>(ReloadConfig, InitializeLoader, InitializeI18n, Write, Load)

object Write : PluginStep<CustomItems> {
    override fun run(plugin: CustomItems) {
        JsonRegistry.global.write()
    }
}

object ReloadConfig : PluginStep<CustomItems> {
    override fun run(plugin: CustomItems) {
        plugin.reloadConfig()
        plugin.config.setDefaults(EMPTY_CONFIGURATION)
        Config.global.reloadConfig(plugin.config)
    }
}