package org.bigcraft.customitems.cooldown

import me.wyne.wutils.common.cooldown.CooldownMap
import me.wyne.wutils.common.cooldown.Period
import me.wyne.wutils.common.kotlin.config.getMillis
import me.wyne.wutils.config.configurables.AttributeConfigurable
import me.wyne.wutils.config.configurables.attribute.AttributeBase
import me.wyne.wutils.config.configurables.attribute.AttributeFactory
import me.wyne.wutils.config.configurables.attribute.AttributeMap
import me.wyne.wutils.config.configurables.attribute.ImmutableAttributeContainer
import org.bigcraft.customitems.attributeSection
import org.bigcraft.customitems.item.CustomItem
import org.bigcraft.customitems.item.CustomItemManager
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import java.util.UUID

val COOLDOWN_MAP = AttributeMap(
    mapOf(
        "player-cooldown" to AttributeFactory { key, config -> config.attributeSection(key).let { s -> PlayerCooldown(key, s?.getMillis("duration") ?: 0L, createCooldownAttributes(s)) } },
        "global-cooldown" to AttributeFactory { key, config -> config.attributeSection(key).let { s -> GlobalCooldown(key, s?.getMillis("duration") ?: 0L, createCooldownAttributes(s)) } },
        "other-cooldown" to AttributeFactory { key, config -> config.attributeSection(key).let { s -> OtherCooldown(key, s?.getStringList("items").orEmpty().toSet(), s?.getMillis("duration") ?: 0L, createCooldownAttributes(s)) } },
    )
)

private fun createCooldownAttributes(section: ConfigurationSection?) =
    AttributeConfigurable(ImmutableAttributeContainer(COOLDOWN_ATTRIBUTE_MAP, section))
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

    abstract fun getRemainingMillis(player: Player): Long
    abstract fun getRemainingFormat(player: Player, format: String): String
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

    override fun getRemainingMillis(player: Player): Long =
        cooldownMap.getRemaining(player.uniqueId)

    override fun getRemainingFormat(player: Player, format: String): String =
        cooldownMap.getRemainingStringFormat(player.uniqueId, format)

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

    override fun getRemainingMillis(player: Player): Long =
        cooldown.getRemaining()

    override fun getRemainingFormat(player: Player, format: String): String =
        cooldown.getRemainingStringFormat(format)

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

    override fun getRemainingMillis(player: Player): Long = 0
    override fun getRemainingFormat(player: Player, format: String) = format

}