package org.bigcraft.customitems.cooldown

import me.wyne.wutils.common.cooldown.CooldownMap
import me.wyne.wutils.common.cooldown.Period
import me.wyne.wutils.common.kotlin.config.getMillis
import me.wyne.wutils.config.configurables.AttributeConfigurable
import me.wyne.wutils.config.configurables.attribute.AttributeBase
import me.wyne.wutils.config.configurables.attribute.AttributeFactory
import me.wyne.wutils.config.configurables.attribute.AttributeMap
import me.wyne.wutils.config.configurables.attribute.ImmutableAttributeContainer
import org.bigcraft.customitems.CustomItems
import org.bigcraft.customitems.attributeSection
import org.bigcraft.customitems.item.CustomItem
import org.bigcraft.customitems.item.CustomItemManager
import org.bukkit.NamespacedKey
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.UUID

val COOLDOWN_MAP = AttributeMap(
    mapOf(
        "player-cooldown" to AttributeFactory { key, config -> config.attributeSection(key).let { s -> PlayerCooldown(key, s?.getMillis("duration") ?: 0L, createCooldownAttributes(s)) } },
        "global-cooldown" to AttributeFactory { key, config -> config.attributeSection(key).let { s -> GlobalCooldown(key, s?.getMillis("duration") ?: 0L, createCooldownAttributes(s)) } },
        "item-cooldown" to AttributeFactory { key, config -> config.attributeSection(key).let { s -> ItemCooldown(key, s?.getMillis("duration") ?: 0L, createCooldownAttributes(s)) } },
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
    open fun isCooldowned(player: Player, customItem: CustomItem, item: ItemStack): Boolean {
        attributes.forEach { it.onTest(player, customItem, item, this) }
        return false
    }

    open fun apply(player: Player, customItem: CustomItem, item: ItemStack) {
        attributes.forEach { it.onApply(player, customItem, item, this) }
    }

    abstract fun reset(player: Player, item: ItemStack)

    abstract fun getRemainingMillis(player: Player, item: ItemStack): Long
    abstract fun getRemainingFormat(player: Player, item: ItemStack, format: String): String
}

class PlayerCooldown(
    key: String,
    durationMillis: Long,
    attributes: Set<CooldownAttribute<*>>
) : Cooldown(key, durationMillis, attributes) {

    private val cooldownMap = CooldownMap<UUID>()

    override fun isCooldowned(player: Player, customItem: CustomItem, item: ItemStack): Boolean {
        if (cooldownMap.isCooldowned(player.uniqueId))
            super.isCooldowned(player, customItem, item)
        return cooldownMap.isCooldowned(player.uniqueId)
    }

    override fun apply(player: Player, customItem: CustomItem, item: ItemStack) {
        super.apply(player, customItem, item)
        cooldownMap.put(player.uniqueId, value)
    }

    override fun reset(player: Player, item: ItemStack) =
        cooldownMap.remove(player.uniqueId)

    override fun getRemainingMillis(player: Player, item: ItemStack): Long =
        cooldownMap.getRemaining(player.uniqueId)

    override fun getRemainingFormat(player: Player, item: ItemStack, format: String): String =
        cooldownMap.getRemainingStringFormat(player.uniqueId, format)

}

class GlobalCooldown(
    key: String,
    durationMillis: Long,
    attributes: Set<CooldownAttribute<*>>
) : Cooldown(key, durationMillis, attributes) {

    private val cooldown = Period()

    override fun isCooldowned(player: Player, customItem: CustomItem, item: ItemStack): Boolean {
        if (!cooldown.isExpired)
            super.isCooldowned(player, customItem, item)
        return !cooldown.isExpired
    }

    override fun apply(player: Player, customItem: CustomItem, item: ItemStack) {
        super.apply(player, customItem, item)
        cooldown.put(value)
    }

    override fun reset(player: Player, item: ItemStack) =
        cooldown.stop()

    override fun getRemainingMillis(player: Player, item: ItemStack): Long =
        cooldown.getRemaining()

    override fun getRemainingFormat(player: Player, item: ItemStack, format: String): String =
        cooldown.getRemainingStringFormat(format)

}

class ItemCooldown(
    key: String,
    durationMillis: Long,
    attributes: Set<CooldownAttribute<*>>
) : Cooldown(key, durationMillis, attributes) {

    private val tagKey = NamespacedKey(
        CustomItems.instance,
        "cooldown-${key.lowercase().replace(Regex("[^a-z0-9._-]"), "-")}"
    )

    private fun readPeriod(item: ItemStack): Period {
        val finishAt = item.itemMeta?.persistentDataContainer?.get(tagKey, PersistentDataType.LONG)
            ?: return Period()
        return Period(finishAt)
    }

    override fun isCooldowned(player: Player, customItem: CustomItem, item: ItemStack): Boolean {
        val cooldowned = !readPeriod(item).isExpired
        if (cooldowned)
            super.isCooldowned(player, customItem, item)
        return cooldowned
    }

    override fun apply(player: Player, customItem: CustomItem, item: ItemStack) {
        super.apply(player, customItem, item)
        item.editMeta { meta ->
            meta.persistentDataContainer.set(tagKey, PersistentDataType.LONG, System.currentTimeMillis() + value)
        }
    }

    override fun reset(player: Player, item: ItemStack) {
        item.editMeta { meta -> meta.persistentDataContainer.remove(tagKey) }
    }

    override fun getRemainingMillis(player: Player, item: ItemStack): Long =
        readPeriod(item).getRemaining()

    override fun getRemainingFormat(player: Player, item: ItemStack, format: String): String =
        readPeriod(item).getRemainingStringFormat(format)

}

class OtherCooldown(
    key: String,
    private val otherItems: Set<String>,
    private val durationMillis: Long,
    private val attributes: Set<CooldownAttribute<*>>
) : Cooldown(key, durationMillis, attributes) {

    override fun isCooldowned(player: Player, customItem: CustomItem, item: ItemStack): Boolean = false

    override fun apply(player: Player, customItem: CustomItem, item: ItemStack) {
        otherItems.mapNotNull { CustomItemManager.instance.getItem(it) }.forEach {
            val cooldown = PlayerCooldown("other-cooldown-${customItem.key}", durationMillis, attributes)
            it.cooldowns.add(cooldown)
            cooldown.apply(player, it, item)
        }
    }

    override fun reset(player: Player, item: ItemStack) = Unit

    override fun getRemainingMillis(player: Player, item: ItemStack): Long = 0
    override fun getRemainingFormat(player: Player, item: ItemStack, format: String) = format

}
