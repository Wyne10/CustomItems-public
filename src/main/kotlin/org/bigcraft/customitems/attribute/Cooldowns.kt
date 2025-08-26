package org.bigcraft.customitems.attribute

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
import org.bukkit.event.Event
import org.bukkit.inventory.ItemStack
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
    open fun isCooldowned(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean {
        attributes.forEach { it.onTest(player, item, this) }
        return false
    }

    open fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        attributes.forEach { it.onApply(player, item, this) }
    }
}

class PlayerCooldown(
    key: String,
    durationMillis: Long,
    attributes: Set<CooldownAttribute<*>>
) : Cooldown(key, durationMillis, attributes) {

    private val cooldownMap = CooldownMap<UUID>()

    override fun isCooldowned(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean {
        if (cooldownMap.isCooldowned(player.uniqueId))
            super.isCooldowned(event, player, customItem, item)
        return cooldownMap.isCooldowned(player.uniqueId)
    }

    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        super.apply(event, player, customItem, item)
        cooldownMap.put(player.uniqueId, value)
    }

}

class GlobalCooldown(
    key: String,
    durationMillis: Long,
    attributes: Set<CooldownAttribute<*>>
) : Cooldown(key, durationMillis, attributes) {

    private val cooldown = Period()

    override fun isCooldowned(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean {
        if (!cooldown.isExpired)
            super.isCooldowned(event, player, customItem, item)
        return !cooldown.isExpired
    }

    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        super.apply(event, player, customItem, item)
        cooldown.put(value)
    }

}

class OtherCooldown(
    key: String,
    private val otherItems: Set<String>,
    durationMillis: Long,
    attributes: Set<CooldownAttribute<*>>
) : Cooldown(key, durationMillis, attributes) {

    private val otherCooldown = PlayerCooldown("other-cooldown-${UUID.randomUUID()}", durationMillis, attributes)

    override fun isCooldowned(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean = false

    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        otherItems.mapNotNull { CustomItemManager.instance.getItem(it) }.forEach { it.cooldowns.add(otherCooldown) }
        otherCooldown.apply(event, player, customItem, item)
    }

}