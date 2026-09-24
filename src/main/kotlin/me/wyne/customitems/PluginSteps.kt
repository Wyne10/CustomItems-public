package me.wyne.customitems

import com.google.gson.Gson
import com.google.inject.CreationException
import com.google.inject.Guice
import com.google.inject.Stage
import me.wyne.wutils.common.loadable.Loader
import me.wyne.wutils.common.plugin.CompositeStep
import me.wyne.wutils.common.plugin.LevelWrapper
import me.wyne.wutils.common.plugin.LoggerWrapper
import me.wyne.wutils.common.plugin.PluginStep
import me.wyne.wutils.common.plugin.Step
import me.wyne.wutils.common.plugin.StepScope
import me.wyne.wutils.common.scheduler.Schedulers
import me.wyne.wutils.config.Config
import me.wyne.wutils.i18n.I18n
import me.wyne.wutils.i18n.PluginI18nBuilder
import me.wyne.wutils.i18n.language.component.BukkitComponentAudiences
import me.wyne.wutils.i18n.language.validation.EmptyValidator
import me.wyne.customitems.api.CustomItemsApi
import me.wyne.wutils.json.JsonRegistry
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import me.wyne.customitems.CustomItems.Companion.EMPTY_CONFIGURATION
import me.wyne.customitems.CustomItems.Companion.logger
import me.wyne.wutils.i18n.language.interpretation.ComponentInterpreters
import me.wyne.customitems.activator.registerActivatorAttributes
import me.wyne.customitems.activator.registerActivatorTypes
import me.wyne.customitems.activator.registerItemIdentifiers
import me.wyne.customitems.activator.condition.registerActivatorConditions
import me.wyne.customitems.item.registerItemAttributes
import me.wyne.customitems.block.registerBlockAttributeTypes
import me.wyne.customitems.context.registerBuiltinPlaceholders
import me.wyne.customitems.cooldown.registerCooldownFeedback
import me.wyne.customitems.cooldown.registerCooldowns
import me.wyne.customitems.restriction.registerRestrictionTypes
import me.wyne.customitems.module.*
import me.wyne.customitems.spi.Registries
import org.bukkit.configuration.serialization.ConfigurationSerialization
import java.io.File

@Step(priority = 0, scope = StepScope.LOAD)
object RegisterLootPool : PluginStep<CustomItems> {
    override fun run(plugin: CustomItems) {
        ConfigurationSerialization.registerClass(CustomKeyedLoot::class.java)
    }
}

@Step(priority = 0, scope = StepScope.ENABLE)
object LoadDefaultConfig : PluginStep<CustomItems> {
    override fun run(plugin: CustomItems) {
        plugin.saveDefaultConfig()
        plugin.config.setDefaults(EMPTY_CONFIGURATION)
    }
}

@Step(priority = 1, scope = StepScope.ENABLE)
object InitializeLogger : PluginStep<CustomItems> {
    override fun run(plugin: CustomItems) {
        logger = LoggerWrapper(plugin.slF4JLogger, LevelWrapper.valueOf(plugin.config.getString("logLevel", "INFO")!!))
    }
}

@Step(priority = 2, scope = StepScope.ENABLE)
class InitializeJson(private val gson: Gson, private val directory: File) : PluginStep<CustomItems> {
    override fun run(plugin: CustomItems) {
        JsonRegistry.global.apply {
            gson = this@InitializeJson.gson
            setDirectory(this@InitializeJson.directory)
        }
    }
}

@Step(priority = 3, scope = StepScope.ENABLE)
object InitializeI18n : PluginStep<CustomItems> {
    override fun run(plugin: CustomItems) {
        I18n.global = PluginI18nBuilder(plugin)
            .setLogger(logger)
            .setComponentAudience(BukkitComponentAudiences(BukkitAudiences.create(plugin)))
            .setComponentInterpreter(
                ComponentInterpreters.valueOf(
                    plugin.config.getString("serializer", "ITEM_MINI_MESSAGE")!!
                ).get(EmptyValidator()))
            .setUsePlayerLanguage(plugin.config.getBoolean("usePlayerLanguage", true))
            .loadLanguage("lang/ru.yml")
            .loadLanguage("lang/en.yml")
            .build()
    }
}

@Step(priority = 4, scope = StepScope.ENABLE)
object RegisterVocabulary : PluginStep<CustomItems> {
    override fun run(plugin: CustomItems) {
        registerItemAttributes(Registries.itemAttributeFactories)
        registerItemIdentifiers(Registries.itemIdentifierInstances)
        registerActivatorTypes(Registries.activatorTypeInstances)
        registerRestrictionTypes(Registries.restrictionTypeInstances)
        registerBlockAttributeTypes(Registries.blockAttributeTypeInstances)
        registerCooldowns(Registries.cooldownFactories)
        registerCooldownFeedback(Registries.cooldownFeedbackFactories)
        registerBuiltinPlaceholders(Registries::registerItemPlaceholders)
        registerActivatorAttributes(Registries.activatorAttributeFactories)
        registerActivatorConditions(Registries.activatorConditionFactories)
        CustomItemsApi.setRegistries(Registries)
    }
}

@Step(priority = 5, scope = StepScope.ENABLE)
object InitializeInjector : PluginStep<CustomItems> {
    override fun run(plugin: CustomItems) {
        try {
            CustomItems.instance.injector = Guice.createInjector(
                Stage.PRODUCTION,
                PluginModule(plugin),
                CustomItemModule,
                CompositorModule,
                ApiModule,
                PlaceholderModule,
                CommandModule,
                AbstractMenusModule
            )
        } catch (e: CreationException) {
            logger.error("Guice injector creation exception", e)
        }
    }
}

@Step(priority = 6, scope = StepScope.ENABLE)
object InitializeConfig : CompositeStep<CustomItems>(ReloadConfig) {
    override fun before(plugin: CustomItems) {
        Config.global.apply {
            this.logger = CustomItems.logger
            setConfigGenerator(plugin, "config.yml")
            generateConfig()
        }
    }
}

@Step(priority = 7, scope = StepScope.ENABLE)
object InitializeLoader : PluginStep<CustomItems> {
    override fun run(plugin: CustomItems) {
        Loader.global.registerConfig(Loader.DEFAULT_PATH, plugin.config)
    }
}

/**
 * Items are loaded a tick after enabling rather than during it, so that plugins depending on
 * CustomItems have had their own `onEnable` to register into [Registries] first.
 */
@Step(priority = 8, scope = StepScope.ENABLE)
object Load : PluginStep<CustomItems> {
    override fun run(plugin: CustomItems) {
        LoadJson.run(plugin)
        Schedulers.sync().runLater({ LoadItems.run(plugin) }, 1)
    }
}

object LoadJson : PluginStep<CustomItems> {
    override fun run(plugin: CustomItems) {
        JsonRegistry.global.load()
    }
}

object LoadItems : PluginStep<CustomItems> {
    override fun run(plugin: CustomItems) {
        Registries.close()
        Registries.registerDispatchers(plugin)
        Registries.clearBindings()
        Loader.global.load(plugin)
    }
}

@Step(scope = StepScope.DISABLE)
object Disable : CompositeStep<CustomItems>(Write, UnregisterDispatchers)

object UnregisterDispatchers : PluginStep<CustomItems> {
    override fun run(plugin: CustomItems) {
        Registries.unregisterDispatchers()
    }
}

@Step(scope = StepScope.RELOAD)
object Reload : CompositeStep<CustomItems>(ReloadConfig, InitializeLogger, InitializeLoader, InitializeI18n, Write, LoadJson, LoadItems)

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