package me.wyne.customitems.cooldown

import me.wyne.wutils.common.cooldown.CooldownMap
import me.wyne.wutils.common.cooldown.Period
import me.wyne.wutils.common.kotlin.config.deep
import me.wyne.wutils.common.kotlin.config.getMillis
import me.wyne.customitems.CustomItems
import me.wyne.customitems.api.spi.Cooldown
import me.wyne.customitems.api.spi.CooldownContext
import me.wyne.customitems.api.spi.FactoryRegistry
import me.wyne.customitems.item.CustomItemManager
import me.wyne.customitems.spi.Registries
import org.bukkit.NamespacedKey
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.UUID

internal fun registerCooldowns(registry: FactoryRegistry<Cooldown>) {
    registry.register("player-cooldown") { key, config -> PlayerCooldown(key, config.duration(key)) }
    registry.register("global-cooldown") { key, config -> GlobalCooldown(key, config.duration(key)) }
    registry.register("item-cooldown") { key, config -> ItemCooldown(key, config.duration(key)) }
    registry.register("other-cooldown") { key, config ->
        OtherCooldown(key, config.deep(key).getStringList("items").toSet(), config.duration(key), config.deep(key))
    }
}

private fun ConfigurationSection.duration(key: String) = deep(key).getMillis("duration", 0L)

/** Held against the player, so every stack of the item shares it. */
class PlayerCooldown(
    private val key: String,
    private val durationMillis: Long,
) : Cooldown {

    private val cooldowns = CooldownMap<UUID>()

    override fun getKey() = key

    override fun isActive(context: CooldownContext) = cooldowns.isCooldowned(context.player.uniqueId)

    override fun start(context: CooldownContext) = cooldowns.put(context.player.uniqueId, durationMillis)

    override fun reset(context: CooldownContext) = cooldowns.remove(context.player.uniqueId)

    override fun getRemainingMillis(context: CooldownContext) = cooldowns.getRemaining(context.player.uniqueId)
}

/** Held against the server, so the item is on cooldown for everyone at once. */
class GlobalCooldown(
    private val key: String,
    private val durationMillis: Long,
) : Cooldown {

    private val period = Period()

    override fun getKey() = key

    override fun isActive(context: CooldownContext) = !period.isExpired

    override fun start(context: CooldownContext) = period.put(durationMillis)

    override fun reset(context: CooldownContext) = period.stop()

    override fun getRemainingMillis(context: CooldownContext) = period.getRemaining()
}

/** Held against the stack itself, so it survives being dropped, traded or stored. */
class ItemCooldown(
    private val key: String,
    private val durationMillis: Long,
) : Cooldown {

    private val tagKey = NamespacedKey(
        CustomItems.instance,
        "cooldown-${key.lowercase().replace(Regex("[^a-z0-9._-]"), "-")}"
    )

    override fun getKey() = key

    override fun isActive(context: CooldownContext) = !readPeriod(context.stack).isExpired

    override fun start(context: CooldownContext) {
        context.stack.editMeta { meta ->
            meta.persistentDataContainer.set(tagKey, PersistentDataType.LONG, System.currentTimeMillis() + durationMillis)
        }
    }

    override fun reset(context: CooldownContext) {
        context.stack.editMeta { meta -> meta.persistentDataContainer.remove(tagKey) }
    }

    override fun getRemainingMillis(context: CooldownContext) = readPeriod(context.stack).getRemaining()

    private fun readPeriod(stack: ItemStack): Period {
        val finishAt = stack.itemMeta?.persistentDataContainer?.get(tagKey, PersistentDataType.LONG)
            ?: return Period()
        return Period(finishAt)
    }
}

/**
 * Puts a cooldown on *other* items rather than on this one, so a group of items can share a single
 * usage window.
 *
 * The cooldown each target receives is built once here and handed to [CooldownService]; starting
 * this one only starts those. It is never active itself.
 */
class OtherCooldown(
    private val key: String,
    targets: Set<String>,
    durationMillis: Long,
    section: ConfigurationSection,
) : Cooldown {

    private val imposed = targets.associateWith {
        ConfiguredCooldown(
            PlayerCooldown(key, durationMillis),
            Registries.cooldownFeedbackFactories.createAll(section).toList()
        ).also { cooldown -> CooldownService.impose(it, cooldown) }
    }

    override fun getKey() = key

    override fun isActive(context: CooldownContext) = false

    override fun start(context: CooldownContext) {
        imposed.forEach { (targetKey, cooldown) ->
            val target = CustomItemManager.instance[targetKey] ?: return@forEach
            cooldown.start(CooldownContext(context.player, target, context.stack))
        }
    }

    override fun reset(context: CooldownContext) = Unit

    override fun getRemainingMillis(context: CooldownContext) = 0L
}
