package me.wyne.customitems.cooldown

import me.wyne.customitems.CustomItems
import me.wyne.customitems.spi.Registries
import org.bukkit.configuration.ConfigurationSection

/**
 * Builds an item's [CooldownGroup] from its `cooldowns` section. Each entry's own section carries
 * both the cooldown's settings and the feedback configured beside them.
 */
internal fun createCooldowns(itemKey: String, section: ConfigurationSection): CooldownGroup {
    val cooldowns = Registries.cooldownFactories.createAllMap(section)
    if (cooldowns.isEmpty()) return CooldownGroup.EMPTY
    section.getKeys(false)
        .filterNot { cooldowns.containsKey(it) }
        .forEach { CustomItems.logger.warn("Unknown cooldown '{}' on item '{}'", it, itemKey) }
    return CooldownGroup(
        cooldowns.mapValues { (key, cooldown) ->
            ConfiguredCooldown(
                cooldown,
                Registries.cooldownFeedbackFactories.createAll(section.deepOrEmpty(key)).toList()
            )
        }
    )
}

private fun ConfigurationSection.deepOrEmpty(key: String) =
    getConfigurationSection(key) ?: CustomItems.EMPTY_CONFIGURATION
