package me.wyne.customitems.cooldown

import me.wyne.customitems.api.spi.Cooldown
import me.wyne.customitems.api.spi.CooldownContext
import me.wyne.customitems.api.spi.CooldownFeedback

/**
 * A [Cooldown] paired with the feedback configured beside it, so that starting and blocking are the
 * only two places the player is ever told anything.
 */
class ConfiguredCooldown(
    private val cooldown: Cooldown,
    private val feedback: List<CooldownFeedback>,
) : Cooldown by cooldown {

    override fun start(context: CooldownContext) {
        cooldown.start(context)
        feedback.forEach { it.onStarted(context, cooldown) }
    }

    fun notifyBlocked(context: CooldownContext) {
        feedback.forEach { it.onBlocked(context, cooldown) }
    }
}

/**
 * One item's cooldowns, fixed at load. Cooldowns another item imposes through `other-cooldown` are
 * resolved through [CooldownService] rather than being copied in, so this stays immutable.
 */
class CooldownGroup(private val byKey: Map<String, ConfiguredCooldown>) {

    val keys: Set<String>
        get() = byKey.keys

    operator fun get(key: String): ConfiguredCooldown? = byKey[key]

    private fun all(itemKey: String): Sequence<ConfiguredCooldown> =
        byKey.values.asSequence() + CooldownService.imposedOn(itemKey)

    /** Every cooldown currently holding this item back. */
    fun active(context: CooldownContext): List<ConfiguredCooldown> =
        all(context.item.key).filter { it.isActive(context) }.toList()

    fun isAnyActive(context: CooldownContext): Boolean =
        all(context.item.key).any { it.isActive(context) }

    fun start(keys: Collection<String>, context: CooldownContext) {
        keys.forEach { byKey[it]?.start(context) }
    }

    fun resetAll(context: CooldownContext) {
        all(context.item.key).forEach { it.reset(context) }
    }

    /** Tells the player why the activation was turned away, once per cooldown holding it back. */
    fun notifyBlocked(context: CooldownContext) {
        active(context).forEach { it.notifyBlocked(context) }
    }

    companion object {
        val EMPTY = CooldownGroup(emptyMap())
    }
}
