package org.bigcraft.customitems.cooldown

import me.wyne.wutils.common.cooldown.CooldownMap
import me.wyne.wutils.common.cooldown.Period
import me.wyne.wutils.config.configurables.AttributeConfigurable
import me.wyne.wutils.config.configurables.attribute.AttributeBase
import me.wyne.wutils.config.configurables.attribute.AttributeFactory
import me.wyne.wutils.config.configurables.attribute.AttributeMap
import me.wyne.wutils.config.configurables.attribute.ImmutableAttributeContainer
import org.bigcraft.customitems.config.getMillis
import org.bigcraft.customitems.item.CustomItem
import org.bigcraft.customitems.item.CustomItemManager
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import java.util.UUID

val COOLDOWN_MAP = AttributeMap(
    mapOf(
        "player-cooldown" to AttributeFactory { key, config -> PlayerCooldown(key, config.getMillis("$key.duration"), createCooldownAttributes(key, config)) },
        "global-cooldown" to AttributeFactory { key, config -> GlobalCooldown(key, config.getMillis("$key.duration"), createCooldownAttributes(key, config)) },
        "other-cooldown" to AttributeFactory { key, config -> OtherCooldown(key, config.getStringList("$key.items").toSet(), config.getMillis("$key.duration"), createCooldownAttributes(key, config)) },
    )
)

private fun createCooldownAttributes(key: String, config: ConfigurationSection) =
    AttributeConfigurable(ImmutableAttributeContainer(COOLDOWN_ATTRIBUTE_MAP, config.getConfigurationSection(key)))
        .getSet(CooldownAttribute::class.java)

abstract class Cooldown(
    key: String,
    durationMillis: Long,
    private val attributes: Set<CooldownAttribute<*>>
) : AttributeBase<Long>(key, durationMillis) {
    open fun isCooldowned(player: Player, customItem: CustomItem): Boolean {
        attributes.forEach { it.onTest(player, customItem, this) }
        return false
    }

    open fun apply(player: Player, customItem: CustomItem) {
        attributes.forEach { it.onApply(player, customItem, this) }
    }
}

class PlayerCooldown(
    key: String,
    durationMillis: Long,
    attributes: Set<CooldownAttribute<*>>
) : Cooldown(key, durationMillis, attributes) {

    private val cooldownMap = CooldownMap<UUID>()

    override fun isCooldowned(player: Player, customItem: CustomItem): Boolean {
        if (cooldownMap.isCooldowned(player.uniqueId))
            super.isCooldowned(player, customItem)
        return cooldownMap.isCooldowned(player.uniqueId)
    }

    override fun apply(player: Player, customItem: CustomItem) {
        super.apply(player, customItem)
        cooldownMap.put(player.uniqueId, value)
    }

}

class GlobalCooldown(
    key: String,
    durationMillis: Long,
    attributes: Set<CooldownAttribute<*>>
) : Cooldown(key, durationMillis, attributes) {

    private val cooldown = Period()

    override fun isCooldowned(player: Player, customItem: CustomItem): Boolean {
        if (!cooldown.isExpired)
            super.isCooldowned(player, customItem)
        return !cooldown.isExpired
    }

    override fun apply(player: Player, customItem: CustomItem) {
        super.apply(player, customItem)
        cooldown.put(value)
    }

}

class OtherCooldown(
    key: String,
    private val otherItems: Set<String>,
    private val durationMillis: Long,
    private val attributes: Set<CooldownAttribute<*>>
) : Cooldown(key, durationMillis, attributes) {

    override fun isCooldowned(player: Player, customItem: CustomItem): Boolean = false

    override fun apply(player: Player, customItem: CustomItem) {
        otherItems.mapNotNull { CustomItemManager.instance.getItem(it) }.forEach {
            val cooldown = PlayerCooldown("other-cooldown-${customItem.key}", durationMillis, attributes)
            it.cooldowns.add(cooldown)
            cooldown.apply(player, it)
        }
    }

}